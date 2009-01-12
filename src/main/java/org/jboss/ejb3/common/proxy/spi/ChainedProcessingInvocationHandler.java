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
import java.lang.reflect.Method;

/**
 * ChainedProcessingInvocationHandler
 * 
 * A Chain of Processors which may be invoked in
 * succession.  At the end of the chain is an underlying 
 * delegate instance to be invoked via reflection.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ChainedProcessingInvocationHandler implements InvocationHandler
{
   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The underlying delegate to be invoked when the chain has exhausted
    */
   private Object delegate;

   /**
    * A Chain of Processors
    */
   private ChainableProcessor[] processorChain;

   /**
    * Internal counter for the next handler to be invoked
    */
   private int nextHandlerIndex = 0;

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public ChainedProcessingInvocationHandler(Object delegate, ChainableProcessor processor)
   {
      this(delegate, new ChainableProcessor[]
      {processor});
   }

   public ChainedProcessingInvocationHandler(Object delegate, ChainableProcessor[] handlerChain)
   {
      // Precondition check
      assert delegate != null : "Requiste delegate was not supplied";

      // Set specified properties
      this.setDelegate(delegate);
      this.setHandlerChain(handlerChain);
   }

   // ------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Invokes the next processor in the chain with the 
    * specified arguments.  In the event we've reached the end of the chain,
    * the underlying delegate will be invoked via reflection
    * 
    * @param proxy
    * @param method
    * @param args
    * @exception Throwable
    * @return
    */
   public Object invokeNext(Object proxy, Method method, Object[] args) throws Throwable
   {
      // Initialize
      Object returnValue = null;

      // If no more handlers in the chain
      if (this.getHandlerChain().length <= this.getNextHandlerIndex())
      {
         // Get the delegate
         Object delegate = this.getDelegate();

         // Ensure the delegate is supplied
         assert delegate != null : "Requiste delegate was not supplied";

         assert method.getDeclaringClass().isAssignableFrom(delegate.getClass());

         // Reset the chain counter so we can invoke again
         this.reset();

         // Use reflection to pass the invocation to the delegate
         return method.invoke(delegate, args);

      }
      // More handlers are present in the chain
      else
      {
         // Invoke upon the next handler in the chain
         // FIXME: This is just a stop-gap solution for the broken ChainedProcessingInvocationHandler construct
         int currentHandlerIndex = this.nextHandlerIndex;
         this.nextHandlerIndex++;
         returnValue = this.getHandlerChain()[currentHandlerIndex].invoke(this, proxy, method, args);
      }

      // Return
      return returnValue;
   }
   
   /**
    * Resets the internal counter for the next processor in the chain
    */
   public void reset()
   {
      this.nextHandlerIndex = 0;
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Provides a base invocation mechanism under which the request
    * is passed along to the delegate instance
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // Start the chain
      return this.invokeNext(proxy, method, args);
   }

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public Object getDelegate()
   {
      return delegate;
   }

   protected void setDelegate(Object delegate)
   {
      this.delegate = delegate;
   }

   protected ChainableProcessor[] getHandlerChain()
   {
      return processorChain == null ? new ChainableProcessor[]
      {} : processorChain;
   }

   protected void setHandlerChain(ChainableProcessor[] handlerChain)
   {
      this.processorChain = handlerChain;
   }

   protected int getNextHandlerIndex()
   {
      return nextHandlerIndex;
   }

}
