package comdemo.internal.feeds;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.red5.server.api.event.IEvent;
import org.red5.server.net.rtmp.event.VideoData;

import com.thebitstream.comserver.feeds.IResourceFeed;
import com.thebitstream.comserver.nodes.IComserverNode;
import com.thebitstream.comserver.stream.IResourceSink;

public class AxisCamera implements Runnable, IResourceFeed {

	public String jpgURL = "http://thebitstream.com/ff.jpg";

	private List<IResourceSink> sinks = new ArrayList<IResourceSink>();

	private static final long serialVersionUID = -3157773137595280067L;

	private Thread appletThread;

	private boolean doRun;

	public void run() {
		try {

			readStream();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {

	}

	public void stop() {
		doRun = false;

	}

	public void readStream() {
		doRun = true;

		try {

			while (doRun) {

				sendTag();

				Thread.sleep(1000);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void sendTag() throws IOException {

		BufferedImage ouput = ImageIO.read(new URL(jpgURL));

		if (ouput == null)
			return;

		edu.mit.star.flv.impl.VideoData data = new edu.mit.star.flv.impl.VideoData(ouput);

		Data buf = new Data();

		try {

			data.write(buf.data, true);

			System.out.println(buf.data.limit());

			IEvent event = new VideoData(buf.data);

			for (int i = 0; i < sinks.size(); i++) {

				sinks.get(i).getStream().dispatchEvent(event);
			}

			buf.dispose();
			return;

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void addResourceSink(IResourceSink arg0) {
		sinks.add(arg0);
	}

	@Override
	public void removeResourceSink(IResourceSink arg0) {
		sinks.remove(arg0);
		stop();
	}

	@Override
	public void execute(IResourceSink arg0) {

	}

	@Override
	public void onClientAdded(IComserverNode arg0) {

	}

	@Override
	public void onClientRemoved(IComserverNode arg0) {

	}

}
