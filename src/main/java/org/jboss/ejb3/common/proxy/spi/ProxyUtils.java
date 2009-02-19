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
package org.jboss.ejb3.common.proxy.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * ProxyUtils
 * 
 * Common Utility methods for use with the Proxies
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyUtils
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ProxyUtils.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * In place to enforce noninstantiability
    */
   private ProxyUtils()
   {
   }

   // --------------------------------------------------------------------------------||
   // Utility Methods ----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Wraps the existing Proxy in a new Proxy to extend functionality, using 
    * the specified InvocationHandler
    * 
    * May be used to, at runtime, extend a service
    */
   public static Object mixinProxy(final Object delegate, final InvocationHandler handler)
   {
      return mixinProxy(delegate, null, handler);
   }

   /**
    * Wraps the existing Proxy in a new Proxy to extend functionality, adding 
    * support of the specified interfaces via the specified 
    * InvocationHandler
    * 
    * May be used to, at runtime, extend a service
    */
   public static Object mixinProxy(final Object delegate, final Class<?>[] additionalInterfaces,
         final InvocationHandler handler)
   {
      return mixinProxy(delegate, additionalInterfaces, handler, Object.class);
   }

   /**
    * Wraps the existing Proxy in a new Proxy to extend functionality, adding 
    * support of the specified interfaces via the specified 
    * InvocationHandler
    * 
    * May be used to, at runtime, extend a service
    */
   @SuppressWarnings("unchecked")
   public static <T> T mixinProxy(final Object delegate, final Class<?>[] additionalInterfaces,
         final InvocationHandler handler, final T expectedType)
   {
      // Initialize
      Set<Class<?>> newInterfaces = new HashSet<Class<?>>();
      Object newProxy = null;

      // Get the interfaces supported by the existing proxy
      Class<?>[] existingInterfaces = delegate.getClass().getInterfaces();

      // Add all existing interfaces to those we'll support in our wrapped Proxy
      for (Class<?> interfaze : existingInterfaces)
      {
         newInterfaces.add(interfaze);
      }

      // Add the new interfaces, if supplied
      if (additionalInterfaces != null)
      {
         for (Class<?> interfaze : additionalInterfaces)
         {
            newInterfaces.add(interfaze);
         }
      }

      // Make a new Proxy, using the Chain as the handler
      newProxy = Proxy.newProxyInstance(delegate.getClass().getClassLoader(), newInterfaces.toArray(new Class<?>[]
      {}), handler);

      // Return
      return (T) newProxy;
   }

}
