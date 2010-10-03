package comdemo.internal.feeds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thebitstream.comserver.feeds.IResourceFeed;
import com.thebitstream.comserver.nodes.IComserverNode;

import com.thebitstream.comserver.stream.IResourceSink;
import comdemo.internal.feeds.Jinngine.CapsuleExample;

public class JinngineEngine implements IResourceFeed, Runnable {

	private List<IResourceSink> sinks = new ArrayList<IResourceSink>();

	private CapsuleExample cap;

	private boolean doRun;

	private long frame=0;
	
	public JinngineEngine(){
		cap=new CapsuleExample();
	}
		
	@Override
	public void addResourceSink(IResourceSink arg0) {
		sinks.add(arg0);		
	}

	@Override
	public void execute(IResourceSink arg0) {
	}

	@Override
	public void onClientAdded(IComserverNode arg0) {
		cap.createBody(arg0.getNodeId(),arg0.getNodeData());	
	}

	@Override
	public void onClientRemoved(IComserverNode arg0) {	
		cap.removeBody(arg0.getNodeId(),arg0.getNodeData());
	}

	@Override
	public void removeResourceSink(IResourceSink arg0) {
		doRun=false;
		sinks.remove(arg0);
	}

	@Override
	public void run() {
		doRun=true;
		while(doRun){
			
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {	
				e.printStackTrace();
			}
					
			frame++;
			
			Map<Object,Object> points= new HashMap<Object,Object>();
			List<Map<Object,Object>> vect=cap.tick();
			points.put("frame", frame);
			points.put("bodies", vect);
				if(doRun){
					sinks.get(0).sendEvent("jinngine",points );
				}
		}
	}

	public synchronized CapsuleExample getCap() {
		return cap;
	}

	public void setCap(CapsuleExample cap) {
		this.cap = cap;
	}
}
