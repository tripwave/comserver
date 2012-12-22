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
 * Copyright (c) 2009-2013 by Andy Shaules. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thebitstream.comserver.stream;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.red5.server.api.IContext;
import org.red5.server.api.scope.IBroadcastScope;
import org.red5.server.api.scope.IScope;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.scope.BroadcastScope;
import org.red5.server.stream.IProviderService;

import com.thebitstream.comserver.feeds.IResourceFeed;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.services.IClientProxyAdapter;
import com.thebitstream.comserver.stream.util.InvocationFactory;
import com.thebitstream.comserver.stream.util.ResourceExecutor;


/**
 * @author Andy Shaules
 * @version 1.0
 */
public class Resource implements IResourceSink  
{

	
	private List<IResourceFeed> _feeds=new ArrayList<IResourceFeed>();
	private ResourceStream _stream;
	private String _name;
	private String _serializedPath;
	private ResourceExecutor _executor;
	private List<IComserverNode> _subscribers=new ArrayList<IComserverNode>();
	private IClientProxyAdapter adapter;
	
	
	public ResourceStream getStream()
	{
		return _stream;
	}
	public boolean removeFeed(IResourceFeed feed )
	{
		//feed.setResourceSink(null);
		return _feeds.remove(feed);
	}
	public void addFeed(IResourceFeed feed)
	{
		_feeds.add(feed);
		feed.addResourceSink(this);
		
	}
	public void execute()
	{
		for (int i=0;i<_feeds.size();i++){
			_feeds.get(i).execute(this);
		}
	}
	public void setFeeds(List<IResourceFeed> feeds) 
	{
		this._feeds = feeds;
	}
	public List<IResourceFeed> getFeeds() {
		return _feeds;
	}
	public synchronized void sendEvent(String method, Map<Object,Object> data)
	{
		Notify event=InvocationFactory.createNotifyEvent(method, data);
		_stream.dispatchEvent(event);
	}
	public Resource(IScope outputScope ,String gameName)
	{
		String pth=outputScope.getContextPath();
		pth=pth.substring(1);
		pth=pth.replace('/', ':');
		setSerializedPath(pth+":"+gameName );
		
		_name=gameName;
		_stream = new ResourceStream(_name);
		_stream.setScope(outputScope);

	
		IContext context = outputScope.getContext();
		
		IProviderService providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
		
		if (providerService.registerBroadcastStream(outputScope, _stream.getPublishedName(), _stream)) 
		{
			IBroadcastScope bsScope = (BroadcastScope) providerService.getLiveProviderInput(outputScope, _stream
					.getPublishedName(), true);
			bsScope.setClientBroadcastStream(_stream);
		}
		
		_stream.start();	
	}
	
	public void close()
	{
		
		for(int i=0;i<_subscribers.size();i++)
		{
			 _subscribers.get(i).closingResource(this);
		}
		
		for (int i=0;i<_feeds.size();i++){
			_feeds.get(i).removeResourceSink(this);
		}	
		
		_stream.stop();
		
	}
	
	public void reRegister(IScope outputScope ,String gameName) throws Exception
	{
		IContext context = outputScope.getContext();
		IProviderService providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
		
		if(providerService.getBroadcastStreamNames(outputScope).contains(gameName))
		{
			return;
		}
	
		if (providerService.registerBroadcastStream(outputScope, _stream.getPublishedName(), _stream))
		{
			IBroadcastScope bsScope = (BroadcastScope) providerService.getLiveProviderInput(outputScope, _stream
					.getPublishedName(), true);
			bsScope.setClientBroadcastStream(_stream);
		}		
	}
	public void setExecutor(ResourceExecutor resourceExecutor) 
	{
		this._executor=resourceExecutor;
		
	}
	public ResourceExecutor getExecutor() 
	{
		return _executor;
	}
	@Override
	public void addSubscriber(IComserverNode connection) {
		
		
		_subscribers.add(connection);
	
		
		for(int i=0;i<_feeds.size();i++)
		{
			 _feeds.get(i).onClientAdded(connection);
		}
		
		
	}
	public List<String> getSubscriberIds(){
	
		ArrayList<String>ret=new ArrayList<String>();
		
		for(int i=0;i<_subscribers.size();i++)
		{
			ret.add( _subscribers.get(i).getNodeId());
		}
		return ret;
	}
	@Override
	public List<IComserverNode> getSubscribers() {
		
		return _subscribers;
	}
	@Override
	public void removeSubscriber(IComserverNode connection) {
		_subscribers.remove(connection);
		
		for(int i=0;i<_feeds.size();i++)
		{
			 _feeds.get(i).onClientRemoved(connection);
		}
		
	}
	private void setSerializedPath(String _serializedPath) {
		this._serializedPath = _serializedPath;
	}
	public String getSerializedPath() {
		return _serializedPath;
	}
	public String getName() {
		return _name;
	}
	@Override
	public void removeSubscriberById(String id) {
		
		for(int i=0;i<_subscribers.size();i++)
		{
			if(_subscribers.get(i).getNodeId().equals(id)){
				IComserverNode node =_subscribers.remove(i);
				
				for(int j=0;j<_feeds.size();j++)
				{
					 _feeds.get(j).onClientRemoved(node);
				}
				return ;
				
			}
		
		}	
		
	}
	public IComserverNode getSubscriberById(String id) {
		
		for(int i=0;i<_subscribers.size();i++)
		{
			if(_subscribers.get(i).getNodeId().equals(id)){
				return _subscribers.get(i);
			}
		}
		return null;
		
	}
	@Override
	public IScope getScope() {
		
		return _stream.getScope();
	}
	@Override
	public IClientProxyAdapter getProxyAdapter() {
		
		return adapter;
	}
	@Override
	public void setProxyAdapter(IClientProxyAdapter adapter) {
		this.adapter=adapter;
		
	}	
}
