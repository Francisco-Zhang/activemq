/*
 * Copyright 2006 The Apache Software Foundation or its licensors, as
 * applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "marshal/SessionInfoMarshaller.hpp"

using namespace apache::activemq::client::marshal;

/*
 *  Marshalling code for Open Wire Format for SessionInfo
 *
 * NOTE!: This file is autogenerated - do not modify!
 *        if you need to make a change, please see the Groovy scripts in the
 *        activemq-core module
 */

SessionInfoMarshaller::SessionInfoMarshaller()
{
    // no-op
}

SessionInfoMarshaller::~SessionInfoMarshaller()
{
    // no-op
}



IDataStructure* SessionInfoMarshaller::createObject() 
{
    return new SessionInfo();
}

char SessionInfoMarshaller::getDataStructureType() 
{
    return SessionInfo.ID_SessionInfo;
}

    /* 
     * Un-marshal an object instance from the data input stream
     */ 
void SessionInfoMarshaller::unmarshal(ProtocolFormat& wireFormat, Object o, BinaryReader& dataIn, BooleanStream& bs) 
{
    base.unmarshal(wireFormat, o, dataIn, bs);

    SessionInfo& info = (SessionInfo&) o;
        info.setSessionId((org.apache.activemq.command.SessionId) tightUnmarsalCachedObject(wireFormat, dataIn, bs));

}


/*
 * Write the booleans that this object uses to a BooleanStream
 */
int SessionInfoMarshaller::marshal1(ProtocolFormat& wireFormat, Object& o, BooleanStream& bs) {
    SessionInfo& info = (SessionInfo&) o;

    int rc = base.marshal1(wireFormat, info, bs);
    rc += marshal1CachedObject(wireFormat, info.getSessionId(), bs);

    return rc + 0;
}

/* 
 * Write a object instance to data output stream
 */
void SessionInfoMarshaller::marshal2(ProtocolFormat& wireFormat, Object& o, BinaryWriter& dataOut, BooleanStream& bs) {
    base.marshal2(wireFormat, o, dataOut, bs);

    SessionInfo& info = (SessionInfo&) o;
    marshal2CachedObject(wireFormat, info.getSessionId(), dataOut, bs);

}
