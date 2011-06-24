package org.red5.server.plugin.shoutcast;



import java.util.Map;

import org.red5.server.api.IScope;
import org.red5.server.plugin.shoutcast.marshal.transpose.AudioFramer;
import org.red5.server.plugin.shoutcast.marshal.transpose.VideoFramer;
import org.red5.server.plugin.shoutcast.stream.ICYStream;

/**
 * Provides access to running data.
 * 
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public interface IICYMarshal extends IICYHandler {
	
	public AudioFramer getAudioFramer();

	public VideoFramer getVideoFramer();

	public IScope getScope();

	public ICYStream getStream();

	public String getContentType();

	public String getAudioType();

	public String getVideoType();
	
	public Map<String,Object> getMetadata();
	
	public void addStatusListener(IStatusListener listener);

	public boolean removeStatusListener(IStatusListener listener);

	public boolean isConnected();

	
}
