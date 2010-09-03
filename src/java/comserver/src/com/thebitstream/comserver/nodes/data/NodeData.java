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
