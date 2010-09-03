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
package com.thebitstream.comserver.stream;

import java.util.List;
import java.util.Map;


import org.red5.server.api.IScope;


import com.thebitstream.comserver.feeds.IResourceFeed;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.services.IClientProxyAdapter;
import com.thebitstream.comserver.stream.util.ResourceExecutor;
/**
 * @author Andy Shaules
 * @version 1.0
 */
public interface IResourceSink {
	public static final String PROP_NODE = "node";
	public static final String PROP_ID = "id";
	public static final String PROP_DATA = "data";
	public static final String PROP_GAME = "game";
	public static final String PROP_TYPE = "type";
	public static final String PROP_RESOURCE = "resource";

	IClientProxyAdapter getProxyAdapter();
	
	void setProxyAdapter(IClientProxyAdapter adapter);
	
	void sendEvent(String method ,Map<Object,Object> object);
	
	void addSubscriber	(IComserverNode connection);
	
	void removeSubscriber(IComserverNode connection);
	
	void removeSubscriberById(String id);
	
	IComserverNode getSubscriberById(String id) ;	
	
	List<IComserverNode> getSubscribers();
	
	boolean removeFeed(IResourceFeed feed );
	
	void addFeed(IResourceFeed feed );
	
	void setFeeds(List<IResourceFeed> feeds); 
	
	List<IResourceFeed> getFeeds(); 
	
	IScope getScope();
	
	IResourceStream getStream();
	
	String getName();

	void setExecutor(ResourceExecutor resourceExecutor);

	void execute();

	String getSerializedPath();

	ResourceExecutor getExecutor();

	void close();
}
