package comdemo.internal.app;

import java.util.Iterator;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.io.utils.ObjectMap;
import org.red5.server.api.scope.IScope;
import org.red5.server.net.rtmp.event.Invoke;
import org.red5.server.net.rtmp.event.Notify;

import com.thebitstream.comserver.app.ComServer;
import com.thebitstream.comserver.feeds.RoomTicker;
import com.thebitstream.comserver.identity.SimpleIdReader;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.services.ExternalService;
import com.thebitstream.comserver.stream.IResourceSink;
import comdemo.internal.feeds.FLVFeed;

public class Application extends ComServer {


	@Override
	protected boolean onAppStart(IScope scope) {
		this.setIdGenerator(new SimpleIdReader());
		return true;
	}
	
	@Override
	protected boolean canSubscribe(IResourceSink arg0, IComserverNode arg1) {
		
		return true;
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
		sink.addFeed(new RoomTicker());
		return true;
	}

	@Override
	protected boolean configureRoomServices(IScope scope) {
		//register any needed room services
		
		return true;
	}

	@Override
	protected void destroyFeed(IResourceSink arg0) {
		
	}

	@Override
	protected void destroyRoomServices(IScope arg0) {
		//unregister room services

	}


}
