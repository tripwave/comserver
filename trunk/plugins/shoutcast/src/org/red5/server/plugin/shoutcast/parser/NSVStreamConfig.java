package org.red5.server.plugin.shoutcast.parser;

import java.util.ArrayList;

/**
 * Individual stream configuration generated from the parser when the shoutcast header is received.

 * @author Andy Shaules (bowljoman@hotmail.com)
 * 
 */
public class NSVStreamConfig {
	public int streamId = -1;
	
	public String video_format = null;

	public String audio_format = null;

	public int video_width = 0;

	public int video_height = 0;

	public double frame_rate = 0;

	public int frame_rate_encoded = 0x0;

	public long total_frames = 0;

	public long start_time = 0;

	public ArrayList<NSVFrame> frames = new ArrayList<NSVFrame>();

	public synchronized void writeFrame(NSVFrame frame) {
		
		frame.timeStamp=(long)  ( total_frames *   (1.0 / frame_rate)  * 1000 );
		frames.add(frame);
		total_frames++;
	}

	public synchronized NSVFrame readFrame() {
		return frames.remove(0);
	}

	public synchronized boolean hasFrames() {
		return (frames.isEmpty()) ? false : true;
	}

	public synchronized int count() {
		return frames.size();
	}

	public synchronized void flush() {
		frames.clear();
	}
}
