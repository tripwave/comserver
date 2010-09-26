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
package com.thebitstream.comserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.red5.server.api.Red5;


import com.thebitstream.comserver.app.INodeDataFactory;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.stream.IResourceSink;
import com.thebitstream.comserver.stream.Resource;
/**
 * @author Andy Shaules
 * @version 1.0
 */
public class Service implements IClientProxyAdapter {
	
	private IResourceSink _game;
	private INodeDataFactory _dataFactory;
	public List<IPacketListener>processors;

	private ServiceHandler handler;
	
	public Service(IResourceSink resource,INodeDataFactory dataFactory) {
		_game=resource;
		_dataFactory=dataFactory;
		processors=new ArrayList<IPacketListener>();
		handler=new ServiceHandler(this);
		
	}

	@Override
	public void addPacketListener(IPacketListener listener) {
		processors.add(listener);
		
	}


	@Override
	public IResourceSink getResource() {
		return _game;
		
	}


	@Override
	public void removePacketListener(IPacketListener listener) {
		processors.remove(listener);
		
	}
	
public class ServiceHandler implements IServiceHandler{
	
	private Service owner;

	public ServiceHandler(Service owner){
		this.owner=owner;
	}
	
	public int sendEvent(String method ,Map<Object,Object> object)
	{
		object.put("id",Red5.getConnectionLocal().getAttribute(Resource.PROP_ID));		
		
		for (int y = 0 ; y < owner.processors.size() ; y ++){
			if(!owner.processors.get(y).processPacket((IComserverNode) Red5.getConnectionLocal().getAttribute(Resource.PROP_NODE),method, object))
				return 0;
		}
		
		_game.sendEvent(method,object);
		
		return 1;
	}

	public int setEvent(String method ,Map<Object,Object> object,Map<Object,Object> data)
	{
		data.put("id",Red5.getConnectionLocal().getAttribute(Resource.PROP_ID));		
		
		for (int y = 0 ; y < owner.processors.size() ; y ++){
			if(!owner.processors.get(y).processPacket((IComserverNode) Red5.getConnectionLocal().getAttribute(Resource.PROP_NODE),method, object, data))
				return 0;
		}
		
		
		Red5.getConnectionLocal().setAttribute(Resource.PROP_DATA, owner._dataFactory.createData(data.get("id").toString(), data));			
		
		_game.sendEvent(method,object);
		
		return 1;
	}
	
	public int setData(Map<Object,Object> data)
	{
		data.put("id",Red5.getConnectionLocal().getAttribute(Resource.PROP_ID));		
		
		for (int y = 0 ; y < owner.processors.size() ; y ++){
			if(!owner.processors.get(y).processPacket((IComserverNode) Red5.getConnectionLocal().getAttribute(Resource.PROP_NODE), data))
				return 0;
		}
		
		Red5.getConnectionLocal().setAttribute(Resource.PROP_DATA, owner._dataFactory.createData(data.get("id").toString(), data));							
		
		return 1;
	}	
}

@Override
public IServiceHandler getHandler() {
	
	return handler;
}

		
}
