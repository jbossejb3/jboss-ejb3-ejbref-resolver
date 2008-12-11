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
package org.jboss.ejb3.common.resolvers.spi;

/**
 * EjbReferenceResolverFactory
 * 
 * Factory allowing abstraction of EjbReferenceResolver instances
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version : $
 */
public class EjbReferenceResolverFactory
{
   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Prohibits instanciation
    */
   private EjbReferenceResolverFactory()
   {
   }

   // --------------------------------------------------------------------------------||
   // Factory Members ----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns a new EjbReferenceResolver using the specified implementation
    * 
    * @param implementationClassName
    * @return
    */
   public static EjbReferenceResolver newInstance(String implementationClassName)
   {
      try
      {
         // Get the class from the defining ClassLoader of this class
         Class<?> implClass = Class.forName(implementationClassName);

         // Ensure we've got a type match
         assert EjbReferenceResolver.class.isAssignableFrom(implClass) : "Specified implementation "
               + implementationClassName + " is not a valid " + EjbReferenceResolver.class.getName();

         // Make a new instance, cast and return it
         return (EjbReferenceResolver) implClass.newInstance();

      }
      catch (Throwable t)
      {
         throw new RuntimeException("Could not create new " + EjbReferenceResolver.class.getSimpleName()
               + " with implementation of " + implementationClassName, t);
      }
   }
}
