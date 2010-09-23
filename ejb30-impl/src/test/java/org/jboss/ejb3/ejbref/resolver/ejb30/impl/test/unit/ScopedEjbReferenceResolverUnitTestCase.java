/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.jboss.deployers.structure.spi.DeploymentUnit;

import org.jboss.ejb3.ejbref.resolver.ejb30.impl.EJB30MetaDataBasedEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.ScopedEJBReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Echo;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.EchoBean;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.MockDeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.NotSoSimpleCalculator;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.SimpleCalculator;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.common.MetadataUtil;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that the {@link ScopedEJBReferenceResolver} functions as expected.
 * 
 * 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ScopedEjbReferenceResolverUnitTestCase extends EjbReferenceResolverUnitTestCaseBase
{

   private static Logger logger = Logger.getLogger(ScopedEjbReferenceResolverUnitTestCase.class);
   
   @Override
   @Before
   public void before()
   {
      this.resolver = new ScopedEJBReferenceResolver();
      logger.info("Using " + EjbReferenceResolver.class.getSimpleName() + ": " + resolver.getClass().getName());  
   }

   /**
    * Tests that the {@link ScopedEJBReferenceResolver} returns the expected 
    * jndi name when a Deployment unit consists more than 2 child deployment units.
    * 
    * This test is to make sure that the bug fix for https://jira.jboss.org/browse/EJBTHREE-2145 
    * works.
    * 
    * @throws Exception
    */
   @Test
   public void testParentDUWithMoreThanTwoChildDU() throws Exception
   {
      // Make an annotation finder
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);

      // Configure to scan the test EJBs
      Collection<Class<?>> echoBeanDUClasses = new ArrayList<Class<?>>();
      echoBeanDUClasses.add(EchoBean.class);

      Collection<Class<?>> childOneDUClasses = new ArrayList<Class<?>>();
      childOneDUClasses.add(SimpleCalculator.class);

      Collection<Class<?>> childTwoDUClasses = new ArrayList<Class<?>>();
      childTwoDUClasses.add(NotSoSimpleCalculator.class);

      // Make the metadata
      JBoss50MetaData echoBeanMetaData = creator.create(echoBeanDUClasses);
      JBoss50MetaData childOneBeanMetaData = creator.create(childOneDUClasses);
      JBoss50MetaData childTwoBeanMetaData = creator.create(childTwoDUClasses);

      // Decorate all EJBs w/ JNDI Policy
      MetadataUtil.decorateEjbsWithJndiPolicy(echoBeanMetaData, Thread.currentThread().getContextClassLoader());
      MetadataUtil.decorateEjbsWithJndiPolicy(childOneBeanMetaData, Thread.currentThread().getContextClassLoader());
      MetadataUtil.decorateEjbsWithJndiPolicy(childTwoBeanMetaData, Thread.currentThread().getContextClassLoader());

      // create a parent DU
      MockDeploymentUnit parentDU = new MockDeploymentUnit("Parent DU");
      // Child1 DU
      DeploymentUnit childOneDU = new MockDeploymentUnit("Child One DU", parentDU);
      childOneDU.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, childOneBeanMetaData);

      // Child2 DU
      DeploymentUnit childTwoDU = new MockDeploymentUnit("Child One DU", parentDU);
      childTwoDU.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, childTwoBeanMetaData);

      // the DU with the echo bean
      DeploymentUnit duWithEchoBean = new MockDeploymentUnit("DU With Echo bean", parentDU);
      duWithEchoBean.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, echoBeanMetaData);

      // Set children of parents for bi-directional support
      parentDU.addChild(childOneDU);
      parentDU.addChild(childTwoDU);
      parentDU.addChild(duWithEchoBean);

      // Create reference to the Echo bean
      EjbReference echoEjbReference = new EjbReference(null, Echo.class.getName(), null);
      // resolve it from the child1 DU
      String jndiNameResolvedFromChildOneDU = this.resolver.resolveEjb(childOneDU, echoEjbReference);

      // Test
      Assert.assertNotNull("Could not resolve jndi name for " + Echo.class.getName()
            + " business interface from child1 DU", jndiNameResolvedFromChildOneDU);

      // now resolve the jndi name for the same reference from the other DUs.
      // Note that since there's only one Echo business interface and bean in the entire DU hierarchy
      // we should always get back the same jndi name, irrespective of from which DU we start the resolution

      // resolve from child2 DU
      String jndiNameResolvedFromChildTwoDU = this.resolver.resolveEjb(childTwoDU, echoEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + Echo.class.getName() + " business interface from child2 DU",
            jndiNameResolvedFromChildOneDU, jndiNameResolvedFromChildTwoDU);

      // resolve from parent DU
      String jndiNameResolvedFromParentDU = this.resolver.resolveEjb(parentDU, echoEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + Echo.class.getName() + " business interface from parent DU",
            jndiNameResolvedFromChildOneDU, jndiNameResolvedFromParentDU);

      // resolve from the DU which has the EchoBean
      String jndiNameResolvedFromDUContainingEchoBean = this.resolver
            .resolveEjb(duWithEchoBean, echoEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + Echo.class.getName()
            + " business interface from the DU containing the EchoBean", jndiNameResolvedFromChildOneDU,
            jndiNameResolvedFromDUContainingEchoBean);

   }
}
