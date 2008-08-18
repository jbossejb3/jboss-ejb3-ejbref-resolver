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
package org.jboss.ejb3.common.registrar.plugin.mc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.ejb3.common.registrar.spi.DuplicateBindException;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;

/**
 * Ejb3McRegistrar
 * 
 * Microcontainer-based Implementation of the Ejb3Registrar 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 * @see {@link Ejb3Registrar}
 */
public class Ejb3McRegistrar implements Ejb3Registrar
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(Ejb3McRegistrar.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The Kernel instance pointing to the underlying Object Store
    */
   private Kernel kernel;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public Ejb3McRegistrar(final Kernel kernel)
   {
      this.setKernel(kernel);
      log.debug("Created " + Ejb3McRegistrar.class.getSimpleName() + " with backing " + Kernel.class.getSimpleName()
            + ": " + this.getKernel());
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /**
    * Lists out all installed (bound) objects in form
    * key == name , value == object.  Primarily for 
    * metrics/debugging/management.  If nothing is installed,
    * an empty Map will be returned.  The returned Map is
    * immutable.
    * 
    * @return
    */
   public Map<Object, Object> list()
   {

      // Obtain all installed Contexts
      Set<ControllerContext> installedContexts = this.getKernel().getController().getContextsByState(
            ControllerState.INSTALLED);

      // If nothing is installed
      if (installedContexts == null)
      {
         // Return an empty Map
         return new HashMap<Object, Object>();
      }

      // Initialize a Map
      Map<Object, Object> installedObjects = new ConcurrentHashMap<Object, Object>();

      // For each installed Context
      for (ControllerContext context : installedContexts)
      {
         Object bindName = context.getName();
         Object value = context.getTarget();
         installedObjects.put(bindName, value == null ? "[null]" : value);
      }

      // Decorate as immutable
      installedObjects = Collections.unmodifiableMap(installedObjects);

      // Return
      return installedObjects;
   }

   /**
    * Obtains the value bound at the specified name, 
    * throwing NotBoundException if there is nothing
    * bound at the key
    * 
    * @param name
    * @throws NotBoundException
    * @return
    */
   public Object lookup(final Object name) throws NotBoundException
   {
      // Get Controller Context
      ControllerContext context = this.getKernel().getController().getInstalledContext(name);

      // Ensure Bound
      if (context == null || context.getTarget() == null)
      {
         throw new NotBoundException("Requested value bound at name \"" + name + "\" is not bound.");
      }

      // If there's an error with the context, throw it
      Throwable error = context.getError();
      if (error != null)
      {
         throw new RuntimeException("Could not lookup object at name \"" + name
               + "\" due to an error with the underlying " + ControllerContext.class.getSimpleName(), error);
      }

      // Return
      Object target = context.getTarget();
      log.debug("Returning from name \"" + name + "\": " + target);
      return target;
   }
   
   /**
    * Obtains the value bound at the specified name, 
    * throwing NotBoundException if there is nothing
    * bound at the key.  The value returned will be automatically
    * casted to the specified type.
    * 
    * @param <T>
    * @param name
    * @param type
    * @return
    * @throws NotBoundException
    */
   public <T> T lookup(Object name, Class<T> type) throws NotBoundException
   {
      // Obtain object
      Object obj = this.lookup(name);
      
      // Cast
      T returned = null;
      try
      {
         returned = type.cast(obj);
      }
      catch(ClassCastException cce)
      {
         throw new RuntimeException("Value returned from key \"" + name
               + "\" in Object Store was not of expected type " + type + ", but was instead "
               + obj.getClass().getName());
      }
      
      // Return
      return returned;
   }

   /**
    * Binds the specified value to the key of specified name, 
    * throwing a DuplicateBindException in the case the
    * name is not unique
    * 
    * @param name
    * @param value
    * @throws DuplicateBindException
    */
   public void bind(final Object name, final Object value) throws DuplicateBindException
   {
      // Ensure there's nothing already at this location
      Object existing = null;
      try
      {
         existing = this.lookup(name);
      }
      // Expected
      catch (NotBoundException e)
      {
         // Install
         this.install(name, value);
         return;
      }

      // Something is already here, throw an exception
      throw new DuplicateBindException("Cannot install " + value + " under name \"" + name
            + "\" as there is already an existing object there: " + existing);
   }

   /**
    * Binds the specified value to the key of specified name, 
    * optionally unbinding the current value if one exists
    * 
    * @param name
    * @param value
    */
   public void rebind(final Object name, final Object value)
   {
      // Initialize
      boolean alreadyBound = true;

      // Determine if already bound
      try
      {
         this.lookup(name);
      }
      // We need to unbind first
      catch (NotBoundException nbe)
      {
         alreadyBound = false;
      }

      // If this name is already bound
      if (alreadyBound)
      {
         try
         {
            // Unbind
            this.unbind(name);
         }
         // Should not occur, if so we've got an error in implementation
         catch (NotBoundException e)
         {
            throw new RuntimeException("Lookup in registry for name \"" + name
                  + "\" has reported an object already bound, but attempt to unbind has failed with "
                  + NotBoundException.class.getSimpleName(), e);
         }
      }

      // Install 
      this.install(name, value);
   }

   /**
    * Unbinds the object at the specified name, throwing
    * NotBoundException if no object exists at that name
    * 
    * @param name
    * @throws NotBoundException
    */
   public void unbind(final Object name) throws NotBoundException
   {
      // Ensure there is an object bound at this location
      try
      {
         this.lookup(name);
      }
      catch (NotBoundException nbe)
      {
         throw new NotBoundException("Could not unbind object at name \"" + name + "\" as none is currently bound");
      }

      // Uninstall
      log.debug("Uninstalling bean with name \"" + name + "\"...");
      this.getKernel().getController().uninstall(name);
      log.debug("Bean with name \"" + name + "\" uninstalled.");
   }

   /**
    * Returns a provider implementation-specific class
    * to break contract and invoke upon vendor-specific
    * features.
    * 
    * @return
    */
   public Kernel getProvider()
   {
      return this.getKernel();
   }

   /**
    * Invokes the specified method name on the object bound at the specified name, 
    * returning the result
    * 
    * @param name
    * @param methodName
    * @param arguments Arguments to pass to the method
    * @param signature String representation of fully-qualified class names of parameter types
    * @return
    * @throws NotBoundException If no object is bound at the specified name
    */
   public Object invoke(Object name, String methodName, Object[] arguments, String[] signature)
         throws NotBoundException
   {
      // Ensure there is an object bound at this location
      try
      {
         this.lookup(name);
      }
      catch (NotBoundException nbe)
      {
         throw new NotBoundException("Could not invoke upon object at name \"" + name + "\" as none is currently bound");
      }

      // Invoke
      try
      {
         log.debug("Invoking on bean with name \"" + name + "\" method " + methodName + ".(" + signature
               + ") with arguments " + arguments + "...");
         Object result = this.getKernel().getBus().invoke(name, methodName, arguments, signature);
         log.debug("Invocation Result: " + result);
         return result;
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error occured in invoking method \"" + methodName
               + "\" upon object bound at name " + name, t);

      }
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Installs the specified value into MC at the specified name
    */
   private void install(final Object name, final Object value)
   {
      // Construct BMDB
      BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder(name.toString(), value.getClass().getName());

      // Install into MC
      try
      {
         this.getKernel().getController().install(bmdb.getBeanMetaData(), value);
      }
      catch (Throwable e)
      {
         throw new RuntimeException("Could not install at name \"" + name + "\" value " + value, e);
      }
      log.debug("Installed in " + this.getKernel().getController() + " at \"" + name + "\": " + value);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private Kernel getKernel()
   {
      return kernel;
   }

   private void setKernel(final Kernel kernel)
   {
      this.kernel = kernel;
   }

}
