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
package org.jboss.ejb3.test.common.proxy.unit;

import junit.framework.TestCase;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.proxy.spi.InterceptorChainInvocationHandler;
import org.jboss.ejb3.common.proxy.spi.ProxyUtils;
import org.jboss.ejb3.test.common.proxy.AddOneInterceptor;
import org.jboss.ejb3.test.common.proxy.Addable;
import org.jboss.ejb3.test.common.proxy.CalculatorServiceBean;
import org.jboss.ejb3.test.common.proxy.ChangeInputInterceptor;
import org.jboss.ejb3.test.common.proxy.Multipliable;
import org.jboss.ejb3.test.common.proxy.MultiplyMixinInterceptor;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * InvocationHandlerChainTestCase
 *
 * Tests 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class InvocationHandlerChainTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(InvocationHandlerChainTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The control for this test; passes along a simple invocation
    * to the calculator service and checks the result as expected
    */
   @Test
   public void testCalculatorServiceControl() throws Exception
   {
      // Initialize
      Addable calc = new CalculatorServiceBean();
      int[] args =
      {1, 2, 3};

      // Get the result from the service
      int result = calc.add(args);

      // Calculate the expected result
      int expected = this.add(args);

      // Test
      TestCase.assertEquals("Control test for the CalculatorService failed", expected, result);

   }

   /**
    * Tests that introducing an invocation handler to add 1
    * to the result of the calculator service succeeds as expected
    */
   @Test
   public void testCalculatorServiceInChain() throws Exception
   {
      // Initialize
      Addable calc = new CalculatorServiceBean();
      int[] args =
      {1, 2, 3};

      // Define the chain
      Interceptor[] interceptorChain = new Interceptor[]
      {new AddOneInterceptor()};

      // Make the handler
      InterceptorChainInvocationHandler handler = new InterceptorChainInvocationHandler(interceptorChain, calc);

      // Apply the chain
      Addable newCalc = (Addable) ProxyUtils.mixinProxy(calc, handler);

      // Get the result from the service
      int result = newCalc.add(args);

      // Calculate the expected result (adding all, plus 1)
      int expected = this.add(args) + 1;

      // Test
      TestCase.assertEquals("Chain Invocation Handler did not work as expected", expected, result);

   }

   /**
    * Tests that invocation upon a chain more than once succeeds
    * (ie. that once the delegate is reached at end of chain, the
    * internal counter is reset)
    */
   @Test
   public void testChainInvokableMoreThanOnce() throws Exception
   {
      // Initialize
      Addable calc = new CalculatorServiceBean();
      int[] args =
      {1, 2, 3};

      // Define the chain
      Interceptor[] interceptorChain = new Interceptor[]
      {new AddOneInterceptor()};

      // Make the handler
      InterceptorChainInvocationHandler handler = new InterceptorChainInvocationHandler(interceptorChain, calc);

      // Apply the chain
      Addable newCalc = (Addable) ProxyUtils.mixinProxy(calc, handler);

      // Get the result from the service
      int result1 = newCalc.add(args);
      // Invoke again
      int result2 = newCalc.add(args);

      // Calculate the expected result (adding all, plus 1)
      int expected = this.add(args) + 1;

      // Test
      TestCase.assertEquals("Chain Invocation Handler did not work as expected", expected, result1);
      TestCase.assertEquals("Second invocation upon the chain fails", expected, result2);

   }

   /**
    * Tests that introducing more than one invocation handler in a chain
    * succeeds as expected
    */
   @Test
   public void testCalculatorServiceInMultiHandlerChain() throws Exception
   {
      // Initialize
      Addable calc = new CalculatorServiceBean();
      int[] args =
      {1, 2, 3};
      int[] overrideArgs =
      {5, 10};

      // Define the chain
      Interceptor[] interceptorChain = new Interceptor[]
      {new ChangeInputInterceptor(overrideArgs), new AddOneInterceptor()};

      // Make the handler
      InterceptorChainInvocationHandler handler = new InterceptorChainInvocationHandler(interceptorChain, calc);

      // Mix it up
      Addable newCalc = (Addable) ProxyUtils.mixinProxy(calc, handler);

      // Get the result from the service
      int result = newCalc.add(args);

      // Calculate the expected result (overriden arguments sum, plus 1)
      int expected = this.add(overrideArgs) + 1;

      // Test
      TestCase.assertEquals("Chain Invocation Handler in multi-processor chain did not work as expected", expected,
            result);

   }

   /**
    * Tests that a mixin-like introduction succeeds 
    */
   @Test
   public void testCalculatorServiceAddingMixin() throws Exception
   {
      // Initialize
      Addable calc = new CalculatorServiceBean();
      int[] args =
      {4, 7, 2};

      // Define the chain
      Interceptor[] interceptorChain = new Interceptor[]
      {new MultiplyMixinInterceptor()};

      // Make the handler
      InterceptorChainInvocationHandler handler = new InterceptorChainInvocationHandler(interceptorChain, calc);

      // Mix it up
      Multipliable newCalc = (Multipliable) ProxyUtils.mixinProxy(calc, new Class<?>[]
      {Multipliable.class}, handler);

      // Get the result from the service
      int result = newCalc.multiply(args);

      // Calculate the expected result (product of arguments)
      int expected = this.multiply(args);

      // Test
      TestCase.assertEquals("Chain Invocation Handler did not work as expected", expected, result);
      log.info("Arguments " + args + " multiplied got expected result: " + result);

   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Adds all arguments
    */
   protected int add(int... args)
   {
      // Initialize
      int returnValue = 0;

      // Add all arguments
      for (int arg : args)
      {
         returnValue += arg;
      }

      // Return
      return returnValue;
   }

   /**
    * Multiplies all arguments
    */
   protected int multiply(int... args)
   {
      // Initialize
      int returnValue = 1;

      // Add all arguments
      for (int arg : args)
      {
         returnValue *= arg;
      }

      // Return
      return returnValue;
   }

}
