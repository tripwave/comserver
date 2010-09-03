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

import java.util.Map;

import com.thebitstream.comserver.nodes.IComserverNode;

/**
 * An IPacketListener listener method can filter the packet messages and reject them by returning false.
 * <p>The IClientProxyAdapter is the interface between clients and the shared FLV stream.  
 * The IClientProxyAdapter has three main functions, sending script data, changing the client's node data, and a combination of the two. 
 * The IPacketListener allows you to add packet processors and filters to reject the messages and even close the client or invoke messages back.</p>
 * 
 *
 * @author Andy Shaules
 * @version 1.0
 */
public interface IPacketListener {
	
	/**
	 * Call to inject flv script data.
	 * @param from The client.
	 * @param method The method name.
	 * @param object The method arguments.
	 * @return false to reject
	 */
	boolean processPacket(IComserverNode from,String method ,Map<Object,Object> object);
	
	/**
	 * Call to inject flv script data and set the IClientNode data object.
	 * @param from The client.
	 * @param method The method name.
	 * @param object The method arguments.
	 * @param data  The Object to replace the node data.
	 * @return false to reject
	 */
	boolean processPacket(IComserverNode from,String method ,Map<Object,Object> object,Map<Object,Object> data);
	
	/**
	 * Call to set the IClientNode data object without injecting flv script data.
	 * 
	 * @param from The client
	 * @param data The Object to replace the node data.
	 * @return false to reject
	 */
	boolean processPacket(IComserverNode from, Map<Object, Object> data);

}
