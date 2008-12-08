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

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.graph.GraphController;
import org.jboss.dependency.spi.graph.SearchInfo;
import org.jboss.kernel.Kernel;

/**
 * Abstract handle.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractHandle implements Handle
{
   private Object name;
   private ControllerState state;

   protected AbstractHandle(Object name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      this.name = name;
   }

   public Object getBean(Kernel kernel) throws Throwable
   {
      if (kernel == null)
         throw new IllegalArgumentException("Null kernel");

      Controller controller = kernel.getController();
      if (controller instanceof GraphController == false)
         throw new IllegalArgumentException("Controller is not GraphController instance: " + controller);

      GraphController gc = GraphController.class.cast(controller);
      return gc.getContext(name, state, getSearchInfo());
   }

   /**
    * Get the search info.
    *
    * @return the search info
    */
   protected abstract SearchInfo getSearchInfo();

   /**
    * Set the bean expected state.
    *
    * @param state the bean expected state
    */
   public void setState(ControllerState state)
   {
      this.state = state;
   }
}
