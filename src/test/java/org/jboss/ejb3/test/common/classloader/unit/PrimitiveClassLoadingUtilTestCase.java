/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.jboss.ejb3.common.classloader.util.PrimitiveClassLoadingUtil;
import org.jboss.ejb3.test.common.lang.MyClass;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * PrimitiveClassLoadingUtilTestCase
 * 
 * Tests the utility {@link PrimitiveClassLoadingUtil} for handling centralized
 * primitive types for classloading
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PrimitiveClassLoadingUtilTestCase
{

   private static final Map<String, Class<?>> nameToClassMapping = new HashMap<String, Class<?>>();

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(PrimitiveAwareClassLoaderTestCase.class);

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

   /**
    * Test that {@link Class} corresponding to the primitives are 
    * returned correctly through the {@link PrimitiveClassLoadingUtil#loadClass(String, ClassLoader)}
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
         Class<?> klass = PrimitiveClassLoadingUtil.loadClass(primitive.getKey(), Thread.currentThread()
               .getContextClassLoader());
         // ensure that it returned the right class
         assertNotNull("loadClass from " + PrimitiveClassLoadingUtil.class.getName() + " returned null", klass);
         assertEquals("loadClass returned " + klass + " for " + primitive.getKey(), klass, primitive.getValue());
      }

   }

   /**
    * Test that the non-primitive classes are loaded as usual when the
    * {@link PrimitiveClassLoadingUtil} is used.
    * 
    * @throws Throwable
    */
   @Test
   public void testOtherClassLoad() throws Throwable
   {
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      // load Integer
      Class<?> integerClass = PrimitiveClassLoadingUtil.loadClass(Integer.class.getName(), tccl);
      assertNotNull("Integer class was not loaded. loadClass returned null", integerClass);
      assertEquals("Incorrect class loaded for " + Integer.class.getName(), integerClass, Integer.class);

      // load Double
      Class<?> doubleClass = PrimitiveClassLoadingUtil.loadClass(Double.class.getName(), tccl);
      assertNotNull("Double class was not loaded. loadClass returned null", doubleClass);
      assertEquals("Incorrect class loaded for " + Double.class.getName(), doubleClass, Double.class);

      // load Float
      Class<?> floatClass = PrimitiveClassLoadingUtil.loadClass(Float.class.getName(), tccl);
      assertNotNull("Float class was not loaded. loadClass returned null", floatClass);
      assertEquals("Incorrect class loaded for " + Float.class.getName(), floatClass, Float.class);

      // load Byte
      Class<?> byteClass = PrimitiveClassLoadingUtil.loadClass(Byte.class.getName(), tccl);
      assertNotNull("Byte class was not loaded. loadClass returned null", byteClass);
      assertEquals("Incorrect class loaded for " + Byte.class.getName(), byteClass, Byte.class);

      // load Character
      Class<?> characterClass = PrimitiveClassLoadingUtil.loadClass(Character.class.getName(), tccl);
      assertNotNull("Character class was not loaded. loadClass returned null", characterClass);
      assertEquals("Incorrect class loaded for " + Character.class.getName(), characterClass, Character.class);

      // load String
      Class<?> stringClass = PrimitiveClassLoadingUtil.loadClass(String.class.getName(), tccl);
      assertNotNull("String class was not loaded. loadClass returned null", stringClass);
      assertEquals("Incorrect class loaded for " + String.class.getName(), stringClass, String.class);

      // lets try one from outside the java.lang package
      Class<?> bigDecimalClass = PrimitiveClassLoadingUtil.loadClass(java.math.BigDecimal.class.getName(), tccl);
      assertNotNull("BigDecimal class was not loaded. loadClass returned null", bigDecimalClass);
      assertEquals("Incorrect class loaded for " + BigDecimal.class.getName(), bigDecimalClass, BigDecimal.class);

      // lets try loading this testcase
      Class<?> thisTestCaseClass = PrimitiveClassLoadingUtil.loadClass(this.getClass().getName(), tccl);
      assertNotNull(this.getClass().getName() + " class was not loaded. loadClass returned null", thisTestCaseClass);
      assertEquals("Incorrect class loaded for " + this.getClass().getName(), thisTestCaseClass, this.getClass());
      
      // let's try some other custom classes
      Class<?> myClass = PrimitiveClassLoadingUtil.loadClass(MyClass.class.getName(), tccl);
      assertNotNull(MyClass.class.getName() + " class was not loaded. loadClass returned null", myClass);
      assertEquals("Incorrect class loaded for " + MyClass.class.getName(), myClass, MyClass.class);
      
      // try with an interface
      Class<?> map = PrimitiveClassLoadingUtil.loadClass(Map.class.getName(), tccl);
      assertNotNull(Map.class.getName() + " class was not loaded. loadClass returned null", map);
      assertEquals("Incorrect class loaded for " + Map.class.getName(), map, Map.class);

   }

   /**
    * Test that arrays are loaded correctly through {@link PrimitiveClassLoadingUtil}
    * 
    * @throws Throwable
    */
   @Test
   public void testLoadArray() throws Throwable
   {

      ClassLoader tccl = Thread.currentThread().getContextClassLoader();

      // lets load an primitive array
      Class<?> primitiveClass = PrimitiveClassLoadingUtil.loadClass(int[].class.getName(), tccl);
      assertNotNull("int array was not loaded", primitiveClass);
      assertEquals("Incorrect class loaded for int array", int[].class, primitiveClass);
      logger.debug("Successfully loaded primitve array");

      // lets load an object array 
      Class<?> klass = PrimitiveClassLoadingUtil.loadClass(String[].class.getName(), tccl);
      assertNotNull("String array could not be loaded", klass);
      assertEquals("Incorrect class loaded for string array", String[].class, klass);
      logger.debug("Successfully loaded object array");

   }

   /**
    * Tests that the {@link PrimitiveClassLoadingUtil} loads the primitives considering their
    * case. The PrimitiveClassLoadingUtil should throw a {@link ClassNotFoundException} if "Int" is being requested
    * for load instead of "int".
    * @throws Throwable
    */
   @Test
   public void testCaseSensitiveLoad() throws Throwable
   {
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();

      // try with all type of primitives (9 in all)
      Iterator<Map.Entry<String, Class<?>>> primitives = nameToClassMapping.entrySet().iterator();
      while (primitives.hasNext())
      {
         Map.Entry<String, Class<?>> primitive = primitives.next();
         try
         {
            String upperCaseName = primitive.getKey().toUpperCase();
            // test our classloader
            Class<?> klass = PrimitiveClassLoadingUtil.loadClass(upperCaseName, tccl);
            fail(PrimitiveClassLoadingUtil.class.getName() + " is not case sensitive. Loaded " + klass + " for "
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
