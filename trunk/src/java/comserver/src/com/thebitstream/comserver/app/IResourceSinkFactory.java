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

package com.thebitstream.comserver.app;

import org.red5.server.api.scope.IScope;

import com.thebitstream.comserver.stream.IResourceSink;

/**
 * The resource sink is the object that is responsible for broadcasting all invocations.
 * <p><This framework accomplishes the task using flv streams. 
 * The IResourceSinkFactory interface generates the IResourceSink pipeline to the clients.</p>
 * @author Andy Shaules
 * @version 1.0
 */
public interface IResourceSinkFactory {
	public IResourceSink createResourceSink(IScope room,String name);
}
