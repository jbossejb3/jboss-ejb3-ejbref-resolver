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
package org.jboss.ejb3.common.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * RedirectProcessOutputThread
 * 
 * Captures output from a Process and redirects to a 
 * specified PrintStream
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class RedirectProcessOutputThread extends Thread implements Runnable
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The Process whose output to Capture
    */
   private Process process;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param process The Process whose output to capture
    */
   public RedirectProcessOutputThread(Process process)
   {
      this.process = process;
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public void run()
   {
      // Call Super
      super.run();

      // Initialize
      int bytesRead = 0;
      byte[] buffer = new byte[1024];

      // Obtain InputStream of process
      InputStream in = this.process.getInputStream();

      // Read in and direct PrintStream
      try
      {
         while ((bytesRead = in.read(buffer)) != -1)
         {
            this.getPrintStream().write(buffer, 0, bytesRead);
         }
      }
      catch (IOException ioe)
      {
         throw new RuntimeException(ioe);
      }
   }

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the PrintStream to which output should be
    * redirected
    */
   protected abstract PrintStream getPrintStream();
}
