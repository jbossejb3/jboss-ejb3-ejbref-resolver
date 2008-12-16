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
package org.jboss.ejb3.common.lang;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.ejb3.common.classloader.PrimitiveAwareClassLoader;
import org.jboss.logging.Logger;

/**
 * SerializableMethod
 * 
 * A Serializable view of an Invoked Method, providing
 * overridden implementations of hashCode, equals, and toString
 * 
 * JIRA: EJBTHREE-1269
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SerializableMethod implements Serializable
{
   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(SerializableMethod.class);

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The name of the method
    */
   private String name;

   /**
    * Fully-qualified declaring class name of the method
    */
   private String declaringClassName;

   /**
    * Fully-qualified final (child) class name of the method, may have inherited the method
    */
   private String actualClassName;

   /**
    * Fully-qualfied class name of the return type of the method
    */
   private String returnType;

   /**
    * Array of fully-qualified class names of arguments, in order
    */
   private String[] argumentTypes;

   // ------------------------------------------------------------------------------||
   // Constructors -----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * Creates a Serializable Method View, using the declaring class
    * of the specified method as the actual class
    * 
    * @param method The method this view represents
    */
   public SerializableMethod(Method method)
   {
      this(method, method.getDeclaringClass());
   }

   /**
    * Constructor
    * 
    * Creates a Serializable Method View, allowing for
    * an alternate "actual class" defining the method in 
    * addition to the declaring class
    * 
    * @param method The method this view represents
    * @param actualClass The class to which this method is associated, 
    *   may be a child of the class declaring the method
    */
   public SerializableMethod(Method method, Class<?> actualClass)
   {
      // Pass to alternate constructor
      this(method, actualClass != null ? actualClass.getName() : null);
   }

   /**
    * Constructor
    * 
    * Creates a Serializable Method View, allowing for
    * an alternate "actual class" defining the method in 
    * addition to the declaring class
    * 
    * @param method The method this view represents
    * @param actualClass The name of the class to which this method is associated, 
    *   may be a child of the class declaring the method
    */
   public SerializableMethod(Method method, String actualClassName)
   {
      // Set properties
      this.setName(method.getName());
      this.setDeclaringClassName(method.getDeclaringClass().getName());
      this.setActualClassName(actualClassName);
      this.setReturnType(method.getReturnType().getName());
      Class<?>[] paramTypes = method.getParameterTypes();
      List<String> paramTypesString = new ArrayList<String>();
      if (paramTypes != null)
      {
         for (Class<?> paramType : paramTypes)
         {
            paramTypesString.add(paramType.getName());
         }
      }
      this.setArgumentTypes(paramTypesString.toArray(new String[]
      {}));
   }

   // ------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------||
   // ------------------------------------------------------------------------------||

   @Override
   public boolean equals(Object obj)
   {
      // If not an instance of SerializableMethod
      if (!(obj instanceof SerializableMethod))
      {
         // Different types, we can't be equal
         return false;
      }

      // Cast
      SerializableMethod other = SerializableMethod.class.cast(obj);

      // We're equal if all properties are equal
      return this.getDeclaringClassName().equals(other.getDeclaringClassName())
            && this.getName().equals(other.getName())
            && Arrays.equals(this.getArgumentTypes(), other.getArgumentTypes())
            && this.getActualClassName().equals(other.getActualClassName());
   }

   @Override
   public int hashCode()
   {
      // toString is unique, use it
      return this.toString().hashCode();
   }

   @Override
   public String toString()
   {
      // Initialize
      StringBuffer sb = new StringBuffer();

      // Construct
      sb.append(this.getActualClassName());
      sb.append(": ");
      sb.append(this.getDeclaringClassName());
      sb.append('.');
      sb.append(this.getName());
      sb.append('(');
      int count = 0;
      for (String argType : this.getArgumentTypes())
      {
         count++;
         sb.append(argType);
         if (count < this.getArgumentTypes().length)
         {
            sb.append(',');
         }
      }
      sb.append(')');

      // Return
      return sb.toString();
   }

   // ------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains the Method described by this view, using the
    * TCL
    * 
    * @return
    */
   public Method toMethod()
   {
      return this.toMethod(SecurityActions.getThreadContextClassLoader());
   }

   /**
    * Obtains the Method described by this view, using the
    * ClassLoader specified
    * 
    * @param cl
    * @return
    */
   public Method toMethod(ClassLoader cl)
   {
      // Load the Class described by the Method
      Class<?> invokingClass = this.getClassType(cl);

      // Load the argument types
      List<Object> argTypesList = new ArrayList<Object>();
      for (String argTypeName : this.getArgumentTypes())
      {
         Class<?> argType = this.getClassFromName(argTypeName, cl);
         argTypesList.add(argType);
      }
      Class<?>[] argTypes = argTypesList.toArray(new Class<?>[]
      {});

      // Obtain the Method
      String methodName = this.getName();
      Method invokedMethod = null;
      try
      {
         invokedMethod = invokingClass.getMethod(methodName, argTypes);
      }
      catch (NoSuchMethodException nsme)
      {
         throw new RuntimeException("Method " + this + " does not exist in " + invokingClass.getName(), nsme);
      }

      // Return
      return invokedMethod;
   }

   /**
    * Obtains the Class described by this view, using the
    * TCL
    * 
    * @return
    */
   public Class<?> getClassType()
   {
      return this.getClassType(Thread.currentThread().getContextClassLoader());
   }

   /**
    * Obtains the Class described by this view, using the
    * specified ClassLoader
    * 
    * @param cl
    * @return
    */
   public Class<?> getClassType(ClassLoader cl)
   {
      // Obtain
      Class<?> clazz = this.getClassFromName(this.getDeclaringClassName(), cl);

      // Return 
      return clazz;
   }

   /**
    * Returns the class associated with the given name
    * 
    * @param name
    * @param cl
    * @return
    */
   protected Class<?> getClassFromName(String name, ClassLoader cl)
   {
      // Perform assertions
      assert cl != null : ClassLoader.class.getSimpleName() + " must be defined.";
      

      // Load the Class described by the Method
      Class<?> clazz = null;

      try
      {
         // use the PrimitiveAwareClassLoader to avoid any primitive check here, in this method.
         clazz = new PrimitiveAwareClassLoader(cl).loadClass(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         throw new RuntimeException("Specified calling class, " + name + " could not be found for " + cl, cnfe);
      }

      // Return
      return clazz;
   }

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public String getDeclaringClassName()
   {
      return declaringClassName;
   }

   public void setDeclaringClassName(String className)
   {
      this.declaringClassName = className;
   }

   public String getReturnType()
   {
      return returnType;
   }

   public void setReturnType(String returnType)
   {
      this.returnType = returnType;
   }

   public String[] getArgumentTypes()
   {
      return argumentTypes;
   }

   public void setArgumentTypes(String[] argumentTypes)
   {
      this.argumentTypes = argumentTypes;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getActualClassName()
   {
      return actualClassName;
   }

   public void setActualClassName(String actualClassName)
   {
      // If no actual class name is specified
      if (actualClassName == null || actualClassName.trim().length() == 0)
      {
         // Use the declaring class name
         String declaringClassName = this.getDeclaringClassName();
         assert declaringClassName != null && declaringClassName.trim().length() > 0 : "Cannot implicitly set actual class to declaring class as declaring class is not defined";
         actualClassName = declaringClassName;
         log.debug("Actual class name has not been explicitly specified, so defaulting to declaring class name: "
               + declaringClassName);
      }
      this.actualClassName = actualClassName;
   }

}
