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

import org.jboss.dependency.plugins.graph.ScopeKeySearchInfo;
import org.jboss.dependency.spi.graph.SearchInfo;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * Scope key handle.
 * It looks up the bean via ScopeKey and non-unique bean name.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ScopeKeyHandle extends AbstractHandle
{
   private ScopeKey scopeKey;
   private transient SearchInfo searchInfo;

   public ScopeKeyHandle(ScopeKey scopeKey, Object name)
   {
      super(name);
      if (scopeKey == null)
         throw new IllegalArgumentException("Null scope key");

      this.scopeKey = scopeKey;
   }

   protected SearchInfo getSearchInfo()
   {
      if (searchInfo == null)
         searchInfo = new ScopeKeySearchInfo(scopeKey);

      return searchInfo;
   }
}