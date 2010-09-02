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

import org.jboss.ejb3.ejbref.resolver.spi.EjbReference;
import org.jboss.ejb3.ejbref.resolver.spi.EjbReferenceResolver;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * Responsible for resolving jndi-name, of a {@link EjbReference}, through the use
 * of bean metadata. 
 * {@link EjbReferenceResolver}s can make use of this {@link MetaDataBasedEjbReferenceResolver} 
 * by passing in the appropriate bean metadata to the {@link #resolveEjb(EjbReference, JBossMetaData, ClassLoader)} 
 * method 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
// This ideally should have been a SPI, but since this interface has a dependency
// on the (bulky and to-be-replaced) JBMETA, it's better to keep this as an internal
// interface, till we have a SPI for EJB metadata
public interface MetaDataBasedEjbReferenceResolver
{

   /**
    * Resolves the jndi name of a {@link EjbReference} using the passed {@link JBossMetaData}
    * 
    * @param reference The {@link EjbReference} which needs to be resolved into a jndi name
    * @param jbossMetaData The metadata which will be used for resolving the jndi name
    * @param cl {@link ClassLoader} that can be used (if required) during the resolution 
    * @return Returns the resolved jndi-name for the passed {@link EjbReference}. If the jndi name
    *           cannot be resolved, then this method returns null
    */
   String resolveEjb(EjbReference reference, JBossMetaData jbossMetaData, ClassLoader cl);
}
