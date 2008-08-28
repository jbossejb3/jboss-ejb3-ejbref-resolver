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

import junit.framework.TestCase;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Ejb3McRegistrarTestCase
 * 
 * Test Cases for the Microcontainer
 * implementation of the Ejb3Registrar
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3McRegistrarTestCase extends Ejb3RegistrarTestCaseBase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static EmbeddedTestMcBootstrap bootstrap;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that an object bound with state other than "INSTALLED" may still be
    * unbound from MC via the Ejb3Registrar
    * 
    * EJBTHREE-1472
    */
   @Test
   public void testUnbindFromControllerStateOtherThanInstalled() throws Throwable
   {
      // Create a new key/value pair for binding into MC
      String name = "MyObject";
      Object value = new Object();

      // Construct BMDB, adding an unmet dependency
      BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder(name, value.getClass().getName());
      bmdb.addDependency("SomeDependencyThatDoesn'tExist");

      // Install into MC, though because of the unmet dependency will not reach "INSTALLED" state
      try
      {
         getBootstrap().getKernel().getController().install(bmdb.getBeanMetaData(), value);
      }
      catch (Throwable e)
      {
         throw new RuntimeException("Could not install at name \"" + name + "\" value " + value, e);
      }

      // Ensure that the install didn't completely succeed
      ControllerContext context = getBootstrap().getKernel().getController().getContext(name, null);
      TestCase.assertTrue("The test object should not be fully installed for this test", !context.getState().equals(
            ControllerState.INSTALLED));

      // Unbind
      Ejb3RegistrarLocator.locateRegistrar().unbind(name);

      // Check that we've unbound
      boolean isUnbound = false;
      try
      {
         Ejb3RegistrarLocator.locateRegistrar().lookup(name);
      }
      catch (NotBoundException nbe)
      {
         isUnbound = true;
      }
      TestCase.assertTrue("The test object should be unbound", isUnbound);

   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set a new MC Bootstrap
      Ejb3McRegistrarTestCase.setBootstrap(EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap());

      // Bind the Ejb3Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(Ejb3McRegistrarTestCase.getBootstrap().getKernel()));

      // Deploy
      Ejb3McRegistrarTestCase.bootstrap.deploy(Ejb3McRegistrarTestCase.class);
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Shutdown MC
      Ejb3McRegistrarTestCase.bootstrap.shutdown();

      // Set Bootstrap to null
      Ejb3McRegistrarTestCase.setBootstrap(null);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static EmbeddedTestMcBootstrap getBootstrap()
   {
      return Ejb3McRegistrarTestCase.bootstrap;
   }

   public static void setBootstrap(EmbeddedTestMcBootstrap bootstrap)
   {
      Ejb3McRegistrarTestCase.bootstrap = bootstrap;
   }

}
