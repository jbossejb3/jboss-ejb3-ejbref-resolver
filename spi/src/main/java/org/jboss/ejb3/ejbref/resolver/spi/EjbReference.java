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
package org.jboss.ejb3.ejbref.resolver.spi;

import java.io.Serializable;

/**
 * EjbReference
 * 
 * Models an EJB reference denoted by either javax.ejb.EJB or
 * its XML equivalent
 * 
 * Encapsulates descriptions required to resolve an @EJB reference
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EjbReference implements Serializable
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The name of the target EJB
    */
   private String beanName;

   /**
    * The fully-qualified name of the target interface (EJB 3.x Business or EJB 2.x Home) 
    */
   private String beanInterface;

   /**
    * The mapped-name used for the target
    */
   private String mappedName;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Sole Constructor
    */
   public EjbReference(String beanName, String interfaceFqn, String mappedName)
   {
      // Ensure at least one of the requisite properties is specified
      assert (beanName != null && beanName.trim().length() > 0)
            || (interfaceFqn != null && interfaceFqn.trim().length() > 0)
            || (mappedName != null && mappedName.trim().length() > 0) : "At least one of beanName, mappedName, or interfaceFqn must be specified";

      // Set properties
      this.setBeanName(beanName);
      this.setBeanInterface(interfaceFqn);
      this.setMappedName(mappedName);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public String getBeanName()
   {
      return beanName;
   }

   protected void setBeanName(String beanName)
   {
      this.beanName = beanName;
   }

   public String getBeanInterface()
   {
      return beanInterface;
   }

   protected void setBeanInterface(String beanInterface)
   {
      this.beanInterface = beanInterface;
   }

   public String getMappedName()
   {
      return mappedName;
   }

   protected void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public String toString()
   {
      // Initialize
      StringBuffer buffer = new StringBuffer();

      // Construct
      buffer.append("[EJB Reference: beanInterface '");
      buffer.append(this.getBeanInterface());
      buffer.append("', beanName '");
      buffer.append(this.getBeanName());
      buffer.append("', mappedName '");
      buffer.append(this.getMappedName());
      buffer.append("']");

      // Return
      return buffer.toString();
   }

}
