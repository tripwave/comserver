package comdemo.internal.filter;

import java.util.Map;

import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.services.IPacketListener;

public class DemoSniffer implements IPacketListener {

	@Override
	public boolean processPacket(IComserverNode arg0, Map<Object, Object> arg1) {
		System.out.println("processPacket 1 "+arg0.getNodeId());
		return true;
	}

	@Override
	public boolean processPacket(IComserverNode arg0, String arg1, Map<Object, Object> arg2) {
		//client is attempting to inject script data.
		System.out.println("processPacket 2 " + arg0.getNodeId());
		return true;
	}

	@Override
	public boolean processPacket(IComserverNode arg0, String arg1, Map<Object, Object> arg2, Map<Object, Object> arg3) {
		//client is attempting to inject script data and update Node data
		System.out.println("processPacket 3 " + arg1);
		return true;
	}

}
