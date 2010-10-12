package comdemo.internal.app;

import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.io.utils.ObjectMap;

import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.so.ISharedObject;


import org.red5.server.net.rtmp.event.Notify;

import com.thebitstream.comserver.app.ComServer;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.nodes.IConnectionNode;

import com.thebitstream.comserver.stream.IResourceSink;
import comdemo.internal.identity.FaceBookToken;
import comdemo.internal.sharedObject.SOHandler;



public class Application extends ComServer {

	FaceBookToken token;
	
	private String salt;
	@Override
	protected boolean onAppStart(IScope scope) {		
		
		token=new FaceBookToken();
		registerSharedObjectSecurity(new SOHandler());
		token.setSalt(salt);
		
		setTokenReader(token);
		
		setIdGenerator(token);	
		
		return true;
	}

	public void appDisconnect(IConnection conn){
		IConnectionNode node=(IConnectionNode) conn.getAttribute(IResourceSink.PROP_NODE);
		
		if(node == null){
			return;
		}
		
		String soName= (String) node.getNodeData().get("sharedObject");
		if(soName != null){
			ISharedObject so=getSharedObject(conn.getScope(), "comdemo");
			if(so != null){

				if(so.hasAttribute(node.getNodeId()));{
					so.beginUpdate();
					so.removeAttribute(node.getNodeId());
					so.endUpdate();
				}
			}
		}
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


	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
}