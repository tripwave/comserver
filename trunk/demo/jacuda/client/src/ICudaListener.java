
import org.red5.server.net.rtmp.event.IRTMPEvent;

public interface ICudaListener {
	void dispatchEvent(IRTMPEvent packet);
}
