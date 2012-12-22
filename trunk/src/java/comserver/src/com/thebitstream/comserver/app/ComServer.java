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

package com.thebitstream.comserver.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.red5.io.utils.ObjectMap;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IServiceCapableConnection;
import org.red5.server.api.stream.IStreamPlaybackSecurity;

import com.thebitstream.comserver.auth.IAuthorize;
import com.thebitstream.comserver.identity.IClientIdentity;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.nodes.IConnectionNode;
import com.thebitstream.comserver.nodes.data.INodeData;
import com.thebitstream.comserver.nodes.data.NodeData;
import com.thebitstream.comserver.services.ExternalService;
import com.thebitstream.comserver.services.IClientProxyAdapter;
import com.thebitstream.comserver.services.Service;
import com.thebitstream.comserver.stream.IResourceSink;
import com.thebitstream.comserver.stream.Resource;
import com.thebitstream.comserver.stream.config.IResourceConfiguration;
import com.thebitstream.comserver.stream.util.ResourceExecutor;




/**
 * @author Andy Shaules
 * @version 1.0
 */
public abstract class ComServer extends MultiThreadedApplicationAdapter implements IResourceSinkFactory,IStreamPlaybackSecurity, IClientProxyAdapterFactory, INodeDataFactory{

	private IAuthorize _tokenReader;
	private IClientIdentity _idGenerator;
	private boolean allowMultipleIdConnections=false;
	protected List<IResourceConfiguration>feedConfigurations = new ArrayList<IResourceConfiguration>();
	protected List<IResourceSink> feedObjects = new ArrayList<IResourceSink>();	
	protected IScope lobby;
	private int executionInterval=1000;
	private INodeDataFactory dataFactory;
	private IClientProxyAdapterFactory adapterFactory;
	private IResourceSinkFactory resourceFactory;
	
	public ComServer(){
		adapterFactory=this;
		dataFactory=this;
		resourceFactory=this;
	}
	
	/**
	 * Returns the list of FLV files at the web context/IScope of the caller.
	 * @return
	 */
	public Map<String, Map<String, Object>> getListOfAvailableFLVs(IScope scope){
		
		Map<String, Map<String, Object>> filesMap = new HashMap<String, Map<String, Object>>();
	
		try {
						
			String path=scope.getContextPath();
		
			for (org.springframework.core.io.Resource flv : lobby.getResources("streams"+path+"/*.flv")){
										
				String flvName = flv.getFile().getName();
				Map<String, Object> fileInfo = new HashMap<String, Object>();
				fileInfo.put("name", flvName);
				filesMap.put(flvName, fileInfo);				
			}
	
		} catch (IOException e) {
						
		}
	
		return filesMap;
	}
	
	
	/**
	 * Get the currently running resources.
	 * @return
	 */
	public List<String> getResources() {
		ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < feedObjects.size(); i++) {

			ret.add(feedObjects.get(i).getSerializedPath());
		}
		return ret;
	}

	protected IResourceConfiguration getConfig(IResourceSink resource){
		for(int y=0;y<this.feedConfigurations.size();y++){
			IResourceConfiguration cfg=feedConfigurations.get(y);
			if(resource.getStream().getScope().getContextPath().equals(cfg.getContext())  && 
					resource.getStream().getName().equals(cfg.getName())) {
					return cfg;
			}
			
		}
		return null;
	}
	
	
	@Override
	public boolean appStart(IScope app) {

		lobby = app;
		lobby.registerServiceHandler("external", new ExternalService(this));
		registerStreamPlaybackSecurity(this);
		return onAppStart(app);
	}
	protected abstract boolean onAppStart(IScope app);

	public void FCSubscribe(String streamName) 
	{

		ObjectMap<Object, Object> param = new ObjectMap<Object, Object>();
		param.put("code", "NetStream.Play.Start");
		param.put("forStream", streamName);
		((IServiceCapableConnection) Red5.getConnectionLocal()).invoke("onFCSubscribe", new Object[] { param });

	}

	public boolean appConnect(IConnection client, Object[] params) 
	{
		if(params.length>1)
		{
			if(_tokenReader != null)
			{
				if(! _tokenReader.appConnect(params))
					return false;
			}
						
			if(_idGenerator != null)
			{
				client.setAttribute(IResourceSink.PROP_ID,_idGenerator.readId(params));
				client.setAttribute(IResourceSink.PROP_TYPE, _idGenerator.readType(params));
			}
			else
			{
				client.setAttribute(IResourceSink.PROP_ID, client.getClient().getId());
				client.setAttribute(IResourceSink.PROP_TYPE,new Object[]{});
			}
			
			INodeData data=dataFactory.createData(client.getAttribute(IResourceSink.PROP_ID).toString() , params[1]);
			
			client.setAttribute(IResourceSink.PROP_DATA, data);
			
			if(! allowMultipleIdConnections){
				alreadyConnected(client);
			}
		}
		else
		{
			return false;
		}
		
		
		IConnectionNode node=new IConnectionNode(client);
		client.setAttribute(IResourceSink.PROP_NODE, node);	
		
		return true;
	}

	protected boolean alreadyConnected(IConnection client)
	{
		
		Iterator<Set<IConnection>>clients = lobby.getConnections().iterator();
		
		while(clients.hasNext())
		{
			Iterator<IConnection> conns=clients.next().iterator();
			while(conns.hasNext())
			{
				IConnection cn=conns.next();
				
				if(Red5.getConnectionLocal() !=  cn && cn.getAttribute(IResourceSink.PROP_ID).toString().equals(client.getAttribute(Resource.PROP_ID).toString()))
				{
					cn.close();
					return true;
				}
			}
			
		}
		
		return false;
	}
	public void appDisconnect(IConnection client) {
		
		
		Iterator<IResourceSink> games = feedObjects.iterator();

		while (games.hasNext()) 
		{
			IResourceSink target = games.next();
			

			if (target.getStream().getScope().getContextPath().equals(client.getScope().getContextPath()))
			{
				target.removeSubscriberById(client.getAttribute(IResourceSink.PROP_ID).toString());
			}
		}		
	}
	public boolean roomStart(IScope room){
		return configureRoomServices(room);
	}

	public void roomStop(IScope room) {
		
		List<IResourceSink> resToRemove=new ArrayList<IResourceSink>();
		
		Iterator<IResourceSink> games = feedObjects.iterator();

		while (games.hasNext()) 
		{
			IResourceSink game = games.next();
			
			if (game.getStream().getScope().getContextPath().equals(room.getContextPath()))
			{
				resToRemove.add(game);	
				
				removeScheduledJob(game.getExecutor().getJobName());			
				
				destroyFeed(game);
				
				game.close();					
			}
		}
		
		while (resToRemove.size()>0) 
		{
			IResourceSink deadStream=resToRemove.remove(0);
			feedObjects.remove(deadStream);
			deadStream=null;
		}
		
		destroyRoomServices(room);
	}
	
	protected abstract boolean configureRoomServices(IScope room);
	
	protected abstract void destroyRoomServices(IScope room);	
	
	@Override
	public boolean isPlaybackAllowed(IScope room, String name, int start, int length, boolean flushPlaylist) 
	{
		
		String target=name;
		
		if(target.endsWith(".flv")){
			return true;
		}
		
		IResourceSink resource = getResourceStream(room, name);
		
		if(resource == null){
				
			return false;
		}
		
			

			
			IConnection connection =Red5.getConnectionLocal();
			
			if(connection != null){
				IComserverNode node=(IComserverNode) connection.getAttribute(IResourceSink.PROP_NODE);
				
				if(node != null){
					
					if( canSubscribe(resource,node)){
						
						resource.addSubscriber(node);
						return true;
					}
				}
			}
		return false;

	}

	protected synchronized IResourceSink getResourceStream(IScope room,String name)
	{
		Iterator<IResourceSink> games = feedObjects.iterator();

		while (games.hasNext()) {
			IResourceSink target = games.next();
			

			if (target.getStream().getScope().getContextPath().equals(room.getContextPath())
					&& target.getStream().getName().equals(name)) {
				//Already exists
				return target;

			}
		}
		
		//Make new one;

		try {
							

			IResourceSink game=resourceFactory.createResourceSink(room, name);

			IClientProxyAdapter service= adapterFactory.createAdapter(game);
			
			game.setProxyAdapter(service);
		
			
			if(service == null || game == null){
				return null;
			}
			
			if(!configureFeed(game))
			{
				return null;	
			}
			
			feedObjects.add(game);
			
			room.registerServiceHandler(name, service.getHandler());
			
			ResourceExecutor gameExec = new ResourceExecutor(game);

			gameExec.setJobName(addScheduledJob(executionInterval, gameExec));				
			
			return game;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}			
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public INodeData createData(String id,Object clientParam) {
		NodeData data=new NodeData(id);
		
		data.setData((Map<Object, Object>) clientParam);
		
		return data;
	}
	
	@Override
	public IClientProxyAdapter  createAdapter(IResourceSink resource) {
	
		Service adapter= new Service(resource,dataFactory);
		
		return adapter;

	}
	
	public IResourceSink createResourceSink(IScope room,String name){
		Resource game;
		
		try {
			game = new Resource(room, name);
			return game;
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected abstract boolean configureFeed(IResourceSink game);
	
	protected abstract boolean canSubscribe(IResourceSink resource,IComserverNode connection);
	
	protected abstract void destroyFeed(IResourceSink game );
	
	public void setTokenReader(IAuthorize _tokenReader) {
		this._tokenReader = _tokenReader;
	}

	public IAuthorize getTokenReader() {
		return _tokenReader;
	}

	public IClientIdentity getIdGenerator() {
		return _idGenerator;
	}

	public void setIdGenerator(IClientIdentity generator) {
		_idGenerator = generator;
	}

	public void setMultipleIdConnections(boolean multipleIdConnections) {
		allowMultipleIdConnections = multipleIdConnections;
	}

	public boolean getMultipleIdConnections() {
		return allowMultipleIdConnections;
	}

	public int getExecutionInterval() {
		return executionInterval;
	}

	public void setExecutionInterval(int executionInterval) {
		this.executionInterval = executionInterval;
	}

	public List<IResourceConfiguration> getFeedConfiguration() {
		return feedConfigurations;
	}

	public void setFeedConfiguration(List<IResourceConfiguration> feedConfiguration) {
		this.feedConfigurations = feedConfiguration;
	}

	public IClientProxyAdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	public void setAdapterFactory(IClientProxyAdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
	}

	public INodeDataFactory getDataFactory() {
		return dataFactory;
	}

	public void setDataFactory(INodeDataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}

	public void setResourceFactory(IResourceSinkFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

	public IResourceSinkFactory getResourceFactory() {
		return resourceFactory;
	}
	
}
