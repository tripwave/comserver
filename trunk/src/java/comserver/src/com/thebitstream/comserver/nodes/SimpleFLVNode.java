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
