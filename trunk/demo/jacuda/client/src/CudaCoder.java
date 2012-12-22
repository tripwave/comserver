
import java.io.IOException;

import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.stream.StreamingProxy;
import org.red5.server.stream.message.RTMPMessage;

public class CudaCoder implements ICudaListener {
	private Capture capture;
	private IGForce parser;
	private ICudaListener publisher;
	
	private CudaProxy streamer;
	
	private int deviceIndex=0;
	private int duration=60;
	private String app="cuda";
	private String host="127.0.0.1";
	private int port=1935;
	private String publishName="cuda";
	
	public CudaCoder(){
		parser=new VideoFramer();
		publisher=this;
		streamer=new CudaProxy();
		
	}
	
	public void initCapture(){
			
		streamer.init();
		
		streamer.setApp(app);
		streamer.setHost(host);
		streamer.setPort(port);
		streamer.start(publishName, "live", new Object[]{});
		capture= new Capture();
		
		parser.setOutput(publisher);
		
		capture.framer=parser;
		
		capture.initiateCapture(deviceIndex,duration);

		capture.stopCapture();
		
		capture=null;
		
		streamer.stop();

	}
	
	public void stopCapture(){
		
		if(capture!=null){
			capture.stopCapture();
			capture=null;
		}
	}

	public void dispatchEvent(IRTMPEvent packet) {
		System.out.println("dispatchEvent");
		
		if(streamer==null)
			return;
		
		RTMPMessage message= RTMPMessage.build(packet);
		
		try {
			streamer.pushMessage(null,message );
		} catch (IOException e) {
			 stopCapture();
		}
		
	}

	public Capture getCapture() {
		return capture;
	}

	public void setParser(IGForce parser) {
		this.parser = parser;
	}

	public IGForce getParser() {
		return parser;
	}

	public void setPublisher(ICudaListener publisher) {
		this.publisher = publisher;
	}

	public ICudaListener getPublisher() {
		return publisher;
	}

	public void setDeviceIndex(int deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	public int getDeviceIndex() {
		return deviceIndex;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPublishName() {
		return publishName;
	}

	public void setPublishName(String publishName) {
		this.publishName = publishName;
	}

	public CudaProxy getStreamer() {
		return streamer;
	}

	public static void main(String args[]){
		CudaCoder coder=new CudaCoder();
		coder.initCapture();
	}
	
	
	
}
