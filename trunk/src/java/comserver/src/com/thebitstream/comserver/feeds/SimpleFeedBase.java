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

package com.thebitstream.comserver.feeds;


import com.thebitstream.comserver.stream.IResourceSink;

/**
 * Simple type for feeds with one resource sink at a time.
 * 
 * @author Andy Shaules
 * @version 1.0
 */
public abstract class SimpleFeedBase implements IResourceFeed {

	
	
	protected IResourceSink resourceSink;
	

	@Override
	public void addResourceSink(IResourceSink sink) {
		this.resourceSink=sink;

	}


	
	@Override
	public void removeResourceSink(IResourceSink sink) {
		
		resourceSink=null;
	}
}
