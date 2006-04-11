/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.transport.reliable;

/**
 * Listens to events on a {@link ReplayBuffer}
 * 
 * @version $Revision$
 */
public interface ReplayBufferListener {

    /**
     * Indications that the buffer has been discarded and so could be
     * re-introduced into some pool
     */
    public void onBufferDiscarded(int commandId, Object buffer);
}
