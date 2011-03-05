package com.thebitstream.comserver.app.light;

import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;

import com.thebitstream.comserver.app.ComServerLt;
import com.thebitstream.comserver.nodes.IConnectionNode;

public class ApplicationLight extends ComServerLt {

	@Override
	protected boolean onAppConnect(IConnection client, IConnectionNode data) {
		
		return true;
	}

	@Override
	protected void onAppDisconnect(IConnection client, IConnectionNode data) {
	
	}

	@Override
	protected boolean onAppStart(IScope room) {
		
		return true;
	}

	@Override
	protected void onAppStop(IScope room) {
		
	
	}

	@Override
	protected boolean onRoomStart(IScope room) {
		
		return true;
	}

	@Override
	protected void onRoomStop(IScope room) {
		

	}

}
