package com.thebitstream.comserver.encoder;
//com.thebitstream.comserver.encoder.DemoLtId
import org.red5.server.api.Red5;

import com.thebitstream.comserver.identity.IClientIdentity;

public class DemoLtId implements IClientIdentity {

	@Override
	public String readId(Object[] params) {
		//return built in id. This could perform sql lookups or what have you
		return Red5.getConnectionLocal().getClient().getId();
	}

	@Override
	public String readType(Object[] params) {
		//ACL level.
		return "0";
	}

}
