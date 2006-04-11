/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.activemq.kaha;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *Marshaller for marshalling in/out objects to a RawContainer
 * 
 * @version $Revision: 1.2 $
 */
public interface Marshaller {
    
       
    /**
     * Write the payload of this entry to the RawContainer
     * @param object 
     * @param dataOut
     * @throws IOException
     */
    public void writePayload(Object object, DataOutputStream dataOut) throws IOException;
    
    
    /**
     * Read the entry from the RawContainer
     * @param dataIn 
     * @return unmarshalled object
     * @throws IOException
     */
    public Object readPayload(DataInputStream dataIn) throws IOException;

   
}