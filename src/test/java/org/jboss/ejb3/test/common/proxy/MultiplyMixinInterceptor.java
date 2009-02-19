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
package org.jboss.ejb3.test.common.proxy;

import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * MultiplyMixinInterceptor
 * 
 * A test ChainableInvocationHandler which ignores the 
 * specified input and replaces it with that specified
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MultiplyMixinInterceptor implements Interceptor, Multipliable
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The method we'll intercept and handle
    */
   private static final Method MULTIPLY_METHOD;
   static
   {
      try
      {
         MULTIPLY_METHOD = Multipliable.class.getMethod("multiply", new Class<?>[]
         {int[].class});
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
      // Get arguments
      MethodInvocation methodInvocation = (MethodInvocation) invocation;
      Object[] args = methodInvocation.getArguments();

      // Do we handle this invocation?
      if (this.handlesInvocation(methodInvocation))
      {
         // Invoke
         return new Integer(this.multiply((int[]) args[0]));
      }
      // We don't handle the invocation, send along the chain
      else
      {
         return invocation.invokeNext();
      }
   }

   /**
    * Returns the product of the specified arguments
    */
   public int multiply(int... args)
   {
      // Initialize
      int result = 1;

      // For each argument, get the product
      for (int arg : args)
      {
         result *= arg;
      }

      // Return
      return result;

   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods  -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Determines whether this processor may handle the invocation
    */
   private boolean handlesInvocation(MethodInvocation invocation)
   {
      /*
       * Determine if we'll handle this invocation
       */

      if (invocation.getActualMethod().equals(MULTIPLY_METHOD))
      {
         return true;
      }

      // Did not meet requirements
      return false;
   }

}
