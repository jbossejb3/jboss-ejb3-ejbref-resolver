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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.EJB30MetaDataBasedEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.FirstMatchEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child1Bean;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child1CommonBusiness;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child1LocalBusiness;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child1LocalHome;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child1RemoteBusiness;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child1RemoteHome;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child2And3CommonBusiness;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child2Bean;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child2LocalBusiness;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child3Bean;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.Child3LocalBusiness;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.ChildServiceBean;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.ChildServiceLocal;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.MockDeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.NestedChildBean;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.NestedChildLocalBusiness;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.ParentBean;
import org.jboss.ejb3.ejbref.resolver.ejb30.impl.test.ParentLocalBusiness;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.spi.NonDeterministicInterfaceException;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.BasicJndiBindingPolicy;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.JBossServicePolicyDecorator;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.JBossSessionPolicyDecorator;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.JbossEntityPolicyDecorator;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DefaultJndiBindingPolicy;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * EjbReferenceResolverUnitTestCaseBase
 * 
 * Test Cases to validate the pluggable EjbReferenceResolver
 * 
 * Uses DUs with the following structure:
 * 
 * Parent (Parent EJB)
 * |
 * |------ Child1 (Child EJB)
 * |         |
 * |         |------NestedChild (NestedChild EJB)
 * |
 * |------ Child2 (Child2 and Child3 EJBs)
 * 
 * ...where each DU has an EJB w/ Local Business interface.  
 * "Child1" has bean interfaces for 
 * local business, remote business, home, and local home.  "Child2" DU
 * has "child2" and "Child3" EJBs.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class EjbReferenceResolverUnitTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EjbReferenceResolverUnitTestCaseBase.class);

   protected static MockDeploymentUnit parentDu;

   protected static MockDeploymentUnit child1Du;

   protected static MockDeploymentUnit child2Du;

   protected static MockDeploymentUnit nestedChildDu;

   protected EjbReferenceResolver resolver;
   
   @Before
   public void before()
   {
      this.resolver = new FirstMatchEjbReferenceResolver();
      log.info("Using " + EjbReferenceResolver.class.getSimpleName() + ": " + resolver.getClass().getName());
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that the DeploymentUnit relationships are as expected for this
    * test.  In place to validate that the test setup is correct
    */
   @Test
   public void testDeploymentUnitRelationships() throws Throwable
   {
      // Parent should have 2 children
      TestCase.assertEquals("Parent DU does not have expected number of children", 3, parentDu.getChildren().size());

      // Children should have parent of parentDU
      TestCase.assertEquals("Child1 DU should have parent of Parent DU", parentDu, child1Du.getParent());
      TestCase.assertEquals("Child2 DU should have parent of Parent DU", parentDu, child2Du.getParent());

      // Tests for no children
      TestCase.assertTrue("Child2 DU should have no children", child2Du.getChildren() == null
            || child2Du.getChildren().size() == 0);
      TestCase.assertTrue("NestedChild DU should have no children", nestedChildDu.getChildren() == null
            || nestedChildDu.getChildren().size() == 0);

      // Parent should have one child (Nested Child)
      TestCase.assertEquals("Child1 DU should have 1 child", 1, child1Du.getChildren().size());
      TestCase.assertEquals("Child1 DU should have child of Nested Child", nestedChildDu, child1Du.getChildren()
            .iterator().next());

      // Nested Child should have parent of Child 1
      TestCase.assertEquals("Nested Child DU should have parent of Child1 DU", child1Du, nestedChildDu.getParent());
   }

   @Test
   public void testEJBTHREE2033()
   {
      EjbReference ref = new EjbReference(null, ChildServiceLocal.class.getName(), null);

      String jndiName = resolver.resolveEjb(parentDu, ref);
      String expectedJndiName = ChildServiceBean.class.getSimpleName() + "/local-" + ChildServiceLocal.class.getName();
      assertEquals("Unexpected jndi name",expectedJndiName, jndiName);
   }

   /**
    * Ensures that the parent can resolve itself and children by bean 
    * interface alone (business and homes)
    * 
    * @throws Throwable
    */
   @Test
   public void testParentCanResolveItselfAndChildrenByBeanInterface() throws Throwable
   {
      // Initialize
      String parentBeanInterfaceName = ParentLocalBusiness.class.getName();
      String child1LocalBusinessBeanInterfaceName = Child1LocalBusiness.class.getName();
      String child1RemoteBusinessBeanInterfaceName = Child1RemoteBusiness.class.getName();
      String child1LocalHomeBeanInterfaceName = Child1LocalHome.class.getName();
      String child1RemoteHomeBeanInterfaceName = Child1RemoteHome.class.getName();
      String child2BeanInterfaceName = Child2LocalBusiness.class.getName();
      String child3BeanInterfaceName = Child3LocalBusiness.class.getName();
      String nestedChildBeanInterfaceName = NestedChildLocalBusiness.class.getName();

      // Create references
      EjbReference parentReference = new EjbReference(null, parentBeanInterfaceName, null);
      EjbReference child1LocalBusinessReference = new EjbReference(null, child1LocalBusinessBeanInterfaceName, null);
      EjbReference child1RemoteBusinessReference = new EjbReference(null, child1RemoteBusinessBeanInterfaceName, null);
      EjbReference child1LocalHomeReference = new EjbReference(null, child1LocalHomeBeanInterfaceName, null);
      EjbReference child1RemoteHomeReference = new EjbReference(null, child1RemoteHomeBeanInterfaceName, null);
      EjbReference child2Reference = new EjbReference(null, child2BeanInterfaceName, null);
      EjbReference child3Reference = new EjbReference(null, child3BeanInterfaceName, null);
      EjbReference nestedChildReference = new EjbReference(null, nestedChildBeanInterfaceName, null);

      // Resolve
      DeploymentUnit fromDu = parentDu;
      String jndiNameParentLocalBusiness = resolver.resolveEjb(fromDu, parentReference);
      String jndiNameChild1LocalBusiness = resolver.resolveEjb(fromDu, child1LocalBusinessReference);
      log.debug("Resolved " + child1LocalBusinessReference + " from " + fromDu + " to " + jndiNameChild1LocalBusiness);
      String jndiNameChild1RemoteBusiness = resolver.resolveEjb(fromDu, child1RemoteBusinessReference);
      log
            .debug("Resolved " + child1RemoteBusinessReference + " from " + fromDu + " to "
                  + jndiNameChild1RemoteBusiness);
      String jndiNameChild1LocalHome = resolver.resolveEjb(fromDu, child1LocalHomeReference);
      log.debug("Resolved " + child1LocalHomeReference + " from " + fromDu + " to " + jndiNameChild1LocalHome);
      String jndiNameChild1RemoteHome = resolver.resolveEjb(fromDu, child1RemoteHomeReference);
      log.debug("Resolved " + child1RemoteHomeReference + " from " + fromDu + " to " + jndiNameChild1RemoteHome);
      String jndiNameChild2 = resolver.resolveEjb(fromDu, child2Reference);
      log.debug("Resolved " + child2Reference + " from " + fromDu + " to " + jndiNameChild2);
      String jndiNameChild3 = resolver.resolveEjb(fromDu, child3Reference);
      log.debug("Resolved " + child3Reference + " from " + fromDu + " to " + jndiNameChild3);
      String jndiNameNestedChild = resolver.resolveEjb(fromDu, nestedChildReference);
      log.debug("Resolved " + nestedChildReference + " from " + fromDu + " to " + jndiNameNestedChild);

      // Declare expected
      String expectedParent = ParentBean.class.getSimpleName() + "/local-" + parentBeanInterfaceName;
      String child1EjbName = Child1Bean.class.getSimpleName();
      String expectedChild1LocalBusiness = child1EjbName + "/local-" + child1LocalBusinessBeanInterfaceName;
      String expectedChild1RemoteBusiness = child1EjbName + "/remote-" + child1RemoteBusinessBeanInterfaceName;
      String expectedChild1LocalHome = child1EjbName + "/localHome";
      String expectedChild1RemoteHome = child1EjbName + "/home";
      String expectedChild2 = Child2Bean.class.getSimpleName() + "/local-" + child2BeanInterfaceName;
      String expectedChild3 = Child3Bean.class.getSimpleName() + "/local-" + child3BeanInterfaceName;
      String expectedNestedChild = NestedChildBean.class.getSimpleName() + "/local-" + nestedChildBeanInterfaceName;

      // Test
      TestCase.assertEquals(expectedParent, jndiNameParentLocalBusiness);
      TestCase.assertEquals(expectedChild1LocalBusiness, jndiNameChild1LocalBusiness);
      TestCase.assertEquals(expectedChild1RemoteBusiness, jndiNameChild1RemoteBusiness);
      TestCase.assertEquals(expectedChild1LocalHome, jndiNameChild1LocalHome);
      TestCase.assertEquals(expectedChild1RemoteHome, jndiNameChild1RemoteHome);
      TestCase.assertEquals(expectedChild2, jndiNameChild2);
      TestCase.assertEquals(expectedChild3, jndiNameChild3);
      TestCase.assertEquals(expectedNestedChild, jndiNameNestedChild);
   }

   /**
    * Ensures that a child can resolve a parent by bean interface
    * 
    * @throws Throwable
    */
   @Test
   public void testChildCanResolveParentByBeanInterface() throws Throwable
   {
      /*
       * In this case, "child1" is the parent of "nestedChild", so we refer to 
       * it as "parent"
       */

      // Initialize
      String parentLocalBusinessBeanInterfaceName = Child1LocalBusiness.class.getName();
      String parentRemoteBusinessBeanInterfaceName = Child1RemoteBusiness.class.getName();
      String parentLocalHomeBeanInterfaceName = Child1LocalHome.class.getName();
      String parentRemoteHomeBeanInterfaceName = Child1RemoteHome.class.getName();

      // Create references
      EjbReference parentLocalBusinessReference = new EjbReference(null, parentLocalBusinessBeanInterfaceName, null);
      EjbReference parentRemoteBusinessReference = new EjbReference(null, parentRemoteBusinessBeanInterfaceName, null);
      EjbReference parentLocalHomeReference = new EjbReference(null, parentLocalHomeBeanInterfaceName, null);
      EjbReference parentRemoteHomeReference = new EjbReference(null, parentRemoteHomeBeanInterfaceName, null);

      // Resolve
      DeploymentUnit fromDu = nestedChildDu;
      String jndiNameParentLocalBusiness = resolver.resolveEjb(fromDu, parentLocalBusinessReference);
      log.debug("Resolved " + parentLocalBusinessReference + " from " + fromDu + " to " + jndiNameParentLocalBusiness);
      String jndiNameParentRemoteBusiness = resolver.resolveEjb(fromDu, parentRemoteBusinessReference);
      log
            .debug("Resolved " + parentRemoteBusinessReference + " from " + fromDu + " to "
                  + jndiNameParentRemoteBusiness);
      String jndiNameParentLocalHome = resolver.resolveEjb(fromDu, parentLocalHomeReference);
      log.debug("Resolved " + parentLocalHomeReference + " from " + fromDu + " to " + jndiNameParentLocalHome);
      String jndiNameParentRemoteHome = resolver.resolveEjb(fromDu, parentRemoteHomeReference);
      log.debug("Resolved " + parentRemoteHomeReference + " from " + fromDu + " to " + jndiNameParentRemoteHome);

      // Declare expected
      String parentEjbName = Child1Bean.class.getSimpleName();
      String expectedParentLocalBusiness = parentEjbName + "/local-" + parentLocalBusinessBeanInterfaceName;
      String expectedParentRemoteBusiness = parentEjbName + "/remote-" + parentRemoteBusinessBeanInterfaceName;
      String expectedParentLocalHome = parentEjbName + "/localHome";
      String expectedParentRemoteHome = parentEjbName + "/home";

      // Test
      TestCase.assertEquals(expectedParentLocalBusiness, jndiNameParentLocalBusiness);
      TestCase.assertEquals(expectedParentRemoteBusiness, jndiNameParentRemoteBusiness);
      TestCase.assertEquals(expectedParentLocalHome, jndiNameParentLocalHome);
      TestCase.assertEquals(expectedParentRemoteHome, jndiNameParentRemoteHome);
   }

   /**
    * Ensures that looking up by a non-unique beanInterface
    * results in the expected exception 
    * 
    * @throws Throwable
    */
   @Test
   public void testExceptionOnNonDeterministicInterfaceReference() throws Throwable
   {
      // Initialize
      boolean exceptionReceived = false;
      String commonBeanInterfaceName = Child1CommonBusiness.class.getName();

      // Create reference
      EjbReference commonReference = new EjbReference(null, commonBeanInterfaceName, null);

      // Resolve
      DeploymentUnit fromDu = parentDu;
      try
      {
         resolver.resolveEjb(fromDu, commonReference);
      }
      // Expected
      catch (NonDeterministicInterfaceException ndie)
      {
         exceptionReceived = true;
         log.info("Got expected exception: " + ndie);
      }

      // Test
      TestCase.assertTrue("Expected exception was not received", exceptionReceived);

   }

   /**
    * Ensures that a NonDeterministicInterfaceException may be avoiding by specifying 
    * beanName in the reference
    * 
    * @throws Throwable
    */
   @Test
   public void testNonDeterministicExceptionAvoidedBySpecifyingBeanName() throws Throwable
   {
      // Initialize
      String beanName = Child3Bean.class.getSimpleName();
      String commonBeanInterfaceName = Child2And3CommonBusiness.class.getName();

      // Create reference (to explicit bean)
      EjbReference commonReference = new EjbReference(beanName, commonBeanInterfaceName, null);

      // Resolve
      DeploymentUnit fromDu = parentDu;
      String jndiName = resolver.resolveEjb(fromDu, commonReference);

      // Set expected
      String expected = beanName + "/local-" + Child3LocalBusiness.class.getName();

      // Test
      TestCase.assertEquals(expected, jndiName);
   }

   /**
    * Ensures that a reference honors mappedName above all else
    * 
    * @throws Throwable
    */
   @Test
   public void testMappedNameOverridesAllElse() throws Throwable
   {
      // Create a reference
      String mappedName = "ExplicitMappedName";
      String beanName = "IgnoredBeanName";
      String beanInterface = "IgnoredBeanInterface";
      EjbReference reference = new EjbReference(beanName, beanInterface, mappedName);

      // Resolve
      String resolved = resolver.resolveEjb(parentDu, reference);

      // Test
      TestCase.assertEquals("Use of mapped-name in EJB Reference should override all other properties", mappedName,
            resolved);

   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Exception
   {

      // Make an annotation finder
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);

      // Configure to scan the test EJBs
      Collection<Class<?>> parentClasses, child1Classes, child2Classes, nestedChildClasses;
      parentClasses = new ArrayList<Class<?>>();
      child1Classes = new ArrayList<Class<?>>();
      child2Classes = new ArrayList<Class<?>>();
      nestedChildClasses = new ArrayList<Class<?>>();
      parentClasses.add(ParentBean.class);
      child1Classes.add(Child1Bean.class);
      child2Classes.add(Child2Bean.class);
      child2Classes.add(Child3Bean.class); // Child2 DU has both Child2 and Child3 EJBs
      nestedChildClasses.add(NestedChildBean.class);

      // Make the metadata
      JBoss50MetaData parentMd = creator.create(parentClasses);
      JBoss50MetaData child1Md = creator.create(child1Classes);
      JBoss50MetaData child2Md = creator.create(child2Classes);
      JBoss50MetaData nestedChildMd = creator.create(nestedChildClasses);
      JBoss50MetaData serviceMd = creator.create(Arrays.<Class<?>> asList(ChildServiceBean.class));
      Collection<JBossMetaData> mds = new ArrayList<JBossMetaData>();
      mds.add(parentMd);
      mds.add(child1Md);
      mds.add(child2Md);
      mds.add(nestedChildMd);
      mds.add(serviceMd);

      // Decorate all EJBs w/ JNDI Policy
      for (JBossMetaData md : mds)
      {
         // Decorate
         decorateEjbsWithJndiPolicy(md, Thread.currentThread().getContextClassLoader());
      }

      // Parent DU
      parentDu = new MockDeploymentUnit("Parent");
      parentDu.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, parentMd);

      // Child1 DU
      child1Du = new MockDeploymentUnit("Child 1", parentDu);
      child1Du.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, child1Md);

      // Child1 DU
      child2Du = new MockDeploymentUnit("Child 2", parentDu);
      child2Du.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, child2Md);

      // Service Bean DU
      MockDeploymentUnit serviceDu = new MockDeploymentUnit("Child Service", parentDu);
      serviceDu.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, serviceMd);

      // Nested Child DU
      nestedChildDu = new MockDeploymentUnit("Nested Child", child1Du);
      nestedChildDu.addAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, nestedChildMd);

      // Set children of parents for bi-directional support
      parentDu.addChild(child1Du);
      parentDu.addChild(child2Du);
      parentDu.addChild(serviceDu);
      child1Du.addChild(nestedChildDu);


   }
   
   /**
    * Wraps all EJBs in the specified metadata with JNDI Resolution  
    * logic as determined by the specified policy
    * 
    * @param metadata
    */
   protected static void decorateEjbsWithJndiPolicy(JBossMetaData md, ClassLoader cl)
   {
      // Initialize Map of beans to replace
      Map<JBossEnterpriseBeanMetaData, JBossEnterpriseBeanMetaData> beansToReplace = new HashMap<JBossEnterpriseBeanMetaData, JBossEnterpriseBeanMetaData>();

      // Obtain defined JNDI Binding Policy
      String mdJndiPolicyName = md.getJndiBindingPolicy();
      if (mdJndiPolicyName != null && mdJndiPolicyName.trim().length() == 0)
      {
         mdJndiPolicyName = null;
      }
      if (mdJndiPolicyName != null)
      {
         log.debug(JBossMetaData.class.getSimpleName() + " " + md + " has defined "
               + DefaultJndiBindingPolicy.class.getSimpleName() + " \"" + mdJndiPolicyName + "\"");
      }

      // For each of the Enterprise Beans
      JBossEnterpriseBeansMetaData beans = md.getEnterpriseBeans();
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         // Initialize a decorated instance
         JBossEnterpriseBeanMetaData decoratedBean = null;

         // Obtain a Policy
         DefaultJndiBindingPolicy policy = getJndiBindingPolicy(bean, mdJndiPolicyName, cl);

         // If this is a Session Spec Bean (SLSB or SFSB)
         if (bean.isSession() && !bean.isService())
         {
            // Cast
            assert bean instanceof JBossSessionBeanMetaData : JBossEnterpriseBeanMetaData.class.getSimpleName()
                  + " representing as Session Bean is not castable to " + JBossSessionBeanMetaData.class.getName();
            JBossSessionBeanMetaData sessionBean = (JBossSessionBeanMetaData) bean;

            // Create a Session JNDI Policy Decorated Bean
            decoratedBean = new JBossSessionPolicyDecorator<JBossSessionBeanMetaData>(sessionBean, policy);
         }

         // If this is a @Service Bean
         if (bean.isService())
         {
            // Cast
            assert bean instanceof JBossServiceBeanMetaData : JBossEnterpriseBeanMetaData.class.getSimpleName()
                  + " representing as @Service Bean is not castable to " + JBossServiceBeanMetaData.class.getName();
            JBossServiceBeanMetaData serviceBean = (JBossServiceBeanMetaData) bean;

            // Create a @Service JNDI Policy Decorated Bean
            decoratedBean = new JBossServicePolicyDecorator(serviceBean, policy);
         }

         // If this is an Entity Bean
         if (bean.isEntity())
         {
            // Cast
            assert bean instanceof JBossEntityBeanMetaData : JBossEnterpriseBeanMetaData.class.getSimpleName()
                  + " representing as Entity Bean is not castable to " + JBossEntityBeanMetaData.class.getName();
            JBossEntityBeanMetaData entityBean = (JBossEntityBeanMetaData) bean;

            // Create a Entity JNDI Policy Decorated Bean
            decoratedBean = new JbossEntityPolicyDecorator(entityBean, policy);
         }

         // If we've decorated this bean, add to the map of beans to replace
         if (decoratedBean != null)
         {
            beansToReplace.put(bean, decoratedBean);
         }
      }

      // Replace with decorated beans
      for (JBossEnterpriseBeanMetaData beanToReplace : beansToReplace.keySet())
      {
         JBossEnterpriseBeanMetaData beanToReplaceWith = beansToReplace.get(beanToReplace);
         boolean removed = beans.remove(beanToReplace);
         assert removed : "Remove operation of " + beanToReplace + " from " + beans + " resulted in no action";
         beans.add(beanToReplaceWith);
         log.debug("Replaced " + beanToReplace.getEjbName() + " with decorated instance fit with "
               + DefaultJndiBindingPolicy.class.getSimpleName());
      }
   }

   /**
    * Obtains the JNDI Binding Policy instance to use for the specified metadata,
    * defaulting to a BasicJndiBindingPolicy if none is explicitly specified either in
    * the metadata itself or in its parent deployable unit
    * 
    * @param md The Bean Metadata
    * @param deployableUnitDefaultJndiPolicyClassName The (optional) JNDI Policy declared 
    *       by the deployable unit (JBossMetaData)
    * @param cl The Deployable Unit's ClassLoader
    */
   protected static DefaultJndiBindingPolicy getJndiBindingPolicy(JBossEnterpriseBeanMetaData md,
         String deployableUnitDefaultJndiPolicyClassName, ClassLoader cl)
   {
      // Initialize a JNDI Binding Policy
      DefaultJndiBindingPolicy policy = null;

      // Obtain JNDI Policy Name defined at the EJB level
      String beanJndiPolicyName = md.getJndiBindingPolicy();
      if (beanJndiPolicyName != null && beanJndiPolicyName.trim().length() == 0)
      {
         beanJndiPolicyName = null;
      }
      if (beanJndiPolicyName != null)
      {
         log.debug("Session EJB " + md.getEjbName() + " has defined " + DefaultJndiBindingPolicy.class.getSimpleName()
               + " of \"" + beanJndiPolicyName);
      }

      // Use JNDI Policy defined by MD, then override at bean level
      String jndiPolicyName = deployableUnitDefaultJndiPolicyClassName != null
            ? deployableUnitDefaultJndiPolicyClassName
            : beanJndiPolicyName;

      // If JNDI Policy is defined
      if (jndiPolicyName != null)
      {
         // Load the configured JNDI Binding Policy
         Class<?> policyClass = null;
         try
         {
            policyClass = Class.forName(jndiPolicyName, true, cl);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new RuntimeException("Could not find defined JNDI Binding Policy Class: " + jndiPolicyName, cnfe);
         }

         // Instanciate the configured JNDI Binding Policy
         try
         {
            policy = (DefaultJndiBindingPolicy) policyClass.newInstance();
         }
         catch (Throwable t)
         {
            throw new RuntimeException("Error in instanciating defined JNDI Binding Policy Class: " + jndiPolicyName, t);
         }

         // Log
         log.debug("Using " + DefaultJndiBindingPolicy.class.getSimpleName() + " \"" + policy.getClass().getName()
               + "\" for Session Bean " + md.getEjbName());
      }

      // If no JNDI Binding Policy was defined
      if (policy == null)
      {
         // Default to BasicJndiBindingPolicy
         policy = new BasicJndiBindingPolicy();
         log.debug("Defaulting to " + DefaultJndiBindingPolicy.class.getSimpleName() + " of \""
               + BasicJndiBindingPolicy.class.getName() + "\" for Session Bean " + md.getEjbName());
      }

      // Return
      return policy;
   }
   
   
   

}
