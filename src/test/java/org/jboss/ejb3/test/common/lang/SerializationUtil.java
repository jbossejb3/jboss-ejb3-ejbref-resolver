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
package org.jboss.ejb3.test.common.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 
 * SerializationUtil - Utility class for creating a copy of {@link Serializable} object
 * by serializing and de-serializing the object.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SerializationUtil
{

   /**
    * Creates a copy of the <code>originalObject</code> by serializing/de-serializing
    * the object.
    *  
    * @param originalObject The object whose copy will be created 
    * @return Returns a copy of the <code>originalObject</code>
    * 
    * @throws Exception
    */
   public static Serializable getCopy(Serializable originalObject) throws Exception
   {

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(originalObject);

      ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
      ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      Object copyOfObject = objectInputStream.readObject();

      return (Serializable) copyOfObject;
   }
}
