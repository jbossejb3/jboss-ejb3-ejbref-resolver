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
package org.jboss.ejb3.ejbref.resolver.ejb30.impl.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;

/**
 * A Mock DeploymentUnit with support to:
 * 
 * - Add attachments
 * - Manage the parent/child relationship
 * - toString()
 * - Get the ClassLoader
 */
public class MockDeploymentUnit extends AbstractDeploymentUnit implements DeploymentUnit
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private String name;

   private DeploymentUnit parent;

   private List<DeploymentUnit> children;

   private Map<String, Object> attachments;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public MockDeploymentUnit(String name)
   {
      this.name = name;
      this.children = new ArrayList<DeploymentUnit>();
      this.attachments = new HashMap<String, Object>();
   }

   public MockDeploymentUnit(String name, DeploymentUnit parent)
   {
      this(name);
      this.parent = parent;
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public void addChild(DeploymentUnit child)
   {
      this.children.add(child);
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public List<DeploymentUnit> getChildren()
   {
      return this.children;
   }

   @Override
   public DeploymentUnit getParent()
   {
      return this.parent;
   }

   @Override
   public Object addAttachment(String name, Object attachment)
   {
      return this.attachments.put(name, attachment);
   }

   @Override
   public Object getAttachment(String name)
   {
      return this.attachments.get(name);
   }

   @Override
   public Map<String, Object> getAttachments()
   {
      return Collections.unmodifiableMap(this.attachments);
   }

   @Override
   public String toString()
   {
      return this.getClass().getName() + ": " + this.name;
   }

   @Override
   public ClassLoader getClassLoader()
   {
      return Thread.currentThread().getContextClassLoader();
   }

   @Override
   public DeploymentUnit getTopLevel()
   {
      // if this is the top most level, then it won't have a parent,
      // so return this deployment unit as the top most deployment unit
      if (parent == null)
      {
         return this;
      }
      // this is not the top most level, so let's go to parent and 
      // keep traversing till the top most level
      return parent.getTopLevel();
   }
}