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
