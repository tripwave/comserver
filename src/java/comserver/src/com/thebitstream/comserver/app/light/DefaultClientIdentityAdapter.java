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
