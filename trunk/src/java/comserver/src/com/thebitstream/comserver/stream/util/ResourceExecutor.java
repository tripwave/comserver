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

package com.thebitstream.comserver.stream.util;

import org.red5.server.api.scheduling.IScheduledJob;
import org.red5.server.api.scheduling.ISchedulingService;

import com.thebitstream.comserver.stream.IResourceSink;

/**
 * The courtesy ticker.
 * @author Andy Shaules
 * @version 1.0
 */
public class ResourceExecutor implements IScheduledJob {
	
	private IResourceSink game;
	private String jobName;
	public ResourceExecutor(IResourceSink game2){
		this.game=game2;
		game2.setExecutor(this);
	}
	
	@Override
	public void execute(ISchedulingService service) throws CloneNotSupportedException {
		game.execute();	
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}
}
