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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.jboss.ejb3.common.proxy.plugins.async.AsyncUtils;
import org.jboss.ejb3.test.common.proxy.Addable;
import org.jboss.ejb3.test.common.proxy.CalculatorServiceBean;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * AsyncTestCase
 *
 * Tests that the Async Proxy works as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsyncTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(AsyncTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that introducing an async mixin succeeds
    */
   @Test
   public void testAsync() throws Exception
   {
      // Initialize
      Addable calc = new CalculatorServiceBean();
      int[] args =
      {1, 2, 3};
      int expectedSum = 0;
      for (int arg : args)
      {
         expectedSum += arg;
      }

      // Make async
      Addable asyncCalc = AsyncUtils.mixinAsync(calc);

      // Make the async call
      asyncCalc.add(args);

      // Get the future result
      Future<?> futureResult = AsyncUtils.getFutureResult(asyncCalc);

      // Block until the call returns
      int result = (Integer) futureResult.get(2, TimeUnit.SECONDS);

      // Test
      TestCase.assertEquals("Async Proxy did not complete as expected", expectedSum, result);

   }

   @Test
   public void testException() throws Exception
   {
      Addable failingBean = new Addable() {
         public int add(int... args)
         {
            throw new RuntimeException("Failed predictably");
         }
      };
      
      Addable asyncFailingBean = AsyncUtils.mixinAsync(failingBean);
      
      asyncFailingBean.add(1, 2, 3);
      
      Future<?> futureResult = AsyncUtils.getFutureResult(asyncFailingBean);
      
      try
      {
         futureResult.get(2, TimeUnit.SECONDS);
      }
      catch(ExecutionException e)
      {
         Throwable cause = e.getCause();
         assertEquals(RuntimeException.class, cause.getClass());
         assertEquals("Failed predictably", cause.getMessage());
      }
   }
   
   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

}
