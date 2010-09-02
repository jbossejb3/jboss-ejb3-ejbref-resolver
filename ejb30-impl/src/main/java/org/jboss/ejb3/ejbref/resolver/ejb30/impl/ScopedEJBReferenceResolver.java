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
package org.jboss.ejb3.ejbref.resolver.ejb30.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.ejb3.ejbref.resolver.spi.UnresolvableReferenceException;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ScopedEJBReferenceResolver implements EjbReferenceResolver
{

   private static Logger logger = Logger.getLogger(ScopedEJBReferenceResolver.class);
   
   /** The resolver which will be passed the bean metadata to resolve the jndi-name */
   protected MetaDataBasedEjbReferenceResolver metadataBasedEjbReferenceResolver;

   /**
    * Creates {@link ScopedEJBReferenceResolver} which will use {@link EJB30MetaDataBasedEjbReferenceResolver}
    */
   public ScopedEJBReferenceResolver()
   {
      this.metadataBasedEjbReferenceResolver = new EJB30MetaDataBasedEjbReferenceResolver();
   }

   protected String find(DeploymentUnit du, EjbReference reference)
   {
      JBossMetaData metadata = this.getMetaData(du);
      if (metadata == null)
      {
         return null;
      }
      return this.getMetaDataBasedEjbReferenceResolver().resolveEjb(reference, metadata, du.getClassLoader());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String resolveEjb(DeploymentUnit du, EjbReference reference) throws UnresolvableReferenceException
   {
      if (reference.getMappedName() != null && reference.getMappedName().isEmpty() == false)
      {
         logger.debug("Bypassing resolution, using mappedName of " + reference);
         return reference.getMappedName();
      }

      String jndiName = resolveWithinDeploymentUnit(du, new HashSet<DeploymentUnit>(), reference);
      if (jndiName == null)
      {
         throw new UnresolvableReferenceException("Could not resolve reference " + reference + " in " + du);
      }
      return jndiName;
   }

   /**
    * This method first tries to resolve the passed {@link EjbReference} in the passed <code>du</code>.
    * If the jndi name cannot be resolved in that {@link DeploymentUnit}, then it tries to <i>recursively</i> resolve the reference
    * in the child {@link DeploymentUnit}s of that {@link DeploymentUnit}. If the jndi-name still can't be resolved, then
    * this method recursively repeats the resolution steps with the parent of the passed {@link DeploymentUnit}
    * 
    * <p>
    *   If the jndi-name cannot be resolved in any of the {@link DeploymentUnit}s in the hierarchy, then this method
    *   returns null. Else it returns the resolved jndi-name.
    * </p>
    *  
    * @param du The deployment unit within which the {@link EjbReference} will be resolved
    * @param alreadyScannedDUs The {@link DeploymentUnit}s which have already been scanned for resolving the {@link EjbReference}
    * @param reference The {@link EjbReference} which is being resolved
    * @return Returns the jndi-name resolved out the {@link EjbReference}. If the jndi-name cannot be resolved, then this
    *           method returns null.
    */
   protected String resolveWithinDeploymentUnit(DeploymentUnit du, Collection<DeploymentUnit> alreadyScannedDUs,
         EjbReference reference)
   {
      // first find in the passed DU
      String jndiName = find(du, reference);
      // found, just return it
      if (jndiName != null)
      {
         return jndiName;
      }

      if (alreadyScannedDUs == null)
      {
         alreadyScannedDUs = new HashSet<DeploymentUnit>();
      }

      // jndi-name not resolved in the passed DU, so let's
      // check try resolving in its children DUs
      List<DeploymentUnit> children = du.getChildren();
      if (children != null)
      {
         for (DeploymentUnit child : children)
         {
            // already searched that one
            if (alreadyScannedDUs.contains(child))
            {
               continue;
            }
            // try resolving in this child DU
            jndiName = resolveWithinDeploymentUnit(child, alreadyScannedDUs, reference);
            // found in this child DU (or its nested child), return the jndi name
            if (jndiName != null)
            {
               return jndiName;
            }
            // add the child DU to the already scanned DU collection
            // so that we don't scan it again
            alreadyScannedDUs.add(child);
         }
      }

      // add this DU to the already scanned DU collection
      alreadyScannedDUs.add(du);

      // we haven't yet resolved the jndi-name, so let's
      // try resolving in our parent (and any of its children)
      DeploymentUnit parent = du.getParent();
      if (parent != null)
      {
         return resolveWithinDeploymentUnit(parent, alreadyScannedDUs, reference);
      }
      // couldn't resolve in the entire DU hierarchy, return null
      return null;
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
