/*******************************************************************************
 * Copyright 2009-2013 Andy Shaules
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
 ******************************************************************************/

package com.thebitstream.comserver.stream.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;


/**
 * @author Andy Shaules
 * @version 1.0
 */
public class InvocationFactory {
	public static IRTMPEvent createMetaDataEvent(Map<Object, Object> metaData) {

		IoBuffer buf = IoBuffer.allocate(1024);
		buf.setAutoExpand(true);
		Output out = new Output(buf);
		out.writeString("onMetaData");
		Map<Object, Object> props = new HashMap<Object, Object>();
		
		if (metaData != null) {
		props.putAll(metaData);
		}
		props.put("canSeekToEnd", false);
		out.writeMap(props, new Serializer());
		buf.flip();
		return new Notify(buf);
	}

	public static Notify createNotifyEvent( String functionName,Map<Object, Object> parameters) {

		IoBuffer buf = IoBuffer.allocate(128);
		buf.setAutoExpand(true);
		Output out = new Output(buf);
		out.writeString(functionName);

		out.writeObject(parameters,  new Serializer());
		
		buf.flip();

		return new Notify(buf);
	}	
}
