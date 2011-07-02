package com.thebitstream.comserver.app.light;
//com.thebitstream.comserver.app.light.ApplicationLight
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;

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
