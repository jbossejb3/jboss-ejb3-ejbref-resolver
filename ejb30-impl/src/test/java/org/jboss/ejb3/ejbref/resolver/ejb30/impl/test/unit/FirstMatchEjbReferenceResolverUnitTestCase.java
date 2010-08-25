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
package org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.unit;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.FirstMatchEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.InterDuCommonBusiness;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * FirstMatchEjbReferenceResolverUnitTestCase
 * 
 * Test Cases to validate the EjbReferenceResolver 
 * implementation:  
 * FirstMatchEjbReferenceResolverUnitTestCase
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class FirstMatchEjbReferenceResolverUnitTestCase extends EjbReferenceResolverUnitTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(FirstMatchEjbReferenceResolverUnitTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that a non-deterministic reference across deployment units may be resolved.
    * This is due to the "first match" nature of this resolver, whereas other implementations
    * may throw an exception if detected that a reference is not unique within a deployment scope
    */
   @Test
   public void testCanResolveNondeterministicBeanInterfaceAcrossDeploymentUnits() throws Throwable
   {
      // Initialize
      String commonBeanInterfaceName = InterDuCommonBusiness.class.getName();

      // Create reference (to explicit bean)
      EjbReference commonReference = new EjbReference(null, commonBeanInterfaceName, null);

      // Resolve
      DeploymentUnit fromDu = parentDu;
      String jndiName = resolver.resolveEjb(fromDu, commonReference);

      // Test
      Assert.assertNotNull("Shoudld have been able to resolve non-unique reference", jndiName);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      // Do common init
      EjbReferenceResolverUnitTestCaseBase.beforeClass();

      // Set Resolver
      resolver = new FirstMatchEjbReferenceResolver();
      log.info("Using " + EjbReferenceResolver.class.getSimpleName() + ": " + resolver.getClass().getName());
   }
}
