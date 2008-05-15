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
package org.jboss.ejb3.test.common.string;

import junit.framework.TestCase;

import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * AdjustWhiteSpaceStringToNullTestCase
 * 
 * A Test Case to ensure that adjusting a whitespace String
 * to null works as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AdjustWhiteSpaceStringToNullTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger logger = Logger.getLogger(AdjustWhiteSpaceStringToNullTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test that a null String is not adjusted, and returns as null
    */
   @Test
   public void testNullStringReturnsNull()
   {
      String test = null;
      String result = StringUtils.adjustWhitespaceStringToNull(test);
      TestCase.assertNull("Adjusted null argument should return as null", result);
   }

   /**
    * Test that a String with no whitespace returns as equal by value
    * to what was passed in
    */
   @Test
   public void testNoWhitespaceStringReturnsSame()
   {
      String test = "test";
      String result = StringUtils.adjustWhitespaceStringToNull(test);
      TestCase
            .assertEquals("Adjusted String is not equal by value to input of String with no whitespace", test, result);
   }

   /**
    * Tests that a String containing only whitespace returns as null
    */
   @Test
   public void testWhitespaceStringReturnsNull()
   {
      String test = "  ";
      String result = StringUtils.adjustWhitespaceStringToNull(test);
      TestCase.assertNull("Whitespace String should be adjusted to null", result);

   }

   /**
    * Tests that a character string with whitespace returns as equal by value 
    * to what was passed in
    */
   @Test
   public void testWhitespaceWithinCharacterStringReturnsSame()
   {
      String test = "test  ";
      String result = StringUtils.adjustWhitespaceStringToNull(test);
      TestCase
            .assertEquals(
                  "Character string with valid characters and whitespace should return equal by value to input", test,
                  result);
   }

}
