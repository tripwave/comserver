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

package com.thebitstream.comserver.nodes.data;

import java.util.Iterator;
import java.util.Map;

import org.red5.io.utils.ObjectMap;

/**
 * @author Andy Shaules
 * @version 1.0
 */
public class NodeData extends ObjectMap<Object, Object> implements INodeData {

	public static final String ID = "id" ;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1269562521007095568L;
	

	
	public NodeData(String id) {
		super();
		put(ID,id);
	}

	@Override
	public String getId() {
		
		return (get(ID) != null)?get(ID).toString():null;
	}

	@Override
	public void setId(String id) {
		put(ID,id);
	}
	
	public void setData(Map<Object,Object> data){
		Object id=get(ID);
		clear();
		
		Iterator<Object> props =data.keySet().iterator();
		while(props.hasNext()){
			Object propName=props.next();
			
			put(propName,data.get(propName));
			
		}
		put(ID,id);
	}


}
