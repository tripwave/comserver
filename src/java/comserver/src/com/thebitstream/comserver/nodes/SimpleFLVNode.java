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
package com.thebitstream.comserver.nodes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import org.red5.io.utils.ObjectMap;
import org.red5.server.api.IContext;
import org.red5.server.messaging.IConsumer;
import org.red5.server.messaging.IMessage;
import org.red5.server.messaging.IMessageComponent;
import org.red5.server.messaging.IMessageInput;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.OOBControlMessage;

import org.red5.server.stream.IProviderService;
import org.red5.server.stream.ISeekableProvider;
import org.red5.server.stream.message.RTMPMessage;


import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.stream.IResourceSink;

/**
 * Full rock solid Audio and Video support is not planned until version 2.0
 * <p>This is sufficient to play back user recorded script data in flv form.</p>
 * @author Andy Shaules
 * @version 1.0
 */
public class SimpleFLVNode extends Thread implements IComserverNode, IConsumer {

	private IMessageInput msgIn;

	
	Map<Object, Object> data;

	
	private IResourceSink resource;

	
	private boolean doRun;


	private int lastTime;


	private IProviderService providerService;


	private long startTime;


	private long streamTime;


	private String inputFile;


	private String id;


	private boolean loop;


	private boolean transmiting;
	
	
	public SimpleFLVNode(String id, String fileName,boolean loop){
		transmiting=true;
		this.loop=loop;
		this.id=id;
		this.inputFile=fileName;
		data = new ObjectMap<Object, Object>();
		data.put("file",fileName);
	}
	
	private void initiate(IResourceSink resource){
		lastTime=-1;
		
		data.put("id", id);
		
		data.put("nodeType", "0");
		
		IContext context =resource.getStream().getScope().getContext();
		
		providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
					
		msgIn = providerService.getVODProviderInput(resource.getStream().getScope(), inputFile);	
				
	}
	
	
	public void run(){

		
		
		if((msgIn == null))
			return;
		
		doRun=true;
		while(doRun){
	
			IMessage msg;
			try {
				
			msg = msgIn.pullMessage();
			
			RTMPMessage lMsg = null;
			
			if (msg == null) 
			{//EOF
				if(! loop)
					return;
				
				lastTime=-1;
				
				msgIn = providerService.getVODProviderInput(resource.getStream().getScope(), inputFile);
				//Must sleep some time, minimum unknown.
				doSleep(500);
				continue;
			}
			
			long now=0;
			
			if (msg instanceof RTMPMessage) 
			{
				lMsg = (RTMPMessage) msg;	
				if(lastTime==-1){
					
					now=startTime=System.currentTimeMillis();
					streamTime=0;
				
				}else{
					
					now= System.currentTimeMillis()- startTime ;
				}
							
				int time=lMsg.getBody().getTimestamp();
			
				streamTime=time;
				
				
				if(  streamTime > now)
				{
					doSleep((int) (streamTime-now));
				}
				
				lastTime=time;
				
				if(doRun && transmiting)
				resource.getStream().dispatchEvent(lMsg.getBody());
			
			}
				} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}

	
	private void doSleep(int milli){
		try {
			Thread.sleep(milli);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}		
	}

	
	public void setProperty(String prop, String value)
	{
		data.put(prop, value);
	}
	public Object getProperty(String prop)
	{
		return data.get(prop);
	}	
	@Override
	public void closingResource(IResourceSink feed) {
		
		doRun=false;
	}

	
	@Override
	public Map<Object, Object>  getNodeData() {
		
		return data;
	}

	@Override
	public String getNodeId() {
		
		return id;
	}

	@Override
	public String getNodeType() {
		
		return data.get("nodeType").toString();
	}

	@Override
	public void invoke(String method, Map<Object, Object> data) {
		

	}

	@Override
	public void setNodeData(Map<Object, Object>  data) {
		this.data=(ObjectMap<Object, Object>) data;

	}

	@Override
	public void setNodeId(String id) {
		this.id=id;

	}

	@Override
	public void setNodeType(String type) {
	data.put("nodeType", type);

	}

	@Override
	public void setResource(IResourceSink resource) {
		this.resource=resource;
		
		initiate(resource);

	}
	@SuppressWarnings("unused")
	private int sendVODSeekCM(int position) {
		OOBControlMessage oobCtrlMsg = new OOBControlMessage();
		oobCtrlMsg.setTarget(ISeekableProvider.KEY);
		oobCtrlMsg.setServiceName("seek");
		Map<String, Object> paramMap = new HashMap<String, Object>(1);
		paramMap.put("position", position);
		oobCtrlMsg.setServiceParamMap(paramMap);
		msgIn.sendOOBControlMessage((IConsumer) this, oobCtrlMsg);
		if (oobCtrlMsg.getResult() instanceof Integer) {
			return (Integer) oobCtrlMsg.getResult();
		} else {
			return -1;
		}
	}
	@Override
	public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
		
		
	}

	public boolean isTransmiting() {
		return transmiting;
	}

	public void setTransmiting(boolean doTransmit) {
		this.transmiting = doTransmit;
	}
	

}
