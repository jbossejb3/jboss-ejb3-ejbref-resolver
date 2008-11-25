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

import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
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

}
