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
package org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.ejbthree2176.unit;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.ejb3.ejbref.resolver.ejb30.impl.EJB30MetaDataBasedEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.FirstMatchEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.MockDeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.common.MetadataUtil;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.spi.NonDeterministicInterfaceException;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Test case for bug reported in EJBTHREE-2176 https://jira.jboss.org/browse/EJBTHREE-2176.
 * 
 * <p>
 *  The resolvers used to throw a spurious {@link NonDeterministicInterfaceException} exception
 *  during the resolution for the following case:
 *  <pre>
 *      @Stateless
 *      @Remote (SomeBeanRemote.class)
 *      @Local (SomeBeanLocal.class)
 *      public class SomeBean implements SomeBeanLocal, SomeBeanRemote
 *      {
 *      ...
 *      }
 *      
 *      public interface SomeBeanRemote extends SomeBeanLocal
 *      {
 *          ...
 *      }
 *      
 *      public interface SomeBeanLocal
 *      {
 *      ...
 *      }
 *      
 *  </pre>
 *  
 *  The spurious exception would be thrown when a {@link EjbReference} was created for <code>SomeBeanLocal</code>:
 *  
 *  <pre>
 *      @EJB
 *      private SomeBeanLocal bean;
 *  </pre>
 * 
 *  
 * </p>
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBTHREE2176UnitTestCase
{

   /**
    * Test the bug fix for EJBTHREE-2176
    */
   @Test
   public void testResolutionForBeanWithDuplicateInterfaceDeclarations()
   {
      // Make an annotation finder
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);

      // Configure to scan the test EJBs
      Collection<Class<?>> classes = new ArrayList<Class<?>>();
      classes.add(SimpleSLSB.class);
      
      // Make the metadata
      JBossMetaData jbossMetaData = creator.create(classes);
      // decorate with jndi policy
      MetadataUtil.decorateEjbsWithJndiPolicy(jbossMetaData, Thread.currentThread().getContextClassLoader());
      
      // create a DU
      MockDeploymentUnit deploymentUnit = new MockDeploymentUnit("EJBTHREE-2176 DU");
      // add metadata to DU
      deploymentUnit.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, jbossMetaData);

      // create the reference
      String businessInterface = SimpleSLSBLocal.class.getName();
      EjbReference ejbRef = new EjbReference(null, businessInterface, null);

      // Resolve
      EjbReferenceResolver resolver = new FirstMatchEjbReferenceResolver();
      String jndiName = resolver.resolveEjb(deploymentUnit, ejbRef);

      // Test
      Assert.assertNotNull("Should have been able to resolve jndi name for reference " + ejbRef, jndiName);

   }
}
