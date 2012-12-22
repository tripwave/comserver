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

package com.thebitstream.comserver.stream;

import java.util.List;
import java.util.Map;


import org.red5.server.api.scope.IScope;


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
