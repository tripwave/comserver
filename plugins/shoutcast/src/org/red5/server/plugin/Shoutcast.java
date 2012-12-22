package org.red5.server.plugin;



import org.red5.server.api.scope.IScope;
import org.red5.server.plugin.shoutcast.marshal.ICYMarshal;
import org.red5.server.plugin.shoutcast.stream.NSVConsumer;

/**
 * Provides a means to stream media via NSV and Shoutcast.
 * For use with NSVCap, Winamp Shoutcast DSP, and Shoutcast DNAS.
 * <p>Usage:<br/><br/>
 * To open a port and act like a Shoutcast server and stream the content to the flash player, use the following static method. Pass the IScope where you want the stream to be accesed from, the name of the output stream,
 * with the port, and password for the incoming encoder.<br/><br/><br/>
 * {@code  NSVConsumer nsv=Shoutcast.openServerPort(app, "red5StreamDemo", 8001,"changeme");}<br/><br/>
 *  // Implement IStatusListener interface for hooking into connection status<br/>
 *	{@code nsv.getMarshal().addStatusListener(handler);}<br/><br/>
 *	// Accessing IStreamListener packet events<br/>
 *	{@code nsv.getMarshal().getStream().addStreamListener(handler)};<br/><br/>
 * <br/><br/><br/>
 * To connect to a Shoutcast server as a client, and stream the contents through red5, use the following static method. Pass the IScope and name for the output stream,
 *  as well as the uri that the shoutcast server resides at.<br/>
 * <br/><br/>
 * {@code NSVConsumer nsv=Shoutcast.openExternalURI(app, "red5StreamDemo","http://localhost:8000/;stream.nsv","UTF-8");}
 * </p>
 * 
 * @author Wittawas Nakkasem (vittee@hotmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 * @author Paul Gregoire (mondain@gmail.com)
 * @version 0.9
 */
public class Shoutcast  {

	/**
	 * Pass through the H264 byte stream as annex B type, with '001' frame delimiters.
	 * The video packets, although dispatched in RTMP format, will not be playable by the flash player.
	 * However, if you remove the rtmp byte headers from the video frame, it will be playable in Silverlight. 
	 */
	public static boolean KEEP_NAL_FORMAT=false; 
	/**
	 * Build version
	 */
	public final static String version = "@(#)1.0.0118 (on:       23.06.2011 21:33)@";
	
	private Shoutcast(){}
	
	/**
	 * Create a thread to listen for a connection from nsv or winamp shoutcast dsp encoders.
	 * @param outputScope the stream is registered in.
	 * @param outputName stream name in output scope.
	 * @param port Port to open.
	 * @param password Pass word to accept.
	 * @return The running thread wrapper.
	 */
	public static NSVConsumer openServerPort(IScope outputScope, String outputName,int port,String password){
		ICYMarshal marsh=new ICYMarshal(outputScope,outputName,KEEP_NAL_FORMAT);				
		NSVConsumer nsv=new NSVConsumer(NSVConsumer.SERVER_MODE,marsh);
		nsv.setPort(port);		
		nsv.setPassword(password);
		nsv.start();
		return nsv;
	}
	/**
	 * Create a thread to subscribe to a shoutcast server. Host format "http://host:port/;stream.nsv".
	 * Note. The ';' is not a typo. 
	 * @param outputScope The stream is registered to.
	 * @param outputName The output stream name.
	 * @param host	The url to subcribe to.
	 * @param charSet The charset of the in-stream meta data.
	 * 
	 * @return The running thread wrapper.
	 */
	public static NSVConsumer openExternalURI(IScope outputScope, String outputName, String host, String charSet){
		ICYMarshal marsh=new ICYMarshal(outputScope,outputName,KEEP_NAL_FORMAT);				
		NSVConsumer nsv=new NSVConsumer(NSVConsumer.CLIENT_MODE,marsh,host);			
		nsv.setMetaCharset(charSet);
		nsv.start();
		return nsv;
	}	
	
}