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
package org.jboss.ejb3.common.deployers.spi;

import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * Ejb3DeployerUtils
 * 
 * A Set of Utilities to assist w/ EJB3 tasks related
 * to VDF
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3DeployerUtils
{
   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * MC Bind Name of the Main Deployer
    */
   private static final String MC_BEAN_NAME_MAIN_DEPLOYER = "MainDeployer";

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Internal Constructor, to prevent instanciation
    */
   private Ejb3DeployerUtils()
   {
      // Leave intact and private
   }

   // ------------------------------------------------------------------------------||
   // Utility Methods --------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains all EJB3 Deployment Units registered w/ the Main Deployer
    */
   public static Set<DeploymentUnit> getAllEjb3DeploymentUnitsInMainDeployer()
   {
      // Initialize
      Set<DeploymentUnit> deploymentUnits = new HashSet<DeploymentUnit>();

      // Get at the MainDeployer
      Object mainDeployer = Ejb3RegistrarLocator.locateRegistrar().lookup(MC_BEAN_NAME_MAIN_DEPLOYER);
      assert mainDeployer instanceof DeployerClient && mainDeployer instanceof MainDeployerStructure : "Obtained Main Deployer is not of expected type";
      DeployerClient dc = (DeployerClient) mainDeployer;
      MainDeployerStructure mds = (MainDeployerStructure) mainDeployer;

      // Loop through each Deployment
      for (Deployment d : dc.getTopLevel())
      {
         // Get the associated DU
         DeploymentUnit du = mds.getDeploymentUnit(d.getName());

         // Ensure it's an EJB3 DU (by looking for the metadata)
         JBossMetaData metadata = du.getAttachment(JBossMetaData.class);
         if (metadata != null && metadata.isEJB3x())
         {
            // Add to the set
            deploymentUnits.add(du);
         }
      }

      // Return
      return deploymentUnits;
   }

}
