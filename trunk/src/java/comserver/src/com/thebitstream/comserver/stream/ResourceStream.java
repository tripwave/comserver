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
/**
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.statistics.IClientBroadcastStreamStatistics;
import org.red5.server.api.statistics.support.StatisticsCounter;
import org.red5.server.api.stream.IStreamCapableConnection;
import org.red5.server.api.stream.IStreamCodecInfo;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.api.stream.ResourceExistException;
import org.red5.server.api.stream.ResourceNotFoundException;
import org.red5.server.messaging.IConsumer;
import org.red5.server.messaging.IMessageComponent;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IProvider;
import org.red5.server.messaging.OOBControlMessage;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.stream.IStreamData;
import org.red5.server.stream.PlayEngine;
import org.red5.server.stream.codec.StreamCodecInfo;
import org.red5.server.stream.message.RTMPMessage;
import org.slf4j.Logger;

import com.thebitstream.comserver.stream.util.TimeStampGenrator;

// \A[^(\Q/*\E\s+COMSERVER*)]

/**
 * @author Andy Shaules
 * @version 1.0
 */
public class ResourceStream implements IResourceStream {
	
	private static Logger log = Red5LoggerFactory.getLogger(ResourceStream.class, "GameStream");

	private Set<IStreamListener> mListeners = new CopyOnWriteArraySet<IStreamListener>();

	
	private String mPublishedName;

	private IPipe mLivePipe;

	private IScope mScope;

	private StreamCodecInfo mCodecInfo;

	private List<IConsumer> newComsumers = new ArrayList<IConsumer>();

	private StatisticsCounter subscriberStats = new StatisticsCounter();

	private long bytesReceived = 0;

	private long creationTime;

	private IRTMPEvent _metaDataEvent;

	private int _lastEventTime;

	

	public ResourceStream(String name) {
		
		
		mPublishedName = name;
		mLivePipe = null;
		mCodecInfo = new StreamCodecInfo();
		


	}
    /**
     * Implement IStreamListener in an object to monitor the connection status events and auxiliary data.
     */
	@Override
	public void addStreamListener(IStreamListener listener) {
		log.debug("addStreamListener(listener: {})", listener);
		mListeners.add(listener);
	}

	@Override
	public IProvider getProvider() {
		log.debug("getProvider()");
		return this;
	}

	@Override
	public String getPublishedName() {
		return mPublishedName;
	}

	@Override
	public String getSaveFilename() {
		throw new Error("unimplemented method");
	}

	@Override
	public Collection<IStreamListener> getStreamListeners() {
		return mListeners;
	}

	@Override
	public void removeStreamListener(IStreamListener listener) {
		mListeners.remove(listener);
	}

	@Override
	public void saveAs(String name, boolean append) throws IOException, ResourceNotFoundException,
			ResourceExistException {

	}

	@Override
	public void setPublishedName(String name) {
		//log.debug("setPublishedName(name:{})", name);
		mPublishedName = name;
	}

	@Override
	public void close() {
		//	log.debug("close()");
	}

	@Override
	public IStreamCodecInfo getCodecInfo() {

		return mCodecInfo;
	}

	@Override
	public String getName() {
		return mPublishedName;
	}

	@Override
	public IScope getScope() {
		return mScope;
	}

	public void setScope(IScope scope) {
		mScope = scope;
	}

	@Override
	public void start() {
		log.debug("start");
		creationTime=TimeStampGenrator.startTimer(this);
		bytesReceived = 0;
	}

	@Override
	public void stop() {
		log.debug("stop");
		TimeStampGenrator.stopTimer(this);
	}

	@Override
	public void onOOBControlMessage(IMessageComponent arg0, IPipe arg1, OOBControlMessage arg2) {

	}

	@Override
	public void onPipeConnectionEvent(PipeConnectionEvent event) {

		switch (event.getType()) {
			case PipeConnectionEvent.PROVIDER_CONNECT_PUSH:
				if ((event.getProvider() == this) && (event.getParamMap() == null)) {
					mLivePipe = (IPipe) event.getSource();
					log.debug("mLivePipe {}", mLivePipe);
					for (@SuppressWarnings("unused")
					IConsumer consumer : mLivePipe.getConsumers()) {
						subscriberStats.increment();
					}
				}
				break;
			case PipeConnectionEvent.PROVIDER_DISCONNECT:
				if (mLivePipe == event.getSource()) {
					mLivePipe = null;
				}
				break;
			case PipeConnectionEvent.CONSUMER_CONNECT_PUSH:
				if (mLivePipe != null) {
					List<IConsumer> consumers = mLivePipe.getConsumers();
					int count = consumers.size();
					if (count > 0) 
					{
						
						newComsumers.add(consumers.get(count - 1));	
					}
					subscriberStats.increment();
				}
				break;

			case PipeConnectionEvent.CONSUMER_DISCONNECT:
				subscriberStats.decrement();
				break;
			default:
				break;
		}
	}

	private void sendMetaData() {
		

		while (newComsumers.size() > 0) {
			
			IConsumer consumer = newComsumers.remove(0);
			if (consumer instanceof PlayEngine) {

				 if (_metaDataEvent != null) 
				 {
					log.debug("Sending meta data");
					
					int audioTime= getCurrentTimestamp();
					_metaDataEvent.setTimestamp(audioTime);
					_metaDataEvent.setHeader(new Header());
					_metaDataEvent.getHeader().setTimer(audioTime);
					_metaDataEvent.getHeader().setTimerBase(audioTime);
					RTMPMessage msgM = RTMPMessage.build(_metaDataEvent);
					
					try {
						((PlayEngine) consumer).pushMessage(null, msgM);
						
					} catch (IOException e) {
						log.error("Error icyStream 249 " + e.getMessage());
					}
				}
			}
		}
	}
	@Override
	public void dispatchStreamEvent(IEvent event) {
		
		
		 
		if (event instanceof IRTMPEvent) {
			
			IRTMPEvent rtmpEvent = (IRTMPEvent) event;
						
			((IRTMPEvent) event).setSourceType(Constants.SOURCE_TYPE_LIVE);
			IoBuffer buf = null;
			if (rtmpEvent instanceof IStreamData && (buf = ((IStreamData<?>) rtmpEvent).getData()) != null) {
				bytesReceived += buf.limit();
			}
			
			
			if (mLivePipe != null) {
				RTMPMessage msg =  RTMPMessage.build(rtmpEvent);
				
				
				try {
					sendMetaData();
					
					_lastEventTime=msg.getBody().getTimestamp();
					
					
					mLivePipe.pushMessage(msg);
				} catch (Exception e) {
					log.debug("dispatchEvent {}, error: {}", event, e.getMessage());
				}
			}
			// Notify listeners about received packet
			if (rtmpEvent instanceof IStreamPacket) {
				for (IStreamListener listener : getStreamListeners()) {
					try {
						listener.packetReceived(this, (IStreamPacket) rtmpEvent);
					} catch (Exception e) {
						log.error("Error while notifying listener {}, error:{}", listener, e);
					}
				}
			}
		}		
	}
	
	public synchronized void dispatchEvent(IEvent event) {

		if (event instanceof IRTMPEvent) {
			
			IRTMPEvent rtmpEvent = (IRTMPEvent) event;
			int compatibleTime=this.getCurrentTimestamp();		
				rtmpEvent.setTimestamp(compatibleTime);
			
				((IRTMPEvent) event).setSourceType(Constants.SOURCE_TYPE_LIVE);
			IoBuffer buf = null;
			if (rtmpEvent instanceof IStreamData && (buf = ((IStreamData<?>) rtmpEvent).getData()) != null) {
				bytesReceived += buf.limit();
			}
			
			
			if (mLivePipe != null) {
				RTMPMessage msg =RTMPMessage.build(rtmpEvent);
				
				
				try {
					sendMetaData();
					
					
					_lastEventTime=msg.getBody().getTimestamp();
					
					
					
					mLivePipe.pushMessage(msg);
				} catch (Exception e) {
					log.debug("dispatchEvent {}, error: {}", event, e.getMessage());
				}
			}
			// Notify listeners about received packet
			if (rtmpEvent instanceof IStreamPacket) {
				for (IStreamListener listener : getStreamListeners()) {
					try {
						listener.packetReceived(this, (IStreamPacket) rtmpEvent);
					} catch (Exception e) {
						log.error("Error while notifying listener {}, error:{}", listener, e);
					}
				}
			}
		}
	}

	@Override
	public int getActiveSubscribers() {
		return subscriberStats.getCurrent();
	}

	@Override
	public long getBytesReceived() {
		return bytesReceived;
	}

	@Override
	public int getMaxSubscribers() {
		return subscriberStats.getMax();
	}

	@Override
	public int getTotalSubscribers() {
		return subscriberStats.getTotal();
	}

	@Override
	public int getCurrentTimestamp() {
		return TimeStampGenrator.getTime(this);
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}



	public void setMetaDataEvent(IRTMPEvent event) {

		_metaDataEvent = event;
	}
	@Override
	public Notify getMetaData() {

		return null;
	}
	public int getLastEventTime() {
		return _lastEventTime;
	}
	public void setLastEventTime(int eventTime) {
		_lastEventTime = eventTime;
	}
	@Override
	public Map<String, String> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public IClientBroadcastStreamStatistics getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setParameters(Map<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startPublishing() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getBroadcastStreamPublishName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getClientBufferDuration() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public IStreamCapableConnection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getStreamId() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setClientBufferDuration(int arg0) {
		// TODO Auto-generated method stub
		
	}

	


}
