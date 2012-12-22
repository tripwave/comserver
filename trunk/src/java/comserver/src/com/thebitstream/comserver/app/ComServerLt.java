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

import java.util.Iterator;
import java.util.Set;

import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;

import com.thebitstream.comserver.app.light.DefaultAdapterFactory;
import com.thebitstream.comserver.app.light.DefaultNodeFactory;
import com.thebitstream.comserver.auth.IAuthorize;
import com.thebitstream.comserver.identity.IClientIdentity;
import com.thebitstream.comserver.nodes.IConnectionNode;
import com.thebitstream.comserver.nodes.data.INodeData;
import com.thebitstream.comserver.stream.IResourceSink;
import com.thebitstream.comserver.stream.Resource;


public abstract class ComServerLt extends MultiThreadedApplicationAdapter{
	
	private INodeDataFactory dataFactory;
	private IClientProxyAdapterFactory adapterFactory;
	private IAuthorize tokenReader;
	private IClientIdentity idGenerator;
	
	private boolean allowMultipleIdConnections=false;
	
	public ComServerLt(){
		adapterFactory=new DefaultAdapterFactory();
		
		dataFactory=new DefaultNodeFactory();
	}

	public boolean appConnect(IConnection client, Object[] params) 
	{
		if(params.length>1)
		{
			if(tokenReader != null)
			{
				if(! tokenReader.appConnect(params))
					return false;
			}
						
			if(idGenerator != null)
			{
				client.setAttribute(IResourceSink.PROP_ID,idGenerator.readId(params));
				client.setAttribute(IResourceSink.PROP_TYPE, idGenerator.readType(params));
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
			return true;
		}
		
		IConnectionNode node=new IConnectionNode(client);
		client.setAttribute(IResourceSink.PROP_NODE, node);	
		
		return onAppConnect(client,node);
	}
	public void appDisconnect(IConnection conn){
		onAppDisconnect(conn,getClientData());
	}
	public boolean roomStart(IScope room){
		return onRoomStart(room);
	}
	public void roomStop(IScope room){
		onRoomStop(room);
	}
	
	public IConnectionNode getClientData(IConnection connection){
		return (IConnectionNode) connection.getAttribute(IResourceSink.PROP_NODE);
	}
	public IConnectionNode getClientData(){
		return (IConnectionNode) Red5.getConnectionLocal().getAttribute(IResourceSink.PROP_NODE);
	}
	
	public INodeDataFactory getDataFactory() {
		return dataFactory;
	}

	public void setDataFactory(INodeDataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}

	public IClientProxyAdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	public void setAdapterFactory(IClientProxyAdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
	}

	public IAuthorize getTokenReader() {
		return tokenReader;
	}

	public void setTokenReader(IAuthorize reader) {
		tokenReader = reader;
	}

	public IClientIdentity getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IClientIdentity generator) {
		idGenerator = generator;
	}

	public boolean isAllowMultipleIdConnections() {
		return allowMultipleIdConnections;
	}

	public void setAllowMultipleIdConnections(boolean allowMultipleIdConnections) {
		this.allowMultipleIdConnections = allowMultipleIdConnections;
	}
	
	protected boolean alreadyConnected(IConnection client)
	{
		
		Iterator<Set<IConnection>>clients = scope.getConnections().iterator();
		
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

	
	protected abstract boolean onAppStart(IScope room);
	protected abstract boolean onAppConnect(IConnection client, IConnectionNode data);
	protected abstract boolean onRoomStart(IScope room);
	protected abstract void onRoomJoin(IScope room,IConnection client);
	protected abstract void onRoomPart(IScope room,IConnection client);	
	protected abstract void onRoomStop(IScope room);
	protected abstract void onAppDisconnect(IConnection client, IConnectionNode data);	
	protected abstract void onAppStop(IScope room);	
}
