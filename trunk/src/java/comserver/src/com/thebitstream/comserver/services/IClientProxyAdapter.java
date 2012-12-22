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

package com.thebitstream.comserver.services;

import com.thebitstream.comserver.stream.IResourceSink;
/**
 * The framework remote client adapter API.
 * <p>The main purpose of this interface is to add packet listeners.
 * Using IPacketListener, packets sent to the Framework can be intercepted before flv injection. 
 * Application programmers that replace the client proxy code or flv container as invocation method implement this interface.</p>
 * 
 * @author Andy Shaules
 * @version 1.0
 */
public interface IClientProxyAdapter {
	void addPacketListener(IPacketListener listener );
	void removePacketListener(IPacketListener listener );
	IResourceSink getResource();
	IServiceHandler getHandler();
}
