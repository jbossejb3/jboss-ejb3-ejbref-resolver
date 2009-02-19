/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
  *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.common.proxy.plugins.async;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.security.SecurityContext;

/**
 * AsyncInterceptor
 * 
 * An interceptor that invokes upon the chain in a separate Thread,
 * saving a reference to the Future result
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsyncInterceptor implements Interceptor, AsyncProvider
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final ThreadLocal<Future<Object>> LAST_INVOKED_RESULT = new ThreadLocal<Future<Object>>();

   private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

   private static final Method METHOD_GET_FUTURE_RESULT;
   static
   {
      try
      {
         METHOD_GET_FUTURE_RESULT = AsyncProvider.class.getMethod("getFutureResult", new Class<?>[]
         {});
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public String getName()
   {
      return this.getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      // Ensure we've got a method invocation
      assert invocation instanceof MethodInvocation : this.getName() + " is applicable only for "
            + MethodInvocation.class.getSimpleName() + ", instead got: " + invocation.getClass().getName();

      // Get the method information
      MethodInvocation methodInvocation = (MethodInvocation) invocation;
      Method method = methodInvocation.getActualMethod();
      Object[] args = methodInvocation.getArguments();

      // Are we trying to get the future result?
      if (this.isGetFutureResultInvocation(method))
      {
         // Return the future result
         return this.getFutureResult();
      }

      // Get the delegate
      Object delegate = invocation.getTargetObject();

      SecurityContext sc = SecurityActions.getSecurityContext();

      // Construct the async call
      Callable<Object> asyncInvocation = new AsyncTask(delegate, method, args, sc);

      // Invoke as async
      Future<Object> asyncResult = EXECUTOR.submit(asyncInvocation);

      // Set the async result
      LAST_INVOKED_RESULT.set(asyncResult);

      // Return a null or 0 value; we've been spawned off
      return DummyReturnValues.getDummyReturnValue(method.getReturnType());
   }

   /**
    * Obtains the result of the last asynchronous
    * invocation performed as a Future
    * 
    * @return
    */
   public Future<?> getFutureResult()
   {
      Future<?> result = LAST_INVOKED_RESULT.get();
      assert result != null : "No last invoked result is available";
      return result;
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods  -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Determines whether this invocation is to obtain 
    * the future result
    */
   private boolean isGetFutureResultInvocation(Method method)
   {
      if (method.equals(METHOD_GET_FUTURE_RESULT))
      {
         return true;
      }

      return false;
   }

   // ------------------------------------------------------------------------------||
   // Inner Classes ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * A task to send a process off 
    */
   private static class AsyncTask implements Callable<Object>
   {

      private Object proxy;

      private Method method;

      private Object args[];

      /** Optional security context */
      private SecurityContext sc;

      public AsyncTask(Object proxy, Method method, Object[] args, SecurityContext sc)
      {
         this.proxy = proxy;
         this.method = method;
         this.args = args;
         this.sc = sc;
      }

      public Object call() throws Exception
      {
         // Invoke upon the proxy
         SecurityContext prevSC = null;
         try
         {
            if (sc != null)
            {
               prevSC = SecurityActions.getSecurityContext();
               SecurityActions.setSecurityContext(sc);
            }
            return method.invoke(proxy, args);
         }
         catch (InvocationTargetException e)
         {
            Throwable cause = e.getCause();
            if (cause instanceof Exception)
               throw (Exception) cause;
            throw e;
         }
         catch (Throwable t)
         {
            throw new Exception("Exception encountered in Asynchronous Invocation", t);
         }
         finally
         {
            if (sc != null)
               SecurityActions.setSecurityContext(prevSC);
         }
      }
   }

   /**
    * DummyReturnValues
    * 
    * Utility class to return a dummy value when the task has 
    * been spawned to a new Thread
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private static final class DummyReturnValues
   {

      private DummyReturnValues()
      {
      }

      /**
       * Gets a dummy return value (usually either a null or a 0-value)
       * for the expected return type
       * 
       * @param expectedType
       * @return
       */
      public static Object getDummyReturnValue(Class<?> expectedType)
      {
         // Objects
         if (!expectedType.isPrimitive())
         {
            return null;
         }

         // int
         if (expectedType.equals(int.class))
         {
            return 0;
         }
         // long
         if (expectedType.equals(long.class))
         {
            return 0L;
         }
         // short
         if (expectedType.equals(short.class))
         {
            return 0;
         }
         // byte
         if (expectedType.equals(byte.class))
         {
            return 0x0;
         }
         // double
         if (expectedType.equals(double.class))
         {
            return 0.0;
         }
         // float
         if (expectedType.equals(float.class))
         {
            return 0.0;
         }
         // boolean
         if (expectedType.equals(boolean.class))
         {
            return false;
         }
         // char
         if (expectedType.equals(char.class))
         {
            return 0;
         }
         // void
         if (expectedType.equals(void.class))
         {
            return null;
         }

         // If we've reached here, there's an error
         throw new RuntimeException("Did not return proper dummy value for expected type: " + expectedType);
      }

   }

}
