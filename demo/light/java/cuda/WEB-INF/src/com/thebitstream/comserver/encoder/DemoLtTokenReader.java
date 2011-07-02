package com.thebitstream.comserver.encoder;
//com.thebitstream.comserver.encoder.DemoLtTokenReader
import com.thebitstream.comserver.auth.IAuthorize;

public class DemoLtTokenReader implements IAuthorize {


	public boolean appConnect(Object[] params) {
		//read the net connection parameters
		//return true if valid.
		return true;
	}

}
