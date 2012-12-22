package comdemo.internal.sharedObject;

import java.util.List;

import org.red5.server.api.scope.IScope;
import org.red5.server.api.so.ISharedObject;
import org.red5.server.api.so.ISharedObjectSecurity;

import comdemo.internal.feeds.AVFeed;

public class SOHandler implements ISharedObjectSecurity
{
	private AVFeed feed;

	public SOHandler(AVFeed feed) {
		this.feed=feed;
	}

	@Override
	public boolean isConnectionAllowed(ISharedObject so) {
		
		return true;
	}

	@Override
	public boolean isCreationAllowed(IScope scope, String name, boolean persistent) {
		
		return true;
	}

	@Override
	public boolean isDeleteAllowed(ISharedObject so, String key) {
		
		return true;
	}

	@Override
	public boolean isSendAllowed(ISharedObject so, String message, List<?> arguments) {
		
		return true;
	}

	@Override
	public boolean isWriteAllowed(ISharedObject so, String key, Object value) {
	
		if(key.equals("channel")){
			if(feed.channels.contains(value)){
				feed.setChannel(value.toString());
			}else {
				feed.getRandomChan();
			}		
			
		}else{
			
		}
		
		return true;
	}

}
