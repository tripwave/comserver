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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.stream.BroadcastScope;
import org.red5.server.stream.IBroadcastScope;
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
			bsScope.setAttribute(IBroadcastScope.STREAM_ATTRIBUTE, _stream);
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
			bsScope.setAttribute(IBroadcastScope.STREAM_ATTRIBUTE, _stream);
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
