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
package org.jboss.ejb3.common.classloader;


/**
 * PrimitiveAwareClassLoader
 *  
 * This is more of a hack to avoid checking for primitives at multiple places
 * while loading a class from a name. <br/>
 * 
 * The {@link PrimitiveAwareClassLoader} will first check whether the request
 * is to load a primitive. If it's a primitive then it returns back the appropriate
 * {@link Class} corresponding to the primitive. For all other requests, it redirects
 * the request to the parent classloader.
 *
 * @deprecated Do not use this "classloader" any more. Instead use the {@link PrimitiveClassLoadingUtil}
 * utility to take care of centralized logic for primitive handling during classloading. See 
 * https://jira.jboss.org/jira/browse/EJBTHREE-1910 for more details.
 * 
 *  @see PrimitiveClassLoadingUtil#loadClass(String, ClassLoader)
 *  @see https://jira.jboss.org/jira/browse/EJBTHREE-1910
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Deprecated
public class PrimitiveAwareClassLoader extends ClassLoader
{

   /**
    * 
    * @param parent Parent classloader
    */
   public PrimitiveAwareClassLoader(ClassLoader parent)
   {
      super(parent);
   }

   /**
    * As recommended in {@link ClassLoader#findClass(java.lang.String)}, the findClass method
    * should be overriden by the custom classloaders. This method will first check whether 
    * the requested <code>name</code> is a primitive. If yes, it returns the appropriate {@link Class}
    * for the primitive. If not, then it lets the parent handle it.
    * 
    */
   @Override
   protected java.lang.Class<?> findClass(String name) throws ClassNotFoundException
   {

      /*
       * Handle Primitives
       */
      if (name.equals(void.class.getName()))
      {
         return void.class;
      }
      if (name.equals(byte.class.getName()))
      {
         return byte.class;
      }
      if (name.equals(short.class.getName()))
      {
         return short.class;
      }
      if (name.equals(int.class.getName()))
      {
         return int.class;
      }
      if (name.equals(long.class.getName()))
      {
         return long.class;
      }
      if (name.equals(char.class.getName()))
      {
         return char.class;
      }
      if (name.equals(boolean.class.getName()))
      {
         return boolean.class;
      }
      if (name.equals(float.class.getName()))
      {
         return float.class;
      }
      if (name.equals(double.class.getName()))
      {
         return double.class;
      }

      // Now that we know, its not a primitive, lets just allow
      // the parent to handle the request.
      // Note that we are intentionally using Class.forName(name,boolean,cl)
      // to handle issues with loading array types in Java 6 http://bugs.sun.com/view_bug.do?bug_id=6434149
      return Class.forName(name, false, this.getParent());

   }

}
