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

import org.jboss.ejb3.common.proxy.spi.ChainableProcessor;
import org.jboss.ejb3.common.proxy.spi.ChainedProcessingInvocationHandler;

/**
 * ChangeInputProcessor
 * 
 * A test ChainableProcessor which ignores the 
 * specified input and replaces it with that specified
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ChangeInputProcessor implements ChainableProcessor
{
   /**
    * Backing arguments to use
    */
   private int[] args;

   public ChangeInputProcessor(int[] args)
   {
      this.args = args;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.intf.ChainableInvocationHandler#invoke(org.jboss.ejb3.proxy.handler.ChainInvocationHandler, java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke(ChainedProcessingInvocationHandler chain, Object proxy, Method method, Object[] args) throws Throwable
   {
      // Define new arguments
      args = new Object[]
      {this.args};

      // Send along to the rest of the chain, passing overridden argument
      return chain.invokeNext(proxy, method, args);
   }

}
