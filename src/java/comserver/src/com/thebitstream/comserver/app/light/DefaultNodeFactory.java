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
package com.thebitstream.comserver.app.light;

import java.util.Map;

import com.thebitstream.comserver.app.INodeDataFactory;
import com.thebitstream.comserver.nodes.data.INodeData;
import com.thebitstream.comserver.nodes.data.NodeData;

public class DefaultNodeFactory implements INodeDataFactory {
	
	@SuppressWarnings("unchecked")
	public INodeData createData(String id, Object clientParam) {
		NodeData data=new NodeData(id);
		
		data.setData((Map<Object, Object>) clientParam);
		
		return data;
	}	
}
