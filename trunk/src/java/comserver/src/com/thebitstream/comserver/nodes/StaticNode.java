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
