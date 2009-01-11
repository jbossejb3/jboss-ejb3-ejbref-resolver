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
package org.jboss.ejb3.common.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
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
import org.jboss.metadata.process.chain.ProcessorChain;
import org.jboss.metadata.process.chain.ejb.jboss.JBossMetaDataProcessorChain;
import org.jboss.metadata.process.processor.JBossMetaDataProcessor;
import org.jboss.metadata.process.processor.ejb.jboss.ClusterConfigDefaultValueProcessor;
import org.jboss.metadata.process.processor.ejb.jboss.JBossMetaDataValidatorChainProcessor;
import org.jboss.metadata.process.processor.ejb.jboss.SetDefaultLocalBusinessInterfaceProcessor;
import org.jboss.metadata.process.processor.ejb.jboss.SetExplicitLocalJndiNameProcessor;

/**
 * MetadataUtil
 * 
 * Contains helper methods central to EJB3 metadata
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MetadataUtil
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(MetadataUtil.class);

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Sole Constructor; in place to block instanciation
    */
   private MetadataUtil()
   {
   }

   // ------------------------------------------------------------------------------||
   // Helper Methods ---------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains the processors to be applied to metadata once merging is 
    * complete
    */
   @SuppressWarnings("unchecked")
   public static Collection<JBossMetaDataProcessor<JBossMetaData>> getPostMergeMetadataProcessors(ClassLoader cl)
   {
      // Initialize
      Collection<JBossMetaDataProcessor<JBossMetaData>> processors = new ArrayList<JBossMetaDataProcessor<JBossMetaData>>();

      /*
       * Add processors
       * 
       * Maintainer's note: The order here is preserved
       */

      // JBMETA-122 Implicit Local Business Interface
      processors.add(new SetDefaultLocalBusinessInterfaceProcessor<JBossMetaData>(cl));

      // JBMETA-133, EJBTHREE-1539 Default ClusterConfig
      processors.add(ClusterConfigDefaultValueProcessor.INSTANCE);

      // JBMETA-143 Set explicit local JNDI name from @LocalBinding.jndiBinding
      processors.add(SetExplicitLocalJndiNameProcessor.INSTANCE);

      // JBMETA-118 Validation
      processors.add(JBossMetaDataValidatorChainProcessor.INSTANCE);

      /*
       * End Processor Adding
       */

      // Return
      return processors;
   }

   /**
    * Obtains the ProcessorChain to be run upon a fully-merged
    * metadata
    * 
    * @param cl
    * @return
    */
   public static ProcessorChain<JBossMetaData> getPostMergeMetadataProcessorChain(ClassLoader cl)
   {
      // Initialize
      ProcessorChain<JBossMetaData> chain = new JBossMetaDataProcessorChain<JBossMetaData>();
      StringBuffer logMessage = new StringBuffer("Creating ");
      logMessage.append(ProcessorChain.class.getSimpleName());
      logMessage.append(" with the following Processors:");

      // Obtain processors to put in the chain
      Collection<JBossMetaDataProcessor<JBossMetaData>> processors = getPostMergeMetadataProcessors(cl);

      // For each of the processors
      if (processors != null)
      {
         for (JBossMetaDataProcessor<JBossMetaData> processor : processors)
         {
            // Add to the chain
            chain.addProcessor(processor);
            logMessage.append(" ");
            logMessage.append(processor);
         }
      }

      // Log
      log.debug(logMessage.toString());

      // Return
      return chain;
   }

   /**
    * Wraps all EJBs in the specified metadata with JNDI Resolution  
    * logic as determined by the specified policy
    * 
    * @param metadata
    */
   public static void decorateEjbsWithJndiPolicy(JBossMetaData md, ClassLoader cl)
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

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------||
   // ------------------------------------------------------------------------------||

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
