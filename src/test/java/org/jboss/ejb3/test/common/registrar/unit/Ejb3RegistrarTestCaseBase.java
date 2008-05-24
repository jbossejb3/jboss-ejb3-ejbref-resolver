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
package org.jboss.ejb3.test.common.registrar.unit;

import java.util.UUID;

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.DuplicateBindException;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.test.common.registrar.SimplePojo;
import org.junit.Test;

/**
 * Ejb3RegistrarTestCaseBase
 * 
 * Common Test Cases for implementations 
 * of the Ejb3Registrar
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3RegistrarTestCaseBase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The name of the POJO as defined in the *-beans.xml
    */
   private static final String REGISTRY_NAME_POJO = "org.jboss.ejb3.SimplePojo";

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that a simple lookup succeeds
    * 
    * @throws Throwable
    */
   @Test
   public void testRegistrarLookup() throws Throwable
   {
      SimplePojo pojo = (SimplePojo) Ejb3RegistrarLocator.locateRegistrar().lookup(
            Ejb3RegistrarTestCaseBase.REGISTRY_NAME_POJO);
      TestCase.assertNotNull("Retrieved POJO from Registry was null", pojo);
   }

   /**
    * Ensures that a new binding/lookup roundtrip succeeds
    * with the same object retrived as that which was placed into the registry
    * 
    * @throws Throwable
    */
   @Test
   public void testRegistrarBindAndLookup() throws Throwable
   {
      // Initialize
      String bindName = "org.jboss.ejb3.TestBind" + UUID.randomUUID();
      String propertyValue = "propValue";
      SimplePojo pojo = new SimplePojo();
      pojo.setProperty(propertyValue);

      // Bind
      Ejb3RegistrarLocator.locateRegistrar().bind(bindName, pojo);

      // Lookup
      SimplePojo retrieved = (SimplePojo) Ejb3RegistrarLocator.locateRegistrar().lookup(bindName);

      // Test
      TestCase
            .assertTrue("Retrieved value is not equal by reference to what was put in to registry", pojo == retrieved);
      TestCase.assertEquals("Set property was not equal to that which was put into Registry", propertyValue, retrieved
            .getProperty());
   }

   /**
    * Tests that a rebind results in a new object replacing 
    * the old in the Registry
    * 
    * @throws Throwable
    */
   @Test
   public void testRegistrarRebind() throws Throwable
   {
      // Initialize
      String bindName = Ejb3RegistrarTestCaseBase.REGISTRY_NAME_POJO;

      // Lookup the default pojo
      SimplePojo pojoExisting = (SimplePojo) Ejb3RegistrarLocator.locateRegistrar().lookup(bindName);
      TestCase.assertNotNull("Retrieved POJO from Registry was null", pojoExisting);

      // Make a new POJO
      SimplePojo pojoNew = new SimplePojo();

      // Rebind into existing address
      Ejb3RegistrarLocator.locateRegistrar().rebind(bindName, pojoNew);

      // Lookup 
      SimplePojo retrieved = (SimplePojo) Ejb3RegistrarLocator.locateRegistrar().lookup(bindName);

      // Test
      TestCase.assertTrue("Old value and retrieved from registry should not be equal by reference",
            pojoExisting != retrieved);
      TestCase.assertTrue("Placed value and retrieved from registry must be equal by reference", pojoNew == retrieved);

   }

   @Test
   public void testRegistrarDuplicateBindFails() throws Throwable
   {
      // Initialize
      String bindName = Ejb3RegistrarTestCaseBase.REGISTRY_NAME_POJO;

      // Make a new POJO
      SimplePojo pojoNew = new SimplePojo();

      // Attempt to bind into existing address
      try
      {
         Ejb3RegistrarLocator.locateRegistrar().bind(bindName, pojoNew);
      }
      // Expected
      catch (DuplicateBindException dbe)
      {
         return;
      }

      // Should not be reached
      TestCase.fail("Operation to bind into existing name in registry should fail with "
            + DuplicateBindException.class.getName());
   }

   @Test
   public void testRegistrarEmptyLookupFails() throws Throwable
   {
      // Initialize an unused name
      String bindName = "org.jboss.ejb3.unused";

      // Attempt to lookup
      try
      {
         Ejb3RegistrarLocator.locateRegistrar().lookup(bindName);
      }
      // Expected
      catch (NotBoundException nbe)
      {
         return;
      }

      // Should not be reached
      TestCase.fail("Attempt to lookup an unbound name should result in " + NotBoundException.class.getName());
   }

   @Test
   public void testRegistrarImmutableAfterBound() throws Throwable
   {
      try
      {
         // Attempt to bind a new registrar
         Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(null));
      }
      // Expected
      catch (DuplicateBindException dbe)
      {
         return;
      }

      // Should not be reached
      TestCase.fail("Attempts to rebind the " + Ejb3Registrar.class.getSimpleName()
            + " implementation should fail after already bound");
   }

}
