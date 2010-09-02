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
package org.jboss.ejb3.ejbref.resolver.ejb31.impl;

import java.util.Collection;

import org.jboss.ejb3.ejbref.resolver.ejb30.impl.EJB30MetaDataBasedEjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndi.resolver.impl.JNDIPolicyBasedSessionBean31JNDINameResolver;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DefaultJndiBindingPolicy;

/**
 * Resolves the jndi name out of an {@link EjbReference} for EJB3 and EJB3.1 beans.
 * 
 * <p>
 *  This resolver takes into account EJB3.1 semantics including no-interface view 
 *  while resolving the jndi name from the {@link EjbReference}
 * </p>
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJB31MetaDataBasedEjbReferenceResolver extends EJB30MetaDataBasedEjbReferenceResolver
{

   /** Logger */
   private static Logger logger = Logger.getLogger(EJB31MetaDataBasedEjbReferenceResolver.class);

   /**
    * JNDI binding policy which will be used for resolving the jndi name
    */
   protected DefaultJndiBindingPolicy jndiBindingPolicy;

   /**
    * {@inheritDoc}
    * <p>
    * This method takes into account no-interface view of EJB3.1 beans and checks
    * whether the passed {@link EjbReference} represents a no-interface of a bean. If the
    * passed {@link EjbReference} represents a no-interface view of the passed session bean metadata
    * then this method returns true. Else it let's the {@link EJB30MetaDataBasedEjbReferenceResolver} do the
    * matching.
    * </p>
    */
   @Override
   protected boolean isMatch(EjbReference reference, JBossSessionBeanMetaData md, ClassLoader cl)
   {
      if (this.hasNoInterfaceView(md))
      {
         if (md.getEjbClass().equals(reference.getBeanInterface()))
         {
            return true;
         }
      }
      return super.isMatch(reference, md, cl);
   }

   /**
    * {@inheritDoc}
    * This method takes into account the no-interface view (if present) of the passed session bean
    * while determining the jndi name of the passed {@link EjbReference}
    */
   @Override
   protected String getJNDIName(EjbReference reference, JBossSessionBeanMetaData smd, ClassLoader cl)
   {

      // If mapped-name is specified, just use it
      String mappedName = reference.getMappedName();
      if (mappedName != null && mappedName.trim().length() > 0)
      {
         logger.debug("Bypassing resolution, using mappedName of " + reference);
         return mappedName;
      }

      // Get the bean interface name
      String interfaceName = reference.getBeanInterface();

      // Get eligible interfaces
      Collection<String> eligibleInterfaces = this.getEligibleBeanInterfaces(smd);

      // Ensure the bean interface name is directly declared in metadata
      if (!eligibleInterfaces.contains(interfaceName))
      {

         /*
          *  Not directly in metadata, so we've got to resolve this
          */
         logger.debug("Found specified beanInterface that is not a direct beanInterface of EJB " + smd.getEjbName()
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
               logger.debug("Resolved specified beanInterface " + interfaceName + " to " + eligibleInterface
                     + " for EJB " + smd.getEjbName());
               interfaceName = eligibleInterface;
               break;
            }
         }

      }

      // use a jndi name resolver
      JNDIPolicyBasedSessionBean31JNDINameResolver jndiNameResolver = this.getJNDINameResolver();
      // resolve
      String resolvedJndiName = jndiNameResolver.resolveJNDIName(smd, interfaceName);

      // return the resolved jndi name
      logger.debug("Resolved JNDI Name for " + reference + " of EJB " + smd.getEjbName() + ": " + resolvedJndiName);
      return resolvedJndiName;
   }

   /**
    * Sets the jndi binding policy which will be used to resolve the jndi name
    * 
    * @param jndiBindingPolicy
    */
   public void setJNDIBindingPolicy(DefaultJndiBindingPolicy jndiBindingPolicy)
   {
      this.jndiBindingPolicy = jndiBindingPolicy;
   }

   /**
    * Returns true if the passed session bean metadata represents a EJB3.1 bean
    * which exposes a no-interface view. Else returns false.
    * 
    * @param smd Session bean metadata
    * @return
    */
   protected boolean hasNoInterfaceView(JBossSessionBeanMetaData smd)
   {
      if (isEJB31(smd) == false)
      {
         return false;
      }
      if (smd instanceof JBossSessionBean31MetaData == false)
      {
         return false;
      }
      JBossSessionBean31MetaData sessionBean31 = (JBossSessionBean31MetaData) smd;
      return sessionBean31.isNoInterfaceBean();
   }

   /**
    * Returns true if the passed session bean metadata represents a EJB3.1 bean
    * @param smd Session bean metadata
    * @return
    */
   protected boolean isEJB31(JBossSessionBeanMetaData smd)
   {
      JBossMetaData jbossMetaData = smd.getJBossMetaData();
      return jbossMetaData.isEJB31();
   }

   /**
    * Returns a {@link JNDIPolicyBasedSessionBean31JNDINameResolver} to resolve jndi names
    * from metadata.
    * @return
    */
   private JNDIPolicyBasedSessionBean31JNDINameResolver getJNDINameResolver()
   {
      if (this.jndiBindingPolicy == null)
      {
         return new JNDIPolicyBasedSessionBean31JNDINameResolver();
      }
      return new JNDIPolicyBasedSessionBean31JNDINameResolver(this.jndiBindingPolicy);
   }

}
