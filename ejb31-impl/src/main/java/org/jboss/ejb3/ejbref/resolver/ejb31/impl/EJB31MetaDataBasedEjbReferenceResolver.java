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

/**
 * EJB31MetaDataBasedEjbReferenceResolver
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJB31MetaDataBasedEjbReferenceResolver extends EJB30MetaDataBasedEjbReferenceResolver
{

   private static Logger logger = Logger.getLogger(EJB31MetaDataBasedEjbReferenceResolver.class);

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

      // Return
      JNDIPolicyBasedSessionBean31JNDINameResolver jndiNameResolver = new JNDIPolicyBasedSessionBean31JNDINameResolver();
      String resolvedJndiName = jndiNameResolver.resolveJNDIName(smd, interfaceName);
      logger.debug("Resolved JNDI Name for " + reference + " of EJB " + smd.getEjbName() + ": " + resolvedJndiName);
      return resolvedJndiName;
   }

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

   protected boolean isEJB31(JBossSessionBeanMetaData smd)
   {
      JBossMetaData jbossMetaData = smd.getJBossMetaData();
      return jbossMetaData.isEJB31();
   }
}
