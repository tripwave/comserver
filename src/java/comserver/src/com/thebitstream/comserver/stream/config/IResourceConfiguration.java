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
/**
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
package com.thebitstream.comserver.stream.config;

import java.util.List;

import com.thebitstream.comserver.feeds.IResourceFeed;
import com.thebitstream.comserver.nodes.IComserverNode;

/**
 * @author Andy Shaules
 * @version 1.0
 */
public interface IResourceConfiguration {
	
	String getName();
	
	String getContext();
	
	List<IResourceFeed> getFeeds();
	
	List<IComserverNode> getNodes();
	
	void setName(String name);
	
	void setContext(String path);
	
	void setFeeds(List<IResourceFeed> feeds);
	
	void setNodes(List<IComserverNode> nodes);	
}
