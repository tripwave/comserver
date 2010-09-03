package comdemo.internal.app;

import java.util.Iterator;

import org.red5.server.api.IScope;

import com.thebitstream.comserver.app.ComServer;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.stream.IResourceSink;
import comdemo.internal.feeds.FLVFeed;

public class Application extends ComServer {

	private FLVFeed globalFeed;

	@Override
	protected boolean onAppStart(IScope scope) {
		
		globalFeed = new FLVFeed(scope);
		
		Iterator<String>files=getListOfAvailableFLVs(scope).keySet().iterator();
		
		while(files.hasNext()){
			String file=files.next();
			System.out.println(file);
			globalFeed.files.add(file);
		}
		
		globalFeed.start();
		
		
		return true;
	}
	
	@Override
	protected boolean canSubscribe(IResourceSink arg0, IComserverNode arg1) {
		
		return true;
	}

	@Override
	protected boolean configureFeed(IResourceSink sink) {
		
		sink.addFeed(globalFeed);
		
		return true;
	}

	@Override
	protected boolean configureRoomServices(IScope arg0) {
		//register any needed room services
		return true;
	}

	@Override
	protected void destroyFeed(IResourceSink arg0) {
		//sink will remove its own feeds
	}

	@Override
	protected void destroyRoomServices(IScope arg0) {
		//unregister room services

	}


}
