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

import java.util.Map;


import org.red5.server.api.IConnection;
import org.red5.server.api.service.IServiceCapableConnection;

import com.thebitstream.comserver.stream.IResourceSink;

/**
 * @author Andy Shaules
 * @version 1.0
 */
public class IConnectionNode implements IComserverNode {

	private IConnection connection;
	
	public IConnectionNode(IConnection client){
		
		connection=client;
	}
	
	@Override
	public void closingResource(IResourceSink feed) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Object, Object>  getNodeData() {
		
		return (Map<Object, Object>) connection.getAttribute(IResourceSink.PROP_DATA);
	}

	@Override
	public String getNodeId() {
		
		return connection.getAttribute(IResourceSink.PROP_ID).toString();
	}

	@Override
	public String getNodeType() {
		
		return connection.getAttribute(IResourceSink.PROP_TYPE).toString();
	}

	@Override
	public void invoke(String method, Map<Object, Object> data) {
	
		((IServiceCapableConnection) connection).invoke( method , new Object[] { data} );

	}

	@Override
	public void setNodeData(Map<Object, Object>  data) {
		connection.setAttribute(IResourceSink.PROP_DATA,data);

	}

	@Override
	public void setNodeId(String id) {
		connection.setAttribute(IResourceSink.PROP_ID,id);

	}

	@Override
	public void setNodeType(String type) {
		connection.setAttribute(IResourceSink.PROP_TYPE,type);
	
	}

	@Override
	public void setResource(IResourceSink resource) {
				
	}

}
