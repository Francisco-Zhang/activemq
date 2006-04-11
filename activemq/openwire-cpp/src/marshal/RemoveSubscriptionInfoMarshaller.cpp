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
#include "marshal/RemoveSubscriptionInfoMarshaller.hpp"

using namespace apache::activemq::client::marshal;

/*
 *  Marshalling code for Open Wire Format for RemoveSubscriptionInfo
 *
 * NOTE!: This file is autogenerated - do not modify!
 *        if you need to make a change, please see the Groovy scripts in the
 *        activemq-core module
 */

RemoveSubscriptionInfoMarshaller::RemoveSubscriptionInfoMarshaller()
{
    // no-op
}

RemoveSubscriptionInfoMarshaller::~RemoveSubscriptionInfoMarshaller()
{
    // no-op
}



IDataStructure* RemoveSubscriptionInfoMarshaller::createObject() 
{
    return new RemoveSubscriptionInfo();
}

char RemoveSubscriptionInfoMarshaller::getDataStructureType() 
{
    return RemoveSubscriptionInfo.ID_RemoveSubscriptionInfo;
}

    /* 
     * Un-marshal an object instance from the data input stream
     */ 
void RemoveSubscriptionInfoMarshaller::unmarshal(ProtocolFormat& wireFormat, Object o, BinaryReader& dataIn, BooleanStream& bs) 
{
    base.unmarshal(wireFormat, o, dataIn, bs);

    RemoveSubscriptionInfo& info = (RemoveSubscriptionInfo&) o;
        info.setConnectionId((org.apache.activemq.command.ConnectionId) tightUnmarsalCachedObject(wireFormat, dataIn, bs));
        info.setSubcriptionName(tightUnmarshalString(dataIn, bs));
        info.setClientId(tightUnmarshalString(dataIn, bs));

}


/*
 * Write the booleans that this object uses to a BooleanStream
 */
int RemoveSubscriptionInfoMarshaller::marshal1(ProtocolFormat& wireFormat, Object& o, BooleanStream& bs) {
    RemoveSubscriptionInfo& info = (RemoveSubscriptionInfo&) o;

    int rc = base.marshal1(wireFormat, info, bs);
    rc += marshal1CachedObject(wireFormat, info.getConnectionId(), bs);
    rc += writeString(info.getSubcriptionName(), bs);
    rc += writeString(info.getClientId(), bs);

    return rc + 0;
}

/* 
 * Write a object instance to data output stream
 */
void RemoveSubscriptionInfoMarshaller::marshal2(ProtocolFormat& wireFormat, Object& o, BinaryWriter& dataOut, BooleanStream& bs) {
    base.marshal2(wireFormat, o, dataOut, bs);

    RemoveSubscriptionInfo& info = (RemoveSubscriptionInfo&) o;
    marshal2CachedObject(wireFormat, info.getConnectionId(), dataOut, bs);
    writeString(info.getSubcriptionName(), dataOut, bs);
    writeString(info.getClientId(), dataOut, bs);

}
