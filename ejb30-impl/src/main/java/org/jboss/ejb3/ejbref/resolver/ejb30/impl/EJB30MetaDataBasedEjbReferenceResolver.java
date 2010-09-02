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
package org.jboss.ejb3.ejbref.resolver.ejb30.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.NonDeterministicInterfaceException;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.JbossSessionBeanJndiNameResolver;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;

/**
 * Resolves a jndi name from a {@link EjbReference} for EJB3.0 beans.
 * 
 * Note that this resolver has no knowledge of EJB3.1 beans and can't handle no-interface
 * views. For EJB3.1 beans, use org.jboss.ejb3.ejbref.resolver.ejb31.impl.EJB31MetaDataBasedEjbReferenceResolver
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EJB30MetaDataBasedEjbReferenceResolver implements MetaDataBasedEjbReferenceResolver
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EJB30MetaDataBasedEjbReferenceResolver.class);

   /**
    * The attachment name of the metadata within the DU
    */
   //public static final String DU_ATTACHMENT_NAME_METADATA = AttachmentNames.PROCESSED_METADATA;
   // Horrible hack!!! Should have relied on org.jboss.ejb3.common.deployers.spi.AttachmentNames.PROCESSED_METADATA
   // but that's going to bring in the jboss-ejb3-common dependency which is really unnecessary for just
   // this constant and also because it can lead to circular dependency because jboss-ejb3-common, going forward
   // would use this jboss-ejb3-ejbref-resolver
   // FIXME: The attachment name shouldn't be hardcoded here.
   public static final String DU_ATTACHMENT_NAME_METADATA = "processed." + JBossMetaData.class.getName();
   

   public String resolveEjb(EjbReference reference, JBossMetaData jbossMetaData, ClassLoader cl)
   {
      if (reference.getMappedName() != null && reference.getMappedName().isEmpty() == false)
      {
         log.debug("Bypassing resolution, using mappedName of " + reference);
         return reference.getMappedName();
      }
      
      JBossSessionBeanMetaData sessionBean = this.findSessionBean(reference, jbossMetaData, cl);
      if (sessionBean == null)
      {
         return null;
      }
      return this.getJNDIName(reference, sessionBean, cl);

   }


   protected JBossSessionBeanMetaData findSessionBean(EjbReference reference, JBossMetaData metadata, ClassLoader cl)
   {
      // Initialize
      log.debug("Resolving reference for " + reference + " in " + metadata);
      Collection<JBossSessionBeanMetaData> matches = new ArrayList<JBossSessionBeanMetaData>();

      // Get all Enterprise Beans contained in the metadata
      JBossEnterpriseBeansMetaData beans = metadata.getEnterpriseBeans();

      // Loop through all EJBs
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         // We only can inject Session Beans (Entity and MDB are not targets)
         if (!(bean instanceof JBossSessionBeanMetaData))
         {
            continue;
         }

         // Cast our Session Bean
         JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData) bean;

         // See if this is a match
         if (this.isMatch(reference, smd, cl))
         {
            // Add to the matches found
            matches.add(smd);
            log.debug("Found match in EJB " + smd.getEjbName() + " for " + reference);
            continue;
         }
      }

      // Ensure we've only got one match
      if (matches.size() > 1)
      {
         // If more than one match was found while EJB name was specified, there's a problem in resolution
         String beanName = reference.getBeanName();
         assert beanName == null || beanName.trim().length() == 0 : "Error in resolution logic, more than one eligible EJB "
               + "was found to satisfy beanInterface "
               + reference.getBeanInterface()
               + ", but EJB Name was explicitly-specified.";

         // Report error
         throw new NonDeterministicInterfaceException("Specified reference " + reference
               + " was matched by more than one EJB: " + matches
               + ".  Specify beanName explciitly or ensure beanInterface is unique.");
      }

      // Return the JNDI name of the matching metadata if present, otherwise null
      return matches.size() > 0 ? matches.iterator().next() : null;

   }

   protected String getJNDIName(EjbReference reference, JBossSessionBeanMetaData metadata, ClassLoader cl)
   {
      // If mapped-name is specified, just use it
      String mappedName = reference.getMappedName();
      if (mappedName != null && mappedName.trim().length() > 0)
      {
         log.debug("Bypassing resolution, using mappedName of " + reference);
         return mappedName;
      }

      // Get the bean interface name
      String interfaceName = this.getBeanInterfaceName(reference, cl);

      // Get eligible interfaces
      Collection<String> eligibleInterfaces = this.getEligibleBeanInterfaces(metadata);

      // Ensure the bean interface name is directly declared in metadata
      if (!eligibleInterfaces.contains(interfaceName))
      {

         /*
          *  Not directly in metadata, so we've got to resolve this
          */
         log.debug("Found specified beanInterface that is not a direct beanInterface of EJB " + metadata.getEjbName()
               + ": " + interfaceName);

         // Loop through eligible interfaces
         for (String eligibleInterface : eligibleInterfaces)
         {
            // Get the parents of the eligible interface
            Collection<String> parents = this.getAllParentInterfaces(eligibleInterface, cl);
            // If the specified interface name if a parent of this eligible interface
            if (parents.contains(interfaceName))
            {
               // Set the interface name to the resolved
               log.debug("Resolved specified beanInterface " + interfaceName + " to " + eligibleInterface + " for EJB "
                     + metadata.getEjbName());
               interfaceName = eligibleInterface;
               break;
            }
         }

      }

      // Return 
      String resolvedJndiName = JbossSessionBeanJndiNameResolver.resolveJndiName(metadata, interfaceName);
      log.debug("Resolved JNDI Name for " + reference + " of EJB " + metadata.getEjbName() + ": " + resolvedJndiName);
      return resolvedJndiName;
   }

   /**
    * Determines whether the specified session bean is a match for the specified
    * reference
    * 
    * @param reference
    * @param md
    * @param cl The ClassLoader for the specified metadata
    * @return
    */
   protected boolean isMatch(EjbReference reference, JBossSessionBeanMetaData md, ClassLoader cl)
   {
      // Initialize
      List<String> interfaces = new ArrayList<String>();

      // Add all eligible bean interfaces
      interfaces.addAll(this.getAllParentInterfaces(this.getEligibleBeanInterfaces(md), cl));

      // Get the requested bean interface 
      String requestedInterface = reference.getBeanInterface();
      assert requestedInterface != null && requestedInterface.trim().length() > 0 : "beanInterface must be specified";

      // Does this EJB have the requested interface?
      if (interfaces.contains(requestedInterface))
      {
         /*
          * Check that the interface is unique to this EJB
          */
         boolean found = false;
         for (String interfaze : interfaces)
         {
            boolean equal = interfaze.equals(requestedInterface);
            if (equal && !found)
            {
               found = true;
            }
            else if (equal && found)
            {
               throw new NonDeterministicInterfaceException("beanInterface specified, " + interfaze
                     + ", is not unique within EJB " + md.getEjbName());
            }
         }

         // Get the requested EJB name
         String ejbName = reference.getBeanName();

         // If the EJB name is explicitly-provided
         if (ejbName != null && ejbName.trim().length() > 0)
         {
            // Ensure the EJB name matches this EJB
            if (!ejbName.equals(md.getEjbName()))
            {
               return false;
            }
         }

         // We've got a match
         return true;
      }

      // No preconditions met, return false
      return false;
   }

   /**
    * Returns a Collection containing the union of the interface names specified
    * as well as all all parent interfaces
    * 
    * @param interfaceNames
    * @param cl
    * @return
    */
   protected Collection<String> getAllParentInterfaces(Collection<String> interfaceNames, ClassLoader cl)
   {
      // Initialize
      Collection<String> interfaces = new ArrayList<String>();

      // Go through all interface names
      for (String interfaceName : interfaceNames)
      {
         // Add this
         interfaces.addAll(this.getAllParentInterfaces(interfaceName, cl));
      }

      // Return
      return interfaces;
   }

   protected Collection<String> getAllParentInterfaces(String interfaceName, ClassLoader cl)
   {
      // Initialize
      Collection<String> interfaces = new ArrayList<String>();
      interfaces.add(interfaceName);

      // Load this interface, so we can get the parent interfaces
      try
      {
         Class<?> interfaze = Class.forName(interfaceName, false, cl);
         Class<?>[] parentInterfaces = interfaze.getInterfaces();
         for (Class<?> parentInterface : parentInterfaces)
         {
            // Get the parent interface name
            String parentInterfaceName = parentInterface.getName();

            // Get the parents of the parent
            Collection<String> grandParents = this.getAllParentInterfaces(parentInterfaceName, cl);
            if (grandParents.size() > 0)
            {
               interfaces.addAll(grandParents);
            }

            // Add the parent interface name
            if (!interfaces.contains(parentInterfaceName))
            {
               interfaces.add(parentInterfaceName);
            }
         }
      }
      catch (ClassNotFoundException cnfe)
      {
         throw new RuntimeException("Could not load class from specified ClassLoader " + cl, cnfe);
      }

      // Return
      return interfaces;
   }

   /**
    * Obtains all interfaces declared by the metadata
    * that are eligible for "beanInterface" inclusion
    * (local business, remote business, local home, home) 
    * 
    * @param smd
    * @return
    */
   protected Collection<String> getEligibleBeanInterfaces(JBossSessionBeanMetaData smd)
   {
      Collection<String> interfaces = new ArrayList<String>();

      // Add all eligible bean interfaces
      BusinessLocalsMetaData businessLocals = smd.getBusinessLocals();
      BusinessRemotesMetaData businessRemotes = smd.getBusinessRemotes();
      String home = smd.getHome();
      String localHome = smd.getLocalHome();
      if (businessLocals != null)
      {
         interfaces.addAll(businessLocals);
      }
      if (businessRemotes != null)
      {
         interfaces.addAll(businessRemotes);
      }
      if (home != null && home.trim().length() > 0)
      {
         interfaces.add(home);
      }
      if (localHome != null && localHome.trim().length() > 0)
      {
         interfaces.add(localHome);
      }

      // Return
      return interfaces;
   }

   /**
    * Returns the bean interface described by the specified
    * reference, validating its presence along the way
    * 
    * @param reference
    * @param cl
    * @return
    */
   private String getBeanInterfaceName(EjbReference reference, ClassLoader cl)
   {
      // Get the bean interface
      String interfaceName = reference.getBeanInterface();
      assert interfaceName != null && interfaceName.trim().length() > 0 : "beanInterface must be specified";

      // Return
      return interfaceName;

   }

}
