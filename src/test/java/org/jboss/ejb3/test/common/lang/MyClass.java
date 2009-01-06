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
package org.jboss.ejb3.test.common.lang;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.test.common.lang.unit.SerializableMethodTestCase;

/**
 * 
 * MyClass - Helper class for getting various {@link Method}s for testing
 * {@link SerializableMethod}
 *
 * @author Jaikiran Pai
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 * @see {@link SerializableMethodTestCase}
 */
public class MyClass
{

   /**
    * 
    */
   public void methodWithNoParamAndReturningVoid()
   {
      //do nothing
   }

   /**
    * 
    * @param i
    */
   public void methodWithParamAndReturningVoid(Integer i)
   {
      //do nothing
   }

   /**
    * 
    * @param s
    */
   public void methodWithParamAndReturningVoid(String s)
   {
      //do nothing
   }

   /**
    * 
    * @param s
    */
   public void methodWithParamAndReturningVoid(int primitive)
   {
      //do nothing
   }

   /**
    * 
    * @param b
    * @param s
    * @param i
    * @param l
    * @param c
    * @param f
    * @param d
    * @param b
    */
   public void methodWithPrimitiveParamsAndReturningVoid(byte b, short s, int i, long l, char c, float f, double d,
         boolean bo)
   {
      // Do Nothing
   }

   /**
    * 
    * @param obj
    */
   public void methodWithParamAndReturningVoid(Object obj)
   {
      //do nothing
   }

   /**
    * 
    */
   public String toString()
   {
      return this.getClass().getName();
   }

   /**
    * 
    * @param a
    * @return
    */
   public int methodAcceptingArrayOfPrimitives(int[] a)
   {
      return a[0];
   }

   /**
    * 
    * @param a
    * @return
    */
   public Object methodAcceptingArrayOfObjects(Object[] objs)
   {
      return objs[0];
   }

   /**
    * 
    * @param list
    * @return
    */
   public Class<?> methodWithGenerics(List<?> list, int i)
   {
      return null;
   }

   /**
    * 
    * @param i
    * @return
    */
   public Integer methodReturingInteger(Integer i)
   {
      return null;
   }

   /**
    * 
    * @param m
    * @return
    */
   public MyClass methodAcceptingMyClass(MyClass m)
   {
      return m;
   }

   /**
    * 
    * @param ints
    */
   public void methodWithPrimitiveVarArgsAndReturningVoid(int... ints)
   {
      // do nothing
   }

   /**
    * 
    * @param integers
    */
   public void methodWithVarArgsAndReturningVoid(Integer... integers)
   {
      // do nothing
   }

   /**
    * 
    * @param someString
    * @param objects
    */
   public void methodWithVarArgsAndNormalArg(String someString, Object... objects)
   {
      // do nothing
   }

}
