package com.thebitstream.comserver.app.light;

import com.thebitstream.comserver.auth.IAuthorize;

public class DefaultAuthorizationAdapter implements IAuthorize {

	@Override
	public boolean appConnect(Object[] params) {
		return false;
	}

}
