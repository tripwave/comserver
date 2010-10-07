package comdemo.internal.service;

import java.util.Map;

import org.red5.server.api.Red5;

import com.thebitstream.comserver.nodes.IConnectionNode;
import com.thebitstream.comserver.stream.IResourceSink;

import comdemo.internal.feeds.JinngineEngine;

public class BumpService {
	JinngineEngine sim;
	public BumpService( JinngineEngine engine){
		sim=engine;
	}
	public int bumpMe(Map<Object,Object> data){
		IConnectionNode node= (IConnectionNode) Red5.getConnectionLocal().getAttribute(IResourceSink.PROP_NODE);
		sim.getCap().addBump(node,data);
		return 1;
	}
}
