package comdemo.internal.app;


import org.red5.server.api.IScope;

import com.thebitstream.comserver.app.ComServer;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.stream.IResourceSink;
import comdemo.internal.feeds.AxisCamera;


public class Application extends ComServer {


	
	@Override
	protected boolean onAppStart(IScope scope) {		
		
		return true;
	}

	
	@Override
	protected boolean configureFeed(IResourceSink sink) {
		AxisCamera cam=new AxisCamera();
		
		Thread t=new Thread(cam);
		
		sink.addFeed(cam);
		
		t.start();
		
		return true;
	}

	@Override
	protected boolean configureRoomServices(IScope scope) {
		return true;
	}

	@Override
	protected void destroyFeed(IResourceSink arg0) {		
	}
	
	@Override
	protected boolean canSubscribe(IResourceSink arg0, IComserverNode arg1) {
		return true;
	}
	
	@Override
	protected void destroyRoomServices(IScope arg0) {
	}

}