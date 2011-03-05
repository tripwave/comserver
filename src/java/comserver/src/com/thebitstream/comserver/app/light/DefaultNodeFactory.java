package com.thebitstream.comserver.app.light;

import java.util.Map;

import com.thebitstream.comserver.app.INodeDataFactory;
import com.thebitstream.comserver.nodes.data.INodeData;
import com.thebitstream.comserver.nodes.data.NodeData;

public class DefaultNodeFactory implements INodeDataFactory {
	
	@SuppressWarnings("unchecked")
	public INodeData createData(String id, Object clientParam) {
		NodeData data=new NodeData(id);
		
		data.setData((Map<Object, Object>) clientParam);
		
		return data;
	}	
}
