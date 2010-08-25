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
package org.jboss.ejb3.ejbref.resolver.ejb31.impl.test.unit;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.common.deployers.spi.AttachmentNames;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.MockDeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.unit.FirstMatchEjbReferenceResolverUnitTestCase;
import org.jboss.ejb3.ejbref.resolver.ejb31.impl.FirstMatchEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb31.impl.test.EchoImpl;
import org.jboss.ejb3.ejbref.resolver.ejb31.impl.test.NoInterfaceSFSB;
import org.jboss.ejb3.ejbref.resolver.ejb31.impl.test.NoInterfaceSLSB;
import org.jboss.ejb3.ejbref.resolver.ejb31.impl.test.NoInterfaceSingletonBean;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.spi.UnresolvableReferenceException;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.process.processor.ejb.jboss.ImplicitNoInterfaceViewProcessor;
import org.junit.Before;
import org.junit.Test;

/**
 * FirstMatchEjbReferenceResolverTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class FirstMatchEjbReferenceResolverTestCase extends FirstMatchEjbReferenceResolverUnitTestCase
{

   private static Logger logger = Logger.getLogger(FirstMatchEjbReferenceResolverTestCase.class);

   @Override
   @Before
   public void before()
   {
      this.resolver = new FirstMatchEjbReferenceResolver();
      logger.info("Using " + EjbReferenceResolver.class.getSimpleName() + ": " + resolver.getClass().getName());
   }
   
   

   /**
    * Test that the jndi-name of a {@link EjbReference} to a no-interface view of a SLSB is resolved correctly
    */
   @Test
   public void testSLSBNoInterfaceEjbReference()
   {
      // Make an annotation finder
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);

      // Configure to scan the test EJBs
      Collection<Class<?>> nointerfaceSLSBClasses = new ArrayList<Class<?>>();
      nointerfaceSLSBClasses.add(NoInterfaceSLSB.class);

      Collection<Class<?>> someOtherClasses = new ArrayList<Class<?>>();
      someOtherClasses.add(EchoImpl.class);

      // Make the metadata
      JBossMetaData nointerfaceBeanMetaData = creator.create(nointerfaceSLSBClasses);
      JBossMetaData someOtherBeanMetaData = creator.create(someOtherClasses);

      // post-process the metadata
      nointerfaceBeanMetaData = this.runMetaDataPostProcessors(nointerfaceBeanMetaData);
      someOtherBeanMetaData = this.runMetaDataPostProcessors(someOtherBeanMetaData);

      // create a parent DU
      MockDeploymentUnit parentDU = new MockDeploymentUnit("Parent DU");
      // DU with no-interface view bean
      DeploymentUnit nointerfaceBeanDU = new MockDeploymentUnit("No-interface bean DU", parentDU);
      nointerfaceBeanDU.addAttachment(AttachmentNames.PROCESSED_METADATA, nointerfaceBeanMetaData);

      // the DU with some other bean
      DeploymentUnit someOtherBeanDU = new MockDeploymentUnit("Some other bean DU", parentDU);
      someOtherBeanDU.addAttachment(AttachmentNames.PROCESSED_METADATA, someOtherBeanMetaData);

      // Set children of parents for bi-directional support
      parentDU.addChild(nointerfaceBeanDU);
      parentDU.addChild(someOtherBeanDU);

      // Create reference to the no-interface view bean
      EjbReference nointerfaceEjbReference = new EjbReference(null, NoInterfaceSLSB.class.getName(), null);
      // resolve it from the no-interface view bean DU
      String nointerfaceViewJndiName = this.resolver.resolveEjb(nointerfaceBeanDU,
            nointerfaceEjbReference);

      // Test
      Assert.assertNotNull("Could not resolve jndi name for " + NoInterfaceSLSB.class.getName()
            + " no-interface view bean", nointerfaceViewJndiName);

      // now resolve the jndi name for the same reference from the other DUs.
      // Note that since there's only NoInterfaceSLSB bean in the entire DU hierarchy
      // we should always get back the same jndi name, irrespective of from which DU we start the resolution

      // resolve from some other bean DU
      String jndiNameResolvedFromSomeOtherDU = this.resolver.resolveEjb(someOtherBeanDU,
            nointerfaceEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + NoInterfaceSLSB.class.getName() + " no-interface view bean",
            nointerfaceViewJndiName, jndiNameResolvedFromSomeOtherDU);

      // resolve from parent DU
      String jndiNameResolvedFromParentDU = this.resolver.resolveEjb(parentDU,
            nointerfaceEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + NoInterfaceSLSB.class.getName() + " no-interface view bean",
            nointerfaceViewJndiName, jndiNameResolvedFromParentDU);

   }

   /**
    * Test that the jndi-name of a {@link EjbReference} to a no-interface view of a SFSB is resolved correctly
    */
   @Test
   public void testSFSBNoInterfaceEjbReference()
   {
      // Make an annotation finder
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);

      // Configure to scan the test EJBs
      Collection<Class<?>> nointerfaceSFSBClasses = new ArrayList<Class<?>>();
      nointerfaceSFSBClasses.add(NoInterfaceSFSB.class);

      Collection<Class<?>> someOtherClasses = new ArrayList<Class<?>>();
      someOtherClasses.add(EchoImpl.class);

      // Make the metadata
      JBossMetaData nointerfaceBeanMetaData = creator.create(nointerfaceSFSBClasses);
      JBossMetaData someOtherBeanMetaData = creator.create(someOtherClasses);

      // post-process the metadata
      nointerfaceBeanMetaData = this.runMetaDataPostProcessors(nointerfaceBeanMetaData);
      someOtherBeanMetaData = this.runMetaDataPostProcessors(someOtherBeanMetaData);

      // create a parent DU
      MockDeploymentUnit parentDU = new MockDeploymentUnit("Parent DU");
      // DU with no-interface view bean
      DeploymentUnit nointerfaceBeanDU = new MockDeploymentUnit("No-interface bean DU", parentDU);
      nointerfaceBeanDU.addAttachment(AttachmentNames.PROCESSED_METADATA, nointerfaceBeanMetaData);

      // the DU with some other bean
      DeploymentUnit someOtherBeanDU = new MockDeploymentUnit("Some other bean DU", parentDU);
      someOtherBeanDU.addAttachment(AttachmentNames.PROCESSED_METADATA, someOtherBeanMetaData);

      // Set children of parents for bi-directional support
      parentDU.addChild(nointerfaceBeanDU);
      parentDU.addChild(someOtherBeanDU);

      // Create reference to the no-interface view bean
      EjbReference nointerfaceEjbReference = new EjbReference(null, NoInterfaceSFSB.class.getName(), null);
      // resolve it from the no-interface view bean DU
      String nointerfaceViewJndiName = this.resolver.resolveEjb(nointerfaceBeanDU,
            nointerfaceEjbReference);

      // Test
      Assert.assertNotNull("Could not resolve jndi name for " + NoInterfaceSFSB.class.getName()
            + " no-interface view bean", nointerfaceViewJndiName);

      // now resolve the jndi name for the same reference from the other DUs.
      // Note that since there's only NoInterfaceSLSB bean in the entire DU hierarchy
      // we should always get back the same jndi name, irrespective of from which DU we start the resolution

      // resolve from some other bean DU
      String jndiNameResolvedFromSomeOtherDU = this.resolver.resolveEjb(someOtherBeanDU,
            nointerfaceEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + NoInterfaceSFSB.class.getName() + " no-interface view bean",
            nointerfaceViewJndiName, jndiNameResolvedFromSomeOtherDU);

      // resolve from parent DU
      String jndiNameResolvedFromParentDU = this.resolver.resolveEjb(parentDU,
            nointerfaceEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + NoInterfaceSFSB.class.getName() + " no-interface view bean",
            nointerfaceViewJndiName, jndiNameResolvedFromParentDU);

   }

   /**
    * Test that the jndi-name of a {@link EjbReference} to a no-interface view of a Singleton bean is resolved correctly
    */
   @Test
   public void testSingletonBeanNoInterfaceEjbReference()
   {
      // Make an annotation finder
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);

      // Configure to scan the test EJBs
      Collection<Class<?>> nointerfaceSingletonBeanClasses = new ArrayList<Class<?>>();
      nointerfaceSingletonBeanClasses.add(NoInterfaceSingletonBean.class);

      Collection<Class<?>> someOtherClasses = new ArrayList<Class<?>>();
      someOtherClasses.add(EchoImpl.class);

      // Make the metadata
      JBossMetaData nointerfaceBeanMetaData = creator.create(nointerfaceSingletonBeanClasses);
      JBossMetaData someOtherBeanMetaData = creator.create(someOtherClasses);

      // post-process the metadata
      nointerfaceBeanMetaData = this.runMetaDataPostProcessors(nointerfaceBeanMetaData);
      someOtherBeanMetaData = this.runMetaDataPostProcessors(someOtherBeanMetaData);

      // create a parent DU
      MockDeploymentUnit parentDU = new MockDeploymentUnit("Parent DU");
      // DU with no-interface view bean
      DeploymentUnit nointerfaceBeanDU = new MockDeploymentUnit("No-interface bean DU", parentDU);
      nointerfaceBeanDU.addAttachment(AttachmentNames.PROCESSED_METADATA, nointerfaceBeanMetaData);

      // the DU with some other bean
      DeploymentUnit someOtherBeanDU = new MockDeploymentUnit("Some other bean DU", parentDU);
      someOtherBeanDU.addAttachment(AttachmentNames.PROCESSED_METADATA, someOtherBeanMetaData);

      // Set children of parents for bi-directional support
      parentDU.addChild(nointerfaceBeanDU);
      parentDU.addChild(someOtherBeanDU);

      // Create reference to the no-interface view bean
      EjbReference nointerfaceEjbReference = new EjbReference(null, NoInterfaceSingletonBean.class.getName(), null);
      // resolve it from the no-interface view bean DU
      String nointerfaceViewJndiName = this.resolver.resolveEjb(nointerfaceBeanDU,
            nointerfaceEjbReference);

      // Test
      Assert.assertNotNull("Could not resolve jndi name for " + NoInterfaceSingletonBean.class.getName()
            + " no-interface view bean", nointerfaceViewJndiName);

      // now resolve the jndi name for the same reference from the other DUs.
      // Note that since there's only NoInterfaceSLSB bean in the entire DU hierarchy
      // we should always get back the same jndi name, irrespective of from which DU we start the resolution

      // resolve from some other bean DU
      String jndiNameResolvedFromSomeOtherDU = this.resolver.resolveEjb(someOtherBeanDU,
            nointerfaceEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + NoInterfaceSingletonBean.class.getName()
            + " no-interface view bean", nointerfaceViewJndiName, jndiNameResolvedFromSomeOtherDU);

      // resolve from parent DU
      String jndiNameResolvedFromParentDU = this.resolver.resolveEjb(parentDU,
            nointerfaceEjbReference);
      Assert.assertEquals("Unexpected jndi name for " + NoInterfaceSingletonBean.class.getName()
            + " no-interface view bean", nointerfaceViewJndiName, jndiNameResolvedFromParentDU);

   }

   /**
    * Tests that if a {@link EjbReference} has an explicit mapped-name specified, then the EJB reference
    * resolution returns that explicit mapped-name
    */
   @Test
   public void testExplicitMappedNameOnEjbReference()
   {
      final String explicitMappedName = "DummyExplicitJNDIName";

      // Make an annotation finder
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);

      // Configure to scan the test EJBs
      Collection<Class<?>> nointerfaceSingletonBeanClasses = new ArrayList<Class<?>>();
      nointerfaceSingletonBeanClasses.add(NoInterfaceSingletonBean.class);

      // Make the metadata
      JBossMetaData nointerfaceBeanMetaData = creator.create(nointerfaceSingletonBeanClasses);

      // post-process the metadata
      nointerfaceBeanMetaData = this.runMetaDataPostProcessors(nointerfaceBeanMetaData);

      // create a parent DU
      MockDeploymentUnit parentDU = new MockDeploymentUnit("Parent DU");
      // DU with no-interface view bean
      DeploymentUnit nointerfaceBeanDU = new MockDeploymentUnit("No-interface bean DU", parentDU);
      nointerfaceBeanDU.addAttachment(AttachmentNames.PROCESSED_METADATA, nointerfaceBeanMetaData);
      // Set children of parents for bi-directional support
      parentDU.addChild(nointerfaceBeanDU);

      // Create reference to the no-interface view bean
      EjbReference nointerfaceEjbReference = new EjbReference(null, NoInterfaceSingletonBean.class.getName(),
            explicitMappedName);
      // resolve it from the no-interface view bean DU
      String nointerfaceViewJndiName = this.resolver.resolveEjb(nointerfaceBeanDU,
            nointerfaceEjbReference);

      Assert.assertEquals(
            "Unexpected jndi name resolved for no-interface view of bean with explicit mapped-name in EjbReference",
            explicitMappedName, nointerfaceViewJndiName);
   }

   @Test
   public void testUnresolveableEjbReference()
   {

      // create a dummy DU
      MockDeploymentUnit nonEjbDU = new MockDeploymentUnit("Parent DU");
      EjbReference nonExistentEjbReference = new EjbReference(null, NoInterfaceSingletonBean.class.getName(), null);
      try
      {

         String nointerfaceViewJndiName = this.resolver.resolveEjb(nonEjbDU,
               nonExistentEjbReference);
         Assert.fail("EjbReference resolution was expected to fail, but returned jndi name " + nointerfaceViewJndiName);
      }
      catch (UnresolvableReferenceException urre)
      {
         // expected
         logger.debug("Caught the expected " + urre.getClass().getSimpleName() + " : ", urre);
      }

   }

   private JBossMetaData runMetaDataPostProcessors(JBossMetaData metadata)
   {
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      ImplicitNoInterfaceViewProcessor nointerfaceviewProcesser = new ImplicitNoInterfaceViewProcessor(tccl);
      return nointerfaceviewProcesser.process(metadata);
   }
}
