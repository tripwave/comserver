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


import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.stream.IResourceSink;

/**
 * Adding a node to a resource can appear as if there is another client/entity connected.
 *
 * @author Andy Shaules
 * @version 1.0
 */
public class StaticNode implements IComserverNode {

	Map<Object, Object> data;
	private String id;
	private String type;

	@Override
	public void closingResource(IResourceSink feed) {

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
		
		return type;
	}

	@Override
	public void invoke(String method, Map<Object, Object> data) {
	

	}

	@Override
	public void setNodeData(Map<Object, Object>  data) {
		this.data=data;
	}

	@Override
	public void setNodeId(String id) {
		this.id=id;

	}

	@Override
	public void setNodeType(String type) {
		this.type=type;

	}

	@Override
	public void setResource(IResourceSink resource) {

	}

}
