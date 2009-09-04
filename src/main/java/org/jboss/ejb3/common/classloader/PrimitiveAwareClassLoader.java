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

import org.jboss.ejb3.common.classloader.util.PrimitiveClassLoadingUtil;

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
 * @deprecated Since 1.0.1 version of jboss-ejb3-common : Do not use this "classloader" any more. 
 * Instead use the {@link PrimitiveClassLoadingUtil}
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
    * Since jboss-ejb3-common 1.0.1, this just delegates the call to 
    * {@link PrimitiveClassLoadingUtil#loadClass(String, ClassLoader)} passing
    * it the classloader returned by {@link #getParent()}.
    * 
    * @see PrimitiveClassLoadingUtil
    */
   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      // EJBTHREE-1910 Let the new util handle this instead of we doing it ourselves
      // See the javadocs of PrimitiveClassLoadingUtil for more details and reasoning
      return PrimitiveClassLoadingUtil.loadClass(name, this.getParent());
   }

}
