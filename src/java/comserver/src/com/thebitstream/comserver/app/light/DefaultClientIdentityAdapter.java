package com.thebitstream.comserver.app.light;

import org.red5.server.api.Red5;

import com.thebitstream.comserver.identity.IClientIdentity;

public class DefaultClientIdentityAdapter implements IClientIdentity {

	@Override
	public String readId(Object[] params) {
		return Red5.getConnectionLocal().getClient().getId();
		
	}

	@Override
	public String readType(Object[] params) {
		return "0";
	}

}
