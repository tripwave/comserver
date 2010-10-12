package comdemo.internal.sharedObject;

import java.util.List;

import org.red5.server.api.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.so.ISharedObject;
import org.red5.server.api.so.ISharedObjectSecurity;

import com.thebitstream.comserver.nodes.IConnectionNode;
import com.thebitstream.comserver.stream.IResourceSink;

public class SOHandler implements ISharedObjectSecurity {

	@Override
	public boolean isConnectionAllowed(ISharedObject so) {
		IConnectionNode node=(IConnectionNode) Red5.getConnectionLocal().getAttribute(IResourceSink.PROP_NODE);
		node.getNodeData().put("sharedObject",so.getName());
		return true;
	}

	@Override
	public boolean isCreationAllowed(IScope scope, String name, boolean persistent) {
		
		return true;
	}

	@Override
	public boolean isDeleteAllowed(ISharedObject so, String key) {
		
		IConnectionNode node=(IConnectionNode) Red5.getConnectionLocal().getAttribute(IResourceSink.PROP_NODE);

		
		if(key.equals(node.getNodeId())){
			return true;
		}
		
		//admin type from TokenGen.php
		if(node.getNodeType().equals("2")){
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isSendAllowed(ISharedObject so, String message, List<?> arguments) {
		
		return true;
	}

	@Override
	public boolean isWriteAllowed(ISharedObject so, String key, Object value) {
		
		IConnectionNode node=(IConnectionNode) Red5.getConnectionLocal().getAttribute(IResourceSink.PROP_NODE);
		String Owner=node.getNodeId();
		if(key.equals(Owner)){
			return true;
		}
		
		return false;
	
	}

}
