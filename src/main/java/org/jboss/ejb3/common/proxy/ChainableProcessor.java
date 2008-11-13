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
package org.jboss.ejb3.common.proxy;

import java.lang.reflect.Method;


/**
 * ChainableInvocationHandler
 * 
 * An InvocationHandler that is chain-aware.  May perform
 * its own processing before, after, or ignoring the rest of the 
 * InvocationHandlers in the chain of which it is a part
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface ChainableProcessor
{
   /**
    * Invokes this handler with the specified arguments.  Processing
    * may be performed before or after the rest of the chain depending 
    * upon when "chain.invokeNext()" is executed.
    * 
    * @param chain
    * @param proxy
    * @param method
    * @param args
    * @exception Throwable
    * @return
    */
   Object invoke(ChainedProcessingInvocationHandler chain, Object proxy, Method method, Object[] args) throws Throwable;
}
