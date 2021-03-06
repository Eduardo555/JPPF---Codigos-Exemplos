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

package org.jppf.nio;

import org.slf4j.*;

/**
 * Context associated with an open socket channel.
 * @param <S> the type of states associated with this context.
 * @author Laurent Cohen
 */
public abstract class SimpleNioContext<S extends Enum<S>> extends AbstractNioContext<S>
{
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(SimpleNioContext.class);
  /**
   * Determines whether TRACE logging level is enabled.
   */
  private static boolean traceEnabled = log.isTraceEnabled();

  /**
   * Read data from a channel.
   * @param wrapper the channel to read the data from.
   * @return true if all the data has been read, false otherwise.
   * @throws Exception if an error occurs while reading the data.
   */
  @Override
  public boolean readMessage(final ChannelWrapper<?> wrapper) throws Exception
  {
    if (message == null) message = new BaseNioMessage(channel);
    return message.read();
  }

  /**
   * Write data to a channel.
   * @param wrapper the channel to write the data to.
   * @return true if all the data has been written, false otherwise.
   * @throws Exception if an error occurs while writing the data.
   */
  @Override
  public boolean writeMessage(final ChannelWrapper<?> wrapper) throws Exception
  {
    //if (message == null) message = new BaseNioMessage(sslHandler != null);
    return message.write();
  }
}
