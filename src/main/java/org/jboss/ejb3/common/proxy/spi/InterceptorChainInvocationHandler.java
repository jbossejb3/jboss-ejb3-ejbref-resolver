/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.common.proxy.spi;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;

/**
 * InterceptorChainInvocationHandler
 * 
 * A Proxy InvocationHandler which will first pass the invocation
 * through an interceptor chain before carrying on
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class InterceptorChainInvocationHandler implements Serializable, InvocationHandler
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private final Interceptor[] interceptorChain;

   private final Object target;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public InterceptorChainInvocationHandler(final Interceptor[] interceptorChain, final Object target)
   {
      this.interceptorChain = interceptorChain;
      this.target = target;
   }

   public InterceptorChainInvocationHandler(final List<Interceptor> interceptorChain, final Object target)
   {
      this(interceptorChain.toArray(new Interceptor[]
      {}), target);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
   {
      // Do we already have a MethodInvocation?

      // Create an invocation
      MethodInvocation sri = this.constructMethodInvocation(method, args);
      this.addArgumentsToInvocation(sri, args);

      // Return
      return sri.invokeNext();

   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructs a MethodInvocation from the specified Method and
    * arguments
    * 
    * @param method
    * @param args
    * @return
    */
   protected MethodInvocation constructMethodInvocation(Method method, Object[] args)
   {
      long hash = MethodHashing.calculateHash(method);
      MethodInvocation sri = new MethodInvocation(this.getInterceptorChain(), hash, method, method, null);
      sri.setTargetObject(this.getTarget());
      return sri;
   }

   /**
    * Sets the specified arguments on the specified invocation.  Extracted to
    * provide indirection such that the arguments set on the invocation may be 
    * different from those originally passed in.
    * 
    * @param invocation
    * @param originalArguments
    */
   protected void addArgumentsToInvocation(MethodInvocation invocation, Object[] originalArguments)
   {
      invocation.setArguments(originalArguments);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected Interceptor[] getInterceptorChain()
   {
      return interceptorChain;
   }

   protected Object getTarget()
   {
      return this.target;
   }

}
