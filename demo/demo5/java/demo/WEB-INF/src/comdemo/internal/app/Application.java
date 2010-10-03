package comdemo.internal.app;

import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.io.utils.ObjectMap;

import org.red5.server.api.IScope;


import org.red5.server.net.rtmp.event.Notify;

import com.thebitstream.comserver.app.ComServer;
import com.thebitstream.comserver.identity.SimpleIdReader;
import com.thebitstream.comserver.nodes.IComserverNode;

import com.thebitstream.comserver.stream.IResourceSink;
import comdemo.internal.feeds.JinngineEngine;
import comdemo.internal.service.BumpService;

public class Application extends ComServer {

	@Override
	protected boolean onAppStart(IScope scope) {		
		setIdGenerator(new SimpleIdReader());	
		return true;
	}

	@Override
	protected boolean configureFeed(IResourceSink sink) {
	
	//set up meta
		Map<Object,Object>meta=new ObjectMap<Object,Object>();
		IoBuffer buf = IoBuffer.allocate(1024);
		buf.setAutoExpand(true);
		meta.put("canSeekToEnd", false);
		Output out = new Output(buf);
		out.writeString("onMetaData");		
		out.writeMap(meta, new Serializer());
		buf.flip();		
		sink.getStream().setMetaDataEvent(new Notify(buf));
				
	//set up jinngine feed
		JinngineEngine task=new JinngineEngine();
		
		sink.getScope().registerServiceHandler("jinngine"+sink.getName(), new BumpService(task));
		
		sink.addFeed(task);;
		
		Thread thread=new Thread(task);	
		
		thread.start();
		
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