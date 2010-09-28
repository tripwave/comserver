package comdemo.internal.app;

import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.io.utils.ObjectMap;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.net.rtmp.event.Notify;

import com.thebitstream.comserver.app.ComServer;
import com.thebitstream.comserver.identity.SimpleIdReader;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.nodes.IConnectionNode;
import com.thebitstream.comserver.stream.IResourceSink;


import comdemo.internal.feeds.AVFeed;
import comdemo.internal.media.Handler;
import comdemo.internal.sharedObject.SOHandler;


public class Application extends ComServer {

	AVFeed feed;
	
	@Override
	protected boolean onAppStart(IScope scope) {
		feed=new AVFeed();
		
		setIdGenerator(new SimpleIdReader());
		
		registerSharedObjectSecurity(new SOHandler(feed));
		
		return true;
	}
	
	@Override
	protected boolean canSubscribe(IResourceSink arg0, IComserverNode arg1) {
		return true;
	}

	public void streamBroadcastStart(IBroadcastStream stream){
		
		
		IConnectionNode node = (IConnectionNode) Red5.getConnectionLocal().getAttribute(IResourceSink.PROP_NODE);

		System.out.println("Broadcast started: "+node.getNodeId());
		//flex demo form data @ node.getNodeData()
		System.out.println("for group: "+node.getNodeData().get("group"));
		
		System.out.println("at host: "+node.getNodeData().get("host"));
		
		feed.addChannel(node.getNodeId());
		
		feed.setChannel(node.getNodeId());
		
		Handler handler = new Handler(node.getNodeId(), feed,stream.getCreationTime());
		
		stream.addStreamListener(handler);
	}
	
	public void appDisconnect(IConnection client){
		
		IConnectionNode node = (IConnectionNode) client.getAttribute(IResourceSink.PROP_NODE);
		feed.removeChannel(node.getNodeId());	
	}
	
	@Override
	protected boolean configureFeed(IResourceSink sink) {
	
		Map<Object,Object>meta=new ObjectMap<Object,Object>();
		IoBuffer buf = IoBuffer.allocate(1024);
		buf.setAutoExpand(true);
		meta.put("canSeekToEnd", false);
		Output out = new Output(buf);
		out.writeString("onMetaData");		
		out.writeMap(meta, new Serializer());
		buf.flip();		
		sink.getStream().setMetaDataEvent(new Notify(buf));
		
		sink.addFeed(feed);
				
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
	protected void destroyRoomServices(IScope arg0) {
	}
}
