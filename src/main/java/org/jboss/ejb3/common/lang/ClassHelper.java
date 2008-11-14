/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.common.lang;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Useful methods for classes.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class ClassHelper
{
   /**
    * Casts an object to the class or interface represented
    * by the targetClass <tt>Class</tt> object.
    * The ClassCastException thrown is more descriptive than
    * the original.
    *
    * @param obj the object to be cast
    * @return the object after casting, or null if obj is null
    *
    * @throws ClassCastException if the object is not
    * null and is not assignable to the type T.
    */
   public static <T> T cast(Class<T> targetClass, Object obj)
   {
      try
      {
         return targetClass.cast(obj);
      }
      catch(ClassCastException e)
      {
         assert obj != null : "a null can always be cast, it should never throw a ClassCastException";
         throw new ClassCastException("Unable to cast " + obj.getClass() + " to " + targetClass);
      }
   }
   
   /**
    * @see Class#argumentTypesToString
    */
   private static String argumentTypesToString(Class<?>[] argTypes)
   {
      StringBuilder buf = new StringBuilder();
      buf.append("(");
      if (argTypes != null)
      {
         for (int i = 0; i < argTypes.length; i++)
         {
            if (i > 0)
            {
               buf.append(", ");
            }
            Class<?> c = argTypes[i];
            buf.append((c == null) ? "null" : c.getName());
         }
      }
      buf.append(")");
      return buf.toString();
   }

   private static Method findPrivateMethod(Class<?> target, String methodName, Class<?>... paramTypes)
   {
      // Top of the world
      if(target == null)
         return null;
      
      // TODO: what is faster? scan or catch exception?
      for(Method method : SecurityActions.getDeclaredMethods(target))
      {
         if(method.getName().equals(methodName))
         {
            if(paramTypes == null)
               return method;
            
            if(Arrays.equals(method.getParameterTypes(), paramTypes))
               return method;
         }
      }
      
      return findPrivateMethod(target.getSuperclass(), methodName, paramTypes);
   }
   
   /**
    * Find all methods with a specific name on a class and it's super classes
    * regardless of parameter signature.
    * 
    * @param cls            the class to scan
    * @param methodName     the name of the methods to find
    * @return               a list of methods found, or empty
    */
   public static List<Method> getAllMethodsByName(Class<?> cls, String methodName)
   {
      List<Method> methods = new ArrayList<Method>();
      populateWithMethodsByName(methods, cls, methodName);
      return methods;
   }
   
   /**
    * Find all methods starting with the specified prefix on the specified
    * class
    * 
    * @param clazz
    * @param methodNamePrefix
    * @return
    */
   public static List<Method> getAllMethodsByPrefix(Class<?> clazz, String methodNamePrefix)
   {
      List<Method> methods = new ArrayList<Method>();
      ClassHelper.populateWithMethodsByPrefix(methods, clazz, methodNamePrefix);
      return methods;
   }
   
   /**
    * Returns the <code>Method</code> with the given attributes of either this class
    * or one of it's super classes.
    * 
    * TODO: return type specifics are not considered
    * FIXME: rename method (it must return all modifiers)
    * 
    * @param cls            class to scan
    * @param methodName     the name of the method
    * @param paramTypes     the parameter types
    * @return               the <code>Method</code> matching the method name and parameters
    * @throws NoSuchMethodException if no method can be found
    */
   public static Method getPrivateMethod(Class<?> cls, String methodName, Class<?>... paramTypes) throws NoSuchMethodException
   {
      assert cls != null : "cls is null";
      assert methodName != null : "methodName is null";
      
      Method result = findPrivateMethod(cls, methodName, paramTypes);
      if(result == null)
         throw new NoSuchMethodException(cls.getName() + "." + methodName + argumentTypesToString(paramTypes));
      
      return result;
   }
   
   /**
    * Obtains a Class corresponding to the specified type using
    * the specified ClassLoader, throwing a descriptive RuntimeException
    * in the case the Class could not be found
    * 
    * @param type
    * @param cl
    * @return
    * @author ALR
    */
   public static final Class<?> getClassFromTypeName(String type, ClassLoader cl)
   {
      try
      {
         // Load the class
         return cl.loadClass(type);
      }
      catch (ClassNotFoundException e)
      {
         // Throw descriptive message
         throw new RuntimeException("Specified class " + type + " could not be found by the "
               + ClassLoader.class.getSimpleName() + ", " + cl, e);
      }
   }
   
   private static void populateWithMethodsByName(List<Method> methods, Class<?> cls, String methodName)
   {
      // Top of the world
      if(cls == null)
         return;
      
      for(Method method : SecurityActions.getDeclaredMethods(cls))
      {
         if(method.getName().equals(methodName))
            methods.add(method);
      }
      
      populateWithMethodsByName(methods, cls.getSuperclass(), methodName);
   }
   
   private static void populateWithMethodsByPrefix(List<Method> methods, Class<?> clazz, String methodNamePrefix)
   {
      // Exit Condition
      if (clazz == null)
      {
         return;
      }

      // For all declared methods
      for (Method method : SecurityActions.getDeclaredMethods(clazz))
      {
         if (method.getName().startsWith(methodNamePrefix))
            methods.add(method);
      }

      populateWithMethodsByPrefix(methods, clazz.getSuperclass(), methodNamePrefix);
   }
}
