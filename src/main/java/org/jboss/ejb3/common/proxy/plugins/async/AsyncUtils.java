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

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.Future;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.proxy.spi.InterceptorChainInvocationHandler;
import org.jboss.ejb3.common.proxy.spi.ProxyUtils;
import org.jboss.logging.Logger;

/**
 * AsyncUtils
 * 
 * Common Utility methods for use with the Async Proxies
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsyncUtils
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(AsyncUtils.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * In place to enforce noninstantiability
    */
   private AsyncUtils()
   {
   }

   // --------------------------------------------------------------------------------||
   // Utility Methods ----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Future result from the specified proxy, which 
    * must implement AsyncProvider
    */
   public static Future<?> getFutureResult(final Object proxy)
   {
      // Ensure we're given an asyncable proxy
      assert proxy instanceof AsyncProvider : "Specified proxy " + proxy + " was not an instance of "
            + AsyncProvider.class.getName();

      // Get the provider
      final AsyncProvider provider = (AsyncProvider) proxy;

      // Get the future result
      final Future<?> futureResult = provider.getFutureResult();

      // Return
      return futureResult;

   }

   /**
    * Makes the specified delegate object invoked as async, tacking on support to
    * obtain the async result
    */
   public static <T> T mixinAsync(final T delegate)
   {
      // Define async interfaces to add
      final Class<?>[] asyncInterfaces = new Class<?>[]
      {AsyncProvider.class};

      // Define interceptors to use in the chain
      final Interceptor[] interceptorChain = new Interceptor[]
      {new AsyncInterceptor()};

      // Create a Proxy Handler
      final InvocationHandler handler = new InterceptorChainInvocationHandler(interceptorChain, delegate);

      // Make the Proxy
      final T mixin = ProxyUtils.mixinProxy(delegate, asyncInterfaces, handler, delegate);

      // Return
      return mixin;
   }
}
