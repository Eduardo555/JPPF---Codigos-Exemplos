/*
 * JPPF.
 * Copyright (C) 2005-2014 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jppf.logging.jdk;

import java.io.IOException;
import java.util.logging.*;

/**
 * A handler that prints log messages using a {@link JPPFLogFormatter} instance.
 * @author Laurent Cohen
 * @exclude
*/
public class JPPFFileHandler extends FileHandler
{
  /**
   * The log formatter.
   */
  private JPPFLogFormatter jppfFormatter = new JPPFLogFormatter();

  /**
   * Initialize this Handler.
   * @throws IOException if any I/O error occurs.
   * @throws SecurityException if an operation is not allowed.
   */
  public JPPFFileHandler() throws IOException, SecurityException
  {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Formatter getFormatter()
  {
    return jppfFormatter;
  }
}
