/*
 * COMSERVER Open Source Application Framework - http://www.thebitstream.com
 *
 * Copyright (c) 2009-2011 by Andy Shaules. All rights reserved.
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
package org.red5.server.plugin.jni.objects.net;


import java.io.IOException;

import org.red5.server.net.rtmp.RTMPClient;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.stream.message.RTMPMessage;




/**
 * @author Andy Shaules (bowljoman@hotmail.com)
 *
 */
public class ChannelOut implements ICudaListener {
	
	private int id=0;
	private int mode = 0;
	private String host="127.0.0.1";
	private String app="cuda";	
	private int port=1935;
	private String publishName="cuda";
	private IGForce parser;
	private ICudaListener publisher;
	private CudaProxy streamer=null;

	
	public ChannelOut(){
		
	System.out.println("ChannelOut initiation");
				
	parser=new VideoFramer();
	publisher=this;	
		
	}

	public void pushAVCFrame(byte[] frame, int timecode){
		parser.pushAVCFrame(frame, timecode);
	}
	
	public void dispatchEvent(IRTMPEvent packet) {
		
		if(streamer==null)
			return;
		
		RTMPMessage message= RTMPMessage.build(packet);
		
		try {
			streamer.pushMessage(null,message );
		} catch (IOException e) {
			 stopCapture();
		}
		
	}
	
	public void initCapture() throws Exception{
		
	
		if(streamer!=null)
			return;
		
		System.out.println("initCapture");
		
		streamer=new CudaProxy();
			
		RTMPClient client=(RTMPClient) Thread.currentThread().getContextClassLoader().loadClass("org.red5.server.net.rtmp.RTMPClient").newInstance();
			
		streamer.init(client);
	
		streamer.setApp(app);
		streamer.setHost(host);
		streamer.setPort(port);
		String publishMode=CudaProxy.LIVE;
		
		switch(mode){
		
			case 2:
				publishMode=CudaProxy.APPEND;
				break;
				
			case 1:
				publishMode=CudaProxy.RECORD;
				break;

			default:
				publishMode=CudaProxy.LIVE;
				break;
		}
		
		System.out.println("streamer.start "+ publishMode);
		
		streamer.start(publishName, publishMode, new Object[]{});		
	}
	
	public void stopCapture() {
		System.out.println("stopCapture");
		if(streamer==null)
			return;
		streamer.stop();
		streamer=null;
		
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		
		this.id = id;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		System.out.println("setHost "+ host);
		this.host = host;
	}
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		System.out.println("setApp "+ app);
		this.app = app;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		System.out.println("setPort "+ port);
		this.port = port;
	}
	public String getPublishName() {
		return publishName;
	}
	public void setPublishName(String publishName) {
		System.out.println("setPublishName "+ publishName);
		this.publishName = publishName;
	}
	public IGForce getParser() {
		return parser;
	}
	public void setParser(IGForce parser) {
		this.parser = parser;
	}
	public ICudaListener getPublisher() {
		return publisher;
	}
	public void setPublisher(ICudaListener publisher) {
		this.publisher = publisher;
	}

	public CudaProxy getStreamer() {
		return streamer;
	}

	public void setMode(int mode) {
		System.out.println("setMode "+ mode);
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	public static void main(String args[]){
	//	ChannelOut out = new ChannelOut();
	//	out.initCapture();
	}	
}
