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
//com.thebitstream.comserver.app.light.ApplicationLight
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;


import com.thebitstream.comserver.app.ComServerLt;
import com.thebitstream.comserver.nodes.IConnectionNode;

public class ApplicationLight extends ComServerLt {

	
	protected boolean onAppStart(IScope room) {
		
		return true;
	}

	
	protected boolean onAppConnect(IConnection client, IConnectionNode data) {
		
		return true;
	}

	
	protected boolean onRoomStart(IScope room) {
		
		return true;
	}
	
	protected void onRoomJoin(IScope room, IConnection client) {
		
		
	}
	
	protected void onRoomPart(IScope room, IConnection client) {
		
		
	}
	
	protected void onRoomStop(IScope room) {
		

	}
	
	protected void onAppDisconnect(IConnection client, IConnectionNode data) {
		
	}
	
	protected void onAppStop(IScope room) {
		
		
	}

}
