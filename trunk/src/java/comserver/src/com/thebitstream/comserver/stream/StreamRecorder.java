/*
 * COMSERVER Open Source Application Framework - http://www.thebitstream.com
 *
 * Copyright (c) 2009-2010 by Andy Shaules. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.thebitstream.comserver.stream;


import java.io.IOException;

import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.messaging.IMessage;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.stream.StreamingProxy;
import org.red5.server.stream.message.RTMPMessage;


/**
 * @author Andy Shaules
 * @version 1.0
 */
public class StreamRecorder implements IStreamListener {

	private StreamingProxy streamProxy;
	
	public StreamRecorder(String archiveHost,String archiveApp, int archivePort){
		
		streamProxy = new StreamingProxy();	
		streamProxy.init();
		streamProxy.setHost(archiveHost);
		streamProxy.setApp(archiveApp);
		streamProxy.setPort(archivePort);		
	}
	
	public void start(String publishName, String publishMode, Object[] params){
		streamProxy.start(publishName, publishMode, params);
	}
	
	public void stop(){
		streamProxy.stop();
	}
	
	@Override
	public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
		
		try {
			
			RTMPMessage message=RTMPMessage.build((IRTMPEvent) packet);
		
			streamProxy.pushMessage(null, (IMessage) message);
		
		} catch (IOException e) {
			
			streamProxy.stop();
			
		}

	}

}
