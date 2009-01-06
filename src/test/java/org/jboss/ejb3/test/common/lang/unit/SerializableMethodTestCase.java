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
package org.jboss.ejb3.test.common.lang.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.test.common.lang.MyChildClass;
import org.jboss.ejb3.test.common.lang.MyClass;
import org.jboss.ejb3.test.common.lang.SerializationUtil;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * 
 * SerializableMethodTestCase for testing {@link SerializableMethod}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 * 
 * @see {@link SerializableMethod}
 */
public class SerializableMethodTestCase
{

   /**
    * Instance of logger
    */
   private static Logger logger = Logger.getLogger(SerializableMethodTestCase.class);

   /**
    * 
    */
   private static MyClass myClass = new MyClass();

   /**
    * Test the {@link SerializableMethod#equals(Object)} and {@link SerializableMethod#hashCode()}
    * with simple method which accepts no parameter and void return type 
    * 
    * @throws Throwable
    */
   @Test
   public void testEqualsAndHashCodeForMethodWithNoParam() throws Throwable
   {

      // Test that the SerializableMethod instances, created
      // for the same method, are equal and have same hashCode.
      // Intention of this test is to ensure that the methods accepting no parameter are handled correctly.

      Method methodWithNoParamAndReturningVoid = myClass.getClass().getMethod("methodWithNoParamAndReturningVoid",
            (Class<?>[]) null);
      SerializableMethod serializableMethod = new SerializableMethod(methodWithNoParamAndReturningVoid, myClass
            .getClass());
      SerializableMethod anotherSerializableMethod = new SerializableMethod(methodWithNoParamAndReturningVoid, myClass
            .getClass());

      // These 2 SerializableMethod instances should be equal, as they were created for the same Method
      assertTrue("Failure - Two SerializableMethod instances created out of the same Method are not equal",
            serializableMethod.equals(anotherSerializableMethod));
      // test hashCode
      assertEquals(
            "Failure - hashCode does not match for the two SerializableMethod instances created out of the same Method",
            serializableMethod.hashCode(), anotherSerializableMethod.hashCode());

   }

   /**
    * Test the {@link SerializableMethod#equals(Object)} and {@link SerializableMethod#hashCode()}
    * for methods which accept parameters
    * 
    * @throws Throwable
    */
   @Test
   public void testEqualsAndHashCodeForMethodsWithParam() throws Throwable
   {

      // Test that the SerializableMethod instances, created
      // for the same method which accepts a array of primitives, are equal and have same hashCode.
      // Intention of this test is to ensure that the methods accepting array of primitives param are handled correctly.

      Method methodAcceptingArrayOfPrimitives = myClass.getClass().getMethod("methodAcceptingArrayOfPrimitives",
            new Class[]
            {int[].class});
      SerializableMethod serializableMethod_arrayOfPrimitives = new SerializableMethod(
            methodAcceptingArrayOfPrimitives, myClass.getClass());
      SerializableMethod anotherSerializableMethod_arrayOfPrimitives = new SerializableMethod(
            methodAcceptingArrayOfPrimitives, myClass.getClass());

      // test equals
      assertTrue(
            "Failure - Two SerializableMethod instances created out of the same Method(accepting primitive array parameter) are not equal",
            serializableMethod_arrayOfPrimitives.equals(anotherSerializableMethod_arrayOfPrimitives));
      // test hashCode
      assertEquals(
            "Failure - hashCode does not match for instances created out of the same Method(accepting primitive array parameter)",
            serializableMethod_arrayOfPrimitives.hashCode(), anotherSerializableMethod_arrayOfPrimitives.hashCode());

      // Test that the SerializableMethod instances, created
      // for the same method which accepts a array of Object, are equal and have same hashCode.
      // Intention of this test is to ensure that methods accepting array of Object types are handled correctly. 

      Method methodAcceptingArrayOfObjects = myClass.getClass().getMethod("methodAcceptingArrayOfObjects", new Class[]
      {Object[].class});
      SerializableMethod serializableMethod_arrayOfObjects = new SerializableMethod(methodAcceptingArrayOfObjects,
            myClass.getClass());
      SerializableMethod anotherSerializableMethod_arrayOfObjects = new SerializableMethod(
            methodAcceptingArrayOfObjects, myClass.getClass());

      // test equals
      assertTrue(
            "Failure - Two SerializableMethod instances created out of the same Method(accepting Object array parameter) are not equal",
            serializableMethod_arrayOfObjects.equals(anotherSerializableMethod_arrayOfObjects));
      // test hashCode
      assertEquals(
            "Failure - hashCode does not match for instances created out of the same Method(accepting Object array parameter)",
            serializableMethod_arrayOfObjects.hashCode(), anotherSerializableMethod_arrayOfObjects.hashCode());

   }

   /**
    * Test the {@link SerializableMethod#equals(Object)} 
    * for methods which are overloaded
    * 
    * @throws Throwable
    */
   @Test
   public void testEqualsForOverloadedMethod() throws Throwable
   {

      // Test that the SerializableMethod instances created
      // for overloaded methods (one with String param and one with Integer param) are NOT equal
      // Intention of this test is to ensure that two overloaded methods are NOT equal

      Method methodAcceptingStringParamAndReturingVoid = myClass.getClass().getMethod(
            "methodWithParamAndReturningVoid", new Class[]
            {String.class});
      Method methodAcceptingIntegerParamAndReturingVoid = myClass.getClass().getMethod(
            "methodWithParamAndReturningVoid", new Class[]
            {Integer.class});

      SerializableMethod serializableMethod = new SerializableMethod(methodAcceptingStringParamAndReturingVoid, myClass
            .getClass());
      SerializableMethod anotherSerializableMethod = new SerializableMethod(methodAcceptingIntegerParamAndReturingVoid,
            myClass.getClass());

      // test the equals
      assertFalse("Failure - Two Serializable method instances created for 2 different overloaded methods are equal",
            serializableMethod.equals(anotherSerializableMethod));

      // Test that the SerializableMethod instances created
      // for overloaded methods (one with primitive param and one with Object param) are NOT equal
      // Intention of this test is to ensure that two overloaded methods are NOT equal

      Method methodAcceptingPrimitiveIntAndReturningVoid = myClass.getClass().getMethod(
            "methodWithParamAndReturningVoid", new Class[]
            {int.class});

      SerializableMethod serializableMethod_PrimitiveIntParam = new SerializableMethod(
            methodAcceptingPrimitiveIntAndReturningVoid, myClass.getClass());
      SerializableMethod serializableMethod_IntegerParam = new SerializableMethod(
            methodAcceptingIntegerParamAndReturingVoid, myClass.getClass());

      // test the equals
      assertFalse(
            "Failure - Two SerializableMethod instances one with primitive int param and one with Integer param are equal",
            serializableMethod_PrimitiveIntParam.equals(serializableMethod_IntegerParam));

      // Test that the SerializableMethod instances created
      // for overloaded methods (one with Integer and one with base class Object param) are NOT equal
      // Intention of this test is to ensure that two overloaded methods are NOT equal

      Method methodAcceptingObject = myClass.getClass().getMethod("methodWithParamAndReturningVoid", new Class[]
      {Object.class});
      SerializableMethod serializableMethod_ObjectParam = new SerializableMethod(methodAcceptingObject, myClass
            .getClass());

      // test the equals
      assertFalse(
            "Failure - Two SerializableMethod instances one with Integer param and one with Object param are equal",
            serializableMethod_IntegerParam.equals(serializableMethod_ObjectParam));

   }

   /**
    * Test the {@link SerializableMethod#equals(Object)} 
    * for method which have the same name, parameters but belong to different classes
    * 
    * @throws Throwable
    */
   @Test
   public void testEqualsForSameMethodBelongingToDifferentClass() throws Throwable
   {

      // Test that the SerializableMethod instances created
      // for a method with same name, param and return type, but belonging to different classes are NOT equal

      Method toStringMethodOfMyClass = myClass.getClass().getDeclaredMethod("toString", (Class<?>[]) null);
      Method toStringMethodOfObject = Object.class.getDeclaredMethod("toString", (Class<?>[]) null);

      SerializableMethod serializableMethod_toStringForMyClass = new SerializableMethod(toStringMethodOfMyClass,
            myClass.getClass());
      SerializableMethod serializableMethod_toStringForThisTestCase = new SerializableMethod(toStringMethodOfObject,
            Object.class);

      // test the equals 
      assertFalse("Failure - Two SerializableMethod instances for same methods from two different classes are equal",
            serializableMethod_toStringForMyClass.equals(serializableMethod_toStringForThisTestCase));

   }

   /**
    * Test the {@link SerializableMethod#equals(Object)} and {@link SerializableMethod#hashCode()}
    * on two instances of {@link SerializableMethod}, one of which is a copy created by serialization/de-serialization 
    * 
    * @throws Throwable
    * @see {@link SerializationUtil}
    */
   @Test
   public void testEqualsAndHashCodeWithSerialization() throws Throwable
   {

      // Test that the serialized version of the SerializableMethod is equal to the original 
      // instance and has the same hashCode

      Method methodWithParam = myClass.getClass().getMethod("methodAcceptingArrayOfObjects", new Class[]
      {Object[].class});

      SerializableMethod serializableMethod = new SerializableMethod(methodWithParam, myClass.getClass());

      SerializableMethod copyOfSerializableMethod = (SerializableMethod) SerializationUtil.getCopy(serializableMethod);

      // test equals
      assertTrue("Failure - equals fails on serialized-deserialized instance and its original instance",
            serializableMethod.equals(copyOfSerializableMethod));

      // test hashCode
      assertEquals("Failure - Serialized-deserialized instance has a different hashCode",
            serializableMethod.hashCode(), copyOfSerializableMethod.hashCode());

      // One more similar test with a method accepting non-serializable param

      Method method = myClass.getClass().getMethod("methodAcceptingMyClass", new Class[]
      {MyClass.class});
      SerializableMethod serializableMethod_nonSerializableParam = new SerializableMethod(method, myClass.getClass());

      SerializableMethod copyOfSerializableMethod_nonSerilizableParam = (SerializableMethod) SerializationUtil
            .getCopy(serializableMethod_nonSerializableParam);

      // test equals
      assertTrue(
            "Failure - equals fails on serialized-deserialized instance and its original instance which contained non-serializable param",
            serializableMethod_nonSerializableParam.equals(copyOfSerializableMethod_nonSerilizableParam));

      // test hashCode
      assertEquals("Failure - hashCode does not match for serialized-deserialized instance and original instance",
            serializableMethod_nonSerializableParam.hashCode(), copyOfSerializableMethod_nonSerilizableParam.hashCode());

   }

   /**
    * 
    * Test the {@link SerializableMethod#equals(Object)} and {@link SerializableMethod#hashCode()}
    * with methods belonging to parent/child classes
    * 
    * @throws Throwable
    */
   @Test
   public void testEqualsAndHashCodeWithInheritedClasses() throws Throwable
   {

      // Test that the SerializableMethod instances created
      // for a method from the same base class are equal and have the same hashCode

      // Note that this testcase should NOT override the toString method, to ensure that the getMethod() returns
      // the 'Method' of Object.class

      Method toStringMethodOfThisClass = this.getClass().getMethod("toString", (Class<?>[]) null);
      Method toStringMethodOfObject = Object.class.getMethod("toString", (Class<?>[]) null);

      SerializableMethod serializableMethod_toStringOfThisClass = new SerializableMethod(toStringMethodOfThisClass,
            myClass.getClass());
      SerializableMethod serializableMethod_toStringOfObjectClass = new SerializableMethod(toStringMethodOfObject,
            Object.class);

      // test equals
      assertTrue(
            "Failure - Two SerializableMethod instances of method belonging to the same base class must not be equal",
            !serializableMethod_toStringOfObjectClass.equals(serializableMethod_toStringOfThisClass));

      // test hashCode
      assertTrue(
            "Failure - Two SerializableMethod instances of method belonging to the same base class must have different hashCode",
            serializableMethod_toStringOfObjectClass.hashCode() != serializableMethod_toStringOfThisClass.hashCode());

      // Test that the SerializableMethod instances created
      // for overridden methods are NOT equal

      Method methodFromChildClass = MyChildClass.class.getDeclaredMethod("methodWithParamAndReturningVoid", new Class[]
      {Integer.class});
      Method methodFromParentClass = MyClass.class.getDeclaredMethod("methodWithParamAndReturningVoid", new Class[]
      {Integer.class});

      SerializableMethod serializableMethodForChild = new SerializableMethod(methodFromChildClass, MyChildClass.class);
      SerializableMethod serializableMethodForParent = new SerializableMethod(methodFromParentClass, MyClass.class);

      // test equals
      assertFalse("Failure - The SerializableMethod instances of method from base class and child class are equal",
            serializableMethodForChild.equals(serializableMethodForParent));

   }

   /**
    * Test the {@link SerializableMethod#equals(Object)} and {@link SerializableMethod#hashCode()}
    * with methods involving <a href="http://java.sun.com/j2se/1.5.0/docs/guide/language/generics.html">generics</a>
    * 
    * @throws Throwable
    */
   @Test
   public void testEqualsAndHashCodeWithGenerics() throws Throwable
   {

      // Just a simple test to ensure the SerializableMethod works fine with methods
      // involving generics

      Method genericsMethod = myClass.getClass().getMethod("methodWithGenerics", new Class[]
      {List.class, int.class});
      Method anotherGenericMethod = myClass.getClass().getMethod("methodWithGenerics", new Class[]
      {List.class, int.class});

      SerializableMethod serializableMethod = new SerializableMethod(genericsMethod, myClass.getClass());
      SerializableMethod anotherSerializableMethod = new SerializableMethod(anotherGenericMethod, myClass.getClass());

      // test equals
      assertTrue("Failure - Two SerializableMethod instances for a method involving generics are not equal",
            serializableMethod.equals(anotherSerializableMethod));

      // test hashCode
      assertEquals(
            "Failure - Two SerializableMethod instances for a method involving generics have different hashCode",
            serializableMethod.hashCode(), anotherSerializableMethod.hashCode());

   }

   /**
    * Test the {@link SerializableMethod#toMethod()}
    * with simple methods
    * 
    * @throws Throwable
    */
   @Test
   public void testToMethodWithoutClassloader() throws Throwable
   {
      // Test that the toMethod() works fine when the Method, from which the SerializableMethod was created,
      // does not involve any primitive parameters

      logger.info("Testing the toMethod(), for methods without primitive params");

      Method methodWithoutPrimitivesParams = myClass.getClass().getMethod("methodWithParamAndReturningVoid",
            new Class[]
            {Integer.class});
      SerializableMethod serializableMethod = new SerializableMethod(methodWithoutPrimitivesParams, myClass.getClass());
      // invoke the toMethod()
      Method copyOfMethodWithoutPrimitiveParams = serializableMethod.toMethod();

      // test equals
      assertTrue(
            "Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method",
            methodWithoutPrimitivesParams.equals(copyOfMethodWithoutPrimitiveParams));

      // also test that the hashCode of the original method and the returned method are the same.
      assertEquals("Failure - hashCode does not match for method returnd by toMethod()", methodWithoutPrimitivesParams
            .hashCode(), copyOfMethodWithoutPrimitiveParams.hashCode());

      logger.info("Completed testing the toMethod(), for methods without primitive params");

   }

   /**
    * Test the {@link SerializableMethod#toMethod()}
    * with methods involving <a href="http://java.sun.com/j2se/1.5.0/docs/guide/language/generics.html">generics</a>
    * 
    * @throws Throwable
    */
   @Test
   public void testToMethodWithoutClassloaderAndInvolvingGenerics() throws Throwable
   {

      // Test that the toMethod() works fine when the Method, from which the SerializableMethod was created,
      // involves generics

      logger.info("Testing the toMethod(), for methods involving generics");

      Method methodWithGenerics = myClass.getClass().getMethod("methodWithGenerics", new Class[]
      {List.class, int.class});
      SerializableMethod serializableMethod = new SerializableMethod(methodWithGenerics, myClass.getClass());

      Method copyOfMethodWithGenerics = serializableMethod.toMethod();

      // test equals
      assertTrue(
            "Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method",
            methodWithGenerics.equals(copyOfMethodWithGenerics));

      // also test that the hashCode of the original method and the returned method are the same.
      assertEquals("Failure - hashCode does not match for method returnd by toMethod()", methodWithGenerics.hashCode(),
            copyOfMethodWithGenerics.hashCode());

      logger.info("Completed testing the toMethod, for methods involving generics");

   }

   /**
    * Test the {@link SerializableMethod#toMethod()}
    * with methods accepting primitive parameters
    * 
    * @throws Throwable
    */
   @Test
   public void testToMethodWithoutClassloaderAndInvolvingPrimitiveParams() throws Throwable
   {
      // Test that the toMethod() works fine when the Method, from which the SerializableMethod was created,
      // involves primitive params 

      logger.info("Testing the toMethod(), for methods accepting primitives");

      Method method = myClass.getClass().getMethod("methodWithPrimitiveParamsAndReturningVoid", new Class[]
      {byte.class, short.class, int.class, long.class, char.class, float.class, double.class, boolean.class});
      SerializableMethod serializableMethod = new SerializableMethod(method, myClass.getClass());
      // invoke the toMethod()
      Method copyOfMethod = serializableMethod.toMethod();

      // test equals
      assertTrue(
            "Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method",
            method.equals(copyOfMethod));
      // also test the hashCode
      assertEquals("Failure - hashCode of the original method and the method returned by toMethod() dont match", method
            .hashCode(), copyOfMethod.hashCode());

      logger.info("Completed testing the toMethod(), for methods accepting primitives");
   }

   /**
    * Test the {@link SerializableMethod#toMethod()}
    *  
    * @throws Throwable
    */
   @Test
   public void testToMethodWithoutClassloaderAndInvolvingReturnTypes() throws Throwable
   {
      // Test that the toMethod() works fine when the Method, from which the SerializableMethod was created,
      // returns some Object 

      logger.info("Testing the toMethod(), for methods returing Object");

      Method methodReturningInteger = myClass.getClass().getMethod("methodReturingInteger", new Class[]
      {Integer.class});
      SerializableMethod serializableMethod = new SerializableMethod(methodReturningInteger, myClass.getClass());
      // invoke the toMethod()
      Method copyOfMethodReturingInteger = serializableMethod.toMethod();

      // test equals
      assertTrue(
            "Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method",
            methodReturningInteger.equals(copyOfMethodReturingInteger));
      // also test the hashCode
      assertEquals("Failure - hashCode of the original method and the method returned by toMethod() dont match",
            methodReturningInteger.hashCode(), copyOfMethodReturingInteger.hashCode());

      logger.info("Completed testing the toMethod(), for methods returing Object");

   }

   /**
    * Test the {@link SerializableMethod#toMethod()}
    * with methods accepting array parameters
    * 
    * @throws Throwable
    */
   @Test
   public void testToMethodWithoutClassloaderAndInvolvingArrayParams() throws Throwable
   {

      // Test that the toMethod() works fine when the Method, from which the SerializableMethod was created,
      // accepts array of primitives

      logger.info("Testing the toMethod(), for methods accepting array of primitives");

      Method methodAcceptingArrayOfPrimitives = myClass.getClass().getMethod("methodAcceptingArrayOfPrimitives",
            new Class[]
            {int[].class});
      SerializableMethod serializableMethod = new SerializableMethod(methodAcceptingArrayOfPrimitives, myClass
            .getClass());
      // invoke the toMethod()
      Method copyOfMethodAcceptingArrayOfPrimitives = serializableMethod.toMethod();

      //test equals
      assertTrue(
            "Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method",
            methodAcceptingArrayOfPrimitives.equals(copyOfMethodAcceptingArrayOfPrimitives));

      // test hashCode
      assertEquals("Failure - The method returned by toMethod() has a different hashCode than the original method",
            methodAcceptingArrayOfPrimitives.hashCode(), copyOfMethodAcceptingArrayOfPrimitives.hashCode());

      logger.info("Completed testing the toMethod(), for methods accepting array of primitives");

      logger.info("Testing the toMethod(), for methods accepting array of Objects");

      // Test that the toMethod() works fine when the Method, from which the SerializableMethod was created,
      // accepts array of Objects

      Method methodAcceptingArrayOfObjects = myClass.getClass().getMethod("methodAcceptingArrayOfObjects", new Class[]
      {Object[].class});
      SerializableMethod serializableMethod_arrayOfObjParams = new SerializableMethod(methodAcceptingArrayOfObjects,
            myClass.getClass());
      //invoke the toMethod()
      Method copyOfMethodAcceptingArrayOfObjects = serializableMethod_arrayOfObjParams.toMethod();

      // test equals
      assertTrue("Failure - The method returned by toMethod() of SerializableMethod is not equal to original method",
            methodAcceptingArrayOfObjects.equals(copyOfMethodAcceptingArrayOfObjects));

      // test hashCode
      assertEquals("Failure -hashCode does not match for the method returned by toMethod()",
            methodAcceptingArrayOfObjects.hashCode(), copyOfMethodAcceptingArrayOfObjects.hashCode());

      logger.info("Completed testing the toMethod(), for methods accepting array of Objects");

   }

   /**
    * Test the {@link SerializableMethod#toMethod(ClassLoader)}
    * 
    * @throws Throwable
    */
   @Test
   public void testToMethodWithClassloader() throws Throwable
   {

      // Test the toMethod(Classloader) works fine

      logger.info("Testing the toMethod(Classloader)");

      Method method = myClass.getClass().getMethod("methodAcceptingMyClass", new Class[]
      {MyClass.class});
      Method methodToString = myClass.getClass().getMethod("toString", new Class[]
      {});
      SerializableMethod serializableMethod = new SerializableMethod(method, myClass.getClass());
      SerializableMethod serializableMethodToString = new SerializableMethod(methodToString, myClass.getClass());

      // invoke toMethod
      Method copyOfMethod = serializableMethod.toMethod(this.getClass().getClassLoader());
      Method copyOfMethodToString = serializableMethodToString.toMethod(this.getClass().getClassLoader());

      // test equals 
      assertTrue("Failure - equals fails with classsloader passed to toMethod", method.equals(copyOfMethod));

      // test hashCode
      assertEquals("Failure - hashCode does not match when classloader is passed to toMethod", method.hashCode(),
            copyOfMethod.hashCode());

      // test toString
      assertEquals("Roundtrip of inherited method toString failed", copyOfMethodToString, methodToString);

      logger.info("Completed testing the toMethod(Classloader)");

   }

   /**
    * Test the {@link SerializableMethod#toMethod()}
    * on a serialized/de-serialized instance of {@link SerializableMethod}
    * 
    * @throws Throwable
    * @see {@link SerializationUtil}
    */
   @Test
   public void testToMethodWithSerialization() throws Throwable
   {
      // Test the toMethod() to ensure that the serialized/deserialized
      // instance of the SerializableMethod returns the correct Method

      logger.info("Testing toMethod() with serialization");

      Method method = myClass.getClass().getMethod("methodAcceptingMyClass", new Class[]
      {MyClass.class});

      SerializableMethod serializableMethod = new SerializableMethod(method, myClass.getClass());

      // now make a copy through serialization/de-serialization
      SerializableMethod copyOfSerializableMethod = (SerializableMethod) SerializationUtil.getCopy(serializableMethod);
      Method copyOfMethod = copyOfSerializableMethod.toMethod();

      // test equals
      assertTrue("Failure - equals fails for toMethod when serialized", method.equals(copyOfMethod));

      // test hashCode
      assertEquals("Failure - hashCode does not match for Method returned by toMethod, when serialized", method
            .hashCode(), copyOfMethod.hashCode());

      logger.info("Completed testing toMethod() with serialization");

      logger.info("Completed testing toMethod() with serialization");
   }

   /**
    * Test to ensure that when no actual class is specified,
    * this field is automatically populated to the value of the declaring
    * class 
    * 
    * @throws Throwable
    */
   @Test
   public void testDeclaringClassDefaultsWhenNoActualClassSpecified() throws Throwable
   {
      // Obtain a method
      Method method = Object.class.getMethod("toString", new Class<?>[]
      {});

      // Create a Serializable View, without noting an actual class
      SerializableMethod sm = new SerializableMethod(method);

      // Get the actual and declaring class names
      String declaringClassName = sm.getDeclaringClassName();
      String actualClassName = sm.getActualClassName();

      // Ensure they're equal
      assertEquals("When no actual class is specified, should default to the declaring class", declaringClassName,
            actualClassName);

   }

   /**
    * Test to ensure that the {@link SerializableMethod#toMethod()} works as expected 
    * when varargs are involved.
    * 
    * @throws Throwable
    */
   @Test
   public void testToMethodForVarArgs() throws Throwable
   {

      logger.info("Testing the toMethod(), for methods accepting primitive varargs");

      // Get the primitive vararg method
      Method primitiveVarArgMethod = myClass.getClass().getMethod("methodWithPrimitiveVarArgsAndReturningVoid",
            new Class<?>[]
            {int[].class});

      // Create SerializableMethod
      SerializableMethod serializableMethodForPrimitiveVarArgMethod = new SerializableMethod(primitiveVarArgMethod);

      // Call toMethod
      Method returnedMethod = serializableMethodForPrimitiveVarArgMethod.toMethod();
      
      //test equals
      assertTrue("Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method", returnedMethod.equals(primitiveVarArgMethod));

      // test hashCode
      assertEquals("Failure - The method returned by toMethod() has a different hashCode than the original method", returnedMethod.hashCode(), primitiveVarArgMethod.hashCode());

      logger.info("Completed testing the toMethod(), for methods accepting primitive varargs");
      
      logger.info("Testing the toMethod(), for methods accepting non-primitive varargs");
      // Get method for non-primitive vararg
      Method nonPrimitiveVarArgMethod = myClass.getClass().getMethod("methodWithVarArgsAndReturningVoid", new Class<?>[]{Integer[].class});
      
      // Create a serializablemethod out of it
      SerializableMethod serializableMethodForNonPrimitiveVarArg = new SerializableMethod(nonPrimitiveVarArgMethod);
      
      // Call toMethod
      Method returnedMethodForNonPrimitiveVarArg = serializableMethodForNonPrimitiveVarArg.toMethod();
      
      // test equals
      assertTrue("Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method", returnedMethodForNonPrimitiveVarArg.equals(nonPrimitiveVarArgMethod));

      // test hashCode
      assertEquals("Failure - The method returned by toMethod() has a different hashCode than the original method", returnedMethodForNonPrimitiveVarArg.hashCode(), nonPrimitiveVarArgMethod.hashCode());

      logger.info("Completed testing the toMethod(), for methods accepting non-primitive varargs");
      
      
      logger.info("Testing the toMethod(), for methods accepting a normal arg and a vararg");
      // Get the method
      Method mixedVarArgMethod = myClass.getClass().getMethod("methodWithVarArgsAndNormalArg", new Class<?>[]{String.class,Object[].class});
      
      // Create a serializablemethod out of it
      SerializableMethod serializableMethodForMixedVarArgMethod = new SerializableMethod(mixedVarArgMethod);
      
      // Call toMethod
      Method returnedMethodForMixedVarArgMethod = serializableMethodForMixedVarArgMethod.toMethod();
      
      // test equals
      assertTrue("Failure - The method returned by toMethod() of SerializableMethod is not equal to the original method", returnedMethodForMixedVarArgMethod.equals(mixedVarArgMethod));

      // test hashCode
      assertEquals("Failure - The method returned by toMethod() has a different hashCode than the original method", returnedMethodForMixedVarArgMethod.hashCode(), mixedVarArgMethod.hashCode());

      logger.info("Completed testing the toMethod(), for methods accepting a normal arg and a vararg");


   }

}
