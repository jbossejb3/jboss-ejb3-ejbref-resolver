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
package org.jboss.ejb3.common.kernel;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ScopeInfo;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * The default handle factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DefaultHandleFactory implements HandleFactory
{
   public static final DefaultHandleFactory INSTANCE = new DefaultHandleFactory();

   private DefaultHandleFactory()
   {
   }

   /**
    * Get the instance.
    *
    * @return the singleton instance
    */
   public static HandleFactory getInstance()
   {
      return INSTANCE;
   }

   public Handle createHandle(ControllerContext context)
   {
      return createHandle(context, context.getName());
   }

   public Handle createHandle(ControllerContext context, Object alias)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");
      if (alias == null)
         throw new IllegalArgumentException("Null alias");

      ScopeInfo scopeInfo = context.getScopeInfo();
      ScopeKey scopeKey = scopeInfo.getInstallScope();
      if (scopeKey != null)
         return new ScopeKeyHandle(scopeKey, alias);

      return new UniqueNameHandle(alias);
   }
}