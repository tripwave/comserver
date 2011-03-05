package com.thebitstream.comserver.app.light;

import com.thebitstream.comserver.services.IClientProxyAdapter;
import com.thebitstream.comserver.services.IPacketListener;
import com.thebitstream.comserver.services.IServiceHandler;
import com.thebitstream.comserver.stream.IResourceSink;

public class ProxyAdapterLight implements IClientProxyAdapter {

	
	public void addPacketListener(IPacketListener listener) {
	}

	
	public IServiceHandler getHandler() {	
		return null;
	}

	
	public IResourceSink getResource() {
		
		return null;
	}

	
	public void removePacketListener(IPacketListener listener) {	
	}

}
