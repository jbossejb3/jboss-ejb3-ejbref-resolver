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

import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.spi.UnresolvableReferenceException;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * FirstMatchEjbReferenceResolver
 * 
 * An EJB Reference resolver which will return the first match 
 * found.  In cases where the reference may be non-deterministic 
 * this implementation will stop when the reference requirements
 * are satisfied
 * 
 * NonDeterministicInterfaceException is therefore avoided 
 * when a common interface is used across DeploymentUnits
 * in the same DU Hierarchy; very simply the first DU
 * which is able to resolve the reference (without conflicting 
 * with possible references to other EJBs 
 * within that DU) will be used.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class FirstMatchEjbReferenceResolver implements EjbReferenceResolver
{

   private static Logger logger = Logger.getLogger(FirstMatchEjbReferenceResolver.class);
   
   /** The resolver which will be passed the bean metadata to resolve the jndi-name */
   protected MetaDataBasedEjbReferenceResolver metadataBasedEjbReferenceResolver;
   
   /**
    * Creates {@link FirstMatchEjbReferenceResolver} which will use {@link EJB30MetaDataBasedEjbReferenceResolver}
    */
   public FirstMatchEjbReferenceResolver()
   {
      this.metadataBasedEjbReferenceResolver = new EJB30MetaDataBasedEjbReferenceResolver();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String resolveEjb(DeploymentUnit du, EjbReference reference)
   {
      // If mapped-name is specified, just use it
      String mappedName = reference.getMappedName();
      if (mappedName != null && mappedName.trim().length() > 0)
      {
         logger.debug("Bypassing resolution, using mappedName of " + reference);
         return mappedName;
      }

      // Initialize
      String jndiName = null;

      // Resolve from the root deployment
      DeploymentUnit root = du.getTopLevel();
      jndiName = this.resolveEjbFromRoot(root, reference);

      // Check that we could resolve
      if (jndiName == null)
      {
         throw new UnresolvableReferenceException("Could not resolve reference " + reference + " for "
               + DeploymentUnit.class.getSimpleName() + " " + du);
      }

      // Return
      return jndiName;
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Follows a preorder traversal scheme to resolve the specified reference from the
    * specified metadata (or its children).  Returns the resolved target JNDI name. 
    * 
    * @param rootDu
    * @param reference
    * @return
    */
   protected String resolveEjbFromRoot(DeploymentUnit rootDu, EjbReference reference)
   {
      // Initialize
      String jndiName = null;

      // Obtain the metadata for this DU
      JBossMetaData metadata = this.getMetaData(rootDu);

      // Ensure metadata's specified for this DU
      if (metadata != null)
      {
         // Look for a match within this metadata
         jndiName = this.getMetaDataBasedEjbReferenceResolver().resolveEjb(reference, metadata, rootDu.getClassLoader());

      }

      // If we haven't found the JNDI name in this DU
      if (jndiName == null)
      {
         // Look to the children
         List<DeploymentUnit> children = rootDu.getChildren();

         // If we've got child deployments
         if (children != null)
         {
            // Loop through them
            for (DeploymentUnit child : children)
            {
               // Try to get the resolved JNDI name from the child
               jndiName = this.resolveEjbFromRoot(child, reference);

               // If found
               if (jndiName != null)
               {
                  // Break out 
                  break;
               }
            }
         }
      }

      // Return the JNDI Name
      return jndiName;
   }

   /**
    * Obtains the metadata attachment from the specified deployment unit, returning
    * null if not present
    * 
    * @param du
    * @return
    */
   protected JBossMetaData getMetaData(DeploymentUnit du)
   {
      return du.getAttachment(EJB30MetaDataBasedEjbReferenceResolver.DU_ATTACHMENT_NAME_METADATA, JBossMetaData.class);
   }

   /**
    * Returns the {@link MetaDataBasedEjbReferenceResolver} which this {@link EjbReferenceResolver} uses
    * @return
    */
   protected MetaDataBasedEjbReferenceResolver getMetaDataBasedEjbReferenceResolver()
   {
      return this.metadataBasedEjbReferenceResolver;
   }
}
