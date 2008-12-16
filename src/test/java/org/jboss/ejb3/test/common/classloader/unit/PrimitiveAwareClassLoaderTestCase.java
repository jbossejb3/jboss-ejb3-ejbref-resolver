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
package org.jboss.ejb3.test.common.classloader.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.ejb3.common.classloader.PrimitiveAwareClassLoader;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * PrimitiveAwareClassLoaderTestCase
 * 
 * Test case for {@link PrimitiveAwareClassLoader}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PrimitiveAwareClassLoaderTestCase
{

   private static final Map<String, Class<?>> nameToClassMapping = new HashMap<String, Class<?>>();

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(PrimitiveAwareClassLoaderTestCase.class);

   /**
    * The instance of the classloader which we will be testing
    */
   private PrimitiveAwareClassLoader primitiveAwareClassLoader;

   @BeforeClass
   public static void beforeClass()
   {
      nameToClassMapping.put("void", void.class);
      nameToClassMapping.put("boolean", boolean.class);
      nameToClassMapping.put("int", int.class);
      nameToClassMapping.put("long", long.class);
      nameToClassMapping.put("float", float.class);
      nameToClassMapping.put("double", double.class);
      nameToClassMapping.put("char", char.class);
      nameToClassMapping.put("byte", byte.class);
      nameToClassMapping.put("short", short.class);
   }

   @Before
   public void before()
   {

      this.primitiveAwareClassLoader = new PrimitiveAwareClassLoader(Thread.currentThread().getContextClassLoader());
   }

   /**
    * Test that {@link Class} corresponding to the primitives are 
    * returned correctly through the {@link PrimitiveAwareClassLoader#loadClass(String)}
    *  
    * @throws Throwable
    */
   @Test
   public void testPrimitiveLoadThroughLoadClass() throws Throwable
   {
      // load all type of primitives (9 in all)
      Iterator<Map.Entry<String, Class<?>>> primitives = nameToClassMapping.entrySet().iterator();
      while (primitives.hasNext())
      {
         Map.Entry<String, Class<?>> primitive = primitives.next();
         // test our classloader
         Class<?> klass = primitiveAwareClassLoader.loadClass(primitive.getKey());
         // ensure that it returned the right class
         assertNotNull("loadClass from " + PrimitiveAwareClassLoader.class.getName() + " returned null", klass);
         assertEquals("loadClass returned " + klass + " for " + primitive.getKey(), klass, primitive.getValue());
      }

   }

   /**
    * Test that the correct {@link Class} is returned for a primitive when
    * {@link Class#forName(String, boolean, ClassLoader)} is used.
    * 
    * @throws Throwable
    */
   @Test
   public void testPrimitiveLoadThroughForName() throws Throwable
   {

      // load non-primitives through Class.forName()
      // There's no support for loading the primitives through Class.forName 
      // using the PrimitiveAwareClassLoader (see EJBTHREE-1626 for comments). 
      // So no need to test that.

      Class<?> klass = Class.forName("java.lang.Double", false, primitiveAwareClassLoader);
      // ensure that it returned the right class
      assertNotNull("Class.forName returned null with " + primitiveAwareClassLoader.getClass().getName(), klass);
      assertEquals("Class.forName returned " + klass + " for java.lang.Double", java.lang.Double.class, klass);

   }

   /**
    * Test that the non-primitive classes are loaded as usual when the
    * {@link PrimitiveAwareClassLoader} is used.
    * 
    * @throws Throwable
    */
   @Test
   public void testOtherClassLoad() throws Throwable
   {
      // load Integer
      Class<?> integerClass = this.primitiveAwareClassLoader.loadClass(Integer.class.getName());
      assertNotNull("Integer class was not loaded. loadClass returned null", integerClass);
      assertEquals("Incorrect class loaded for " + Integer.class.getName(), integerClass, Integer.class);

      // load Double
      Class<?> doubleClass = this.primitiveAwareClassLoader.loadClass(Double.class.getName());
      assertNotNull("Double class was not loaded. loadClass returned null", doubleClass);
      assertEquals("Incorrect class loaded for " + Double.class.getName(), doubleClass, Double.class);

      // load Float
      Class<?> floatClass = this.primitiveAwareClassLoader.loadClass(Float.class.getName());
      assertNotNull("Float class was not loaded. loadClass returned null", floatClass);
      assertEquals("Incorrect class loaded for " + Float.class.getName(), floatClass, Float.class);

      // load Byte
      Class<?> byteClass = this.primitiveAwareClassLoader.loadClass(Byte.class.getName());
      assertNotNull("Byte class was not loaded. loadClass returned null", byteClass);
      assertEquals("Incorrect class loaded for " + Byte.class.getName(), byteClass, Byte.class);

      // load Character
      Class<?> characterClass = this.primitiveAwareClassLoader.loadClass(Character.class.getName());
      assertNotNull("Character class was not loaded. loadClass returned null", characterClass);
      assertEquals("Incorrect class loaded for " + Character.class.getName(), characterClass, Character.class);

      // load String
      Class<?> stringClass = this.primitiveAwareClassLoader.loadClass(String.class.getName());
      assertNotNull("String class was not loaded. loadClass returned null", stringClass);
      assertEquals("Incorrect class loaded for " + String.class.getName(), stringClass, String.class);

      // lets try one from outside the java.lang package
      Class<?> bigDecimalClass = this.primitiveAwareClassLoader.loadClass(java.math.BigDecimal.class.getName());
      assertNotNull("BigDecimal class was not loaded. loadClass returned null", bigDecimalClass);
      assertEquals("Incorrect class loaded for " + BigDecimal.class.getName(), bigDecimalClass, BigDecimal.class);

      // lets try loading this testcase
      Class<?> thisTestCaseClass = this.primitiveAwareClassLoader.loadClass(this.getClass().getName());
      assertNotNull(this.getClass().getName() + " class was not loaded. loadClass returned null", thisTestCaseClass);
      assertEquals("Incorrect class loaded for " + this.getClass().getName(), thisTestCaseClass, this.getClass());

   }

   /**
    * Test that arrays are loaded correctly.
    * 
    * @throws Throwable
    */
   @Test
   public void testLoadArray() throws Throwable
   {

      // lets load an primitive array
      Class<?> primitiveClass = this.primitiveAwareClassLoader.loadClass(int[].class.getName());
      assertNotNull("int array was not loaded", primitiveClass);
      assertEquals("Incorrect class loaded for int array", int[].class, primitiveClass);
      logger.debug("Successfully loaded primitve array");

      // lets load an object array 
      Class<?> klass = this.primitiveAwareClassLoader.loadClass(String[].class.getName());
      assertNotNull("String array could not be loaded", klass);
      assertEquals("Incorrect class loaded for string array", String[].class, klass);
      logger.debug("Successfully loaded object array");

   }

   /**
    * Tests that the {@link PrimitiveAwareClassLoader} loads the primitives considering their
    * case. The classloader should throw a {@link ClassNotFoundException} if "Int" is being requested
    * for load instead of "int".
    * @throws Throwable
    */
   @Test
   public void testCaseSensitiveLoad() throws Throwable
   {
      // try with all type of primitives (9 in all)
      Iterator<Map.Entry<String, Class<?>>> primitives = nameToClassMapping.entrySet().iterator();
      while (primitives.hasNext())
      {
         Map.Entry<String, Class<?>> primitive = primitives.next();
         try
         {
            String upperCaseName = primitive.getKey().toUpperCase();
            // test our classloader
            Class<?> klass = primitiveAwareClassLoader.loadClass(upperCaseName);
            fail(PrimitiveAwareClassLoader.class.getName() + " is not case sensitive. Loaded " + klass + " for "
                  + upperCaseName);
         }
         catch (ClassNotFoundException cnfe)
         {
            // expected, so continue
            continue;
         }

      }

   }

}
