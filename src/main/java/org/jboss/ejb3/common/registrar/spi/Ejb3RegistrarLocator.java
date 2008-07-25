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
package org.jboss.ejb3.common.registrar.spi;

import org.jboss.logging.Logger;

/**
 * Ejb3RegistrarLocator
 * 
 * Provides simple mechanism for locating and setting
 * the Ejb3Registrar.  Once set, the Ejb3Registrar is
 * immutable and cannot be replaced.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3RegistrarLocator
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(Ejb3RegistrarLocator.class);

   /**
    * Singleton instance
    */
   private static Ejb3RegistrarLocator instance;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The Registrar implementation of record, may only be set once and is then
    * immutable
    */
   private Ejb3Registrar registrar;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private Ejb3RegistrarLocator(Ejb3Registrar registrar)
   {
      this.setRegistrar(registrar);
   }

   // --------------------------------------------------------------------------------||
   // Singleton ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Ejb3Registrar associated with this
    * process; in the case one has not yet been bound, 
    * a RuntimeException will be thrown
    * 
    * @return
    * @throws NotBoundException
    */
   public static Ejb3Registrar locateRegistrar() throws NotBoundException
   {
      // If no registrar implementation has been set
      if (Ejb3RegistrarLocator.instance == null)
      {
         // Throw unchecked exception to the user
         throw new NotBoundException("Could not retrieve " + Ejb3Registrar.class.getSimpleName()
               + " as a registrar implementation has not yet been bound.");
      }

      // Return the registrar
      return Ejb3RegistrarLocator.instance.getRegistrar();
   }

   /**
    * Binds the specified Ejb3Registrar implementation
    * as the registrar of record for the life of this process, 
    * immutable once set 
    * 
    * @param registrar
    * @throws DuplicateBindException
    */
   public synchronized static void bindRegistrar(Ejb3Registrar registrar) throws DuplicateBindException
   {
      // Immutable once bound
      if (Ejb3RegistrarLocator.instance != null)
      {
         throw new DuplicateBindException(Ejb3Registrar.class.getSimpleName()
               + " is already bound and is now immutable");
      }

      // Make a new instance and set registrar
      Ejb3RegistrarLocator.instance = new Ejb3RegistrarLocator(registrar);
      log.debug("Bound " + Ejb3Registrar.class.getSimpleName() + ": " + registrar);
   }

   /**
    * Unbinds the current Ejb3Registrar implementation
    * as the registrar of record 
    * 
    * @param registrar
    * @throws NotBoundException
    */
   public synchronized static void unbindRegistrar() throws NotBoundException
   {
      // Ensure bound
      if (!isRegistrarBound())
      {
         throw new NotBoundException(Ejb3Registrar.class.getSimpleName() + " is not bound, cannot unbind");
      }

      // Unbind Registrar
      Ejb3Registrar reg = Ejb3RegistrarLocator.instance.getRegistrar();
      log.debug("Unbinding " + Ejb3Registrar.class.getSimpleName() + ": " + reg);
      Ejb3RegistrarLocator.instance.registrar = null;
   }

   /**
    * Returns whether or not the Ejb3Registrar 
    * has been bound to this Process
    * 
    * @return
    */
   public static boolean isRegistrarBound()
   {
      return Ejb3RegistrarLocator.instance != null;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private Ejb3Registrar getRegistrar()
   {
      return this.registrar;
   }

   private void setRegistrar(Ejb3Registrar registrar)
   {
      this.registrar = registrar;
   }

}
