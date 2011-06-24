package org.red5.server.plugin.shoutcast;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.server.api.stream.IBroadcastStream;


/**
 * A registered IStatusListener will receive connection event notifications and auxiliary data.
 * Use this interface to hook into publish starts and stops.
 * @author Andy Shaules
 *
 */
public interface IStatusListener {
	/**
	 * Dispatched on disconnection event.
	 */
	static String EVENT_DISCONNECTED = "eventDisconnected";
	/**
	 * Dispatched on connection event.
	 */
	static String EVENT_CONNECTED = "eventConnected";
    /**
     * 
     * @param stream The ICYStream dispatching the event.
     * @param status Either IStatusListener.EVENT_DISCONNECTED, or IStatusListener.EVENT_CONNECTED
     */
	void onStatus(IBroadcastStream stream, String status);
    /**
     * See the <a href="http://ultravox.aol.com/NSVFormat.rtf">NSVFormat</a> file for known types. Or develop your own.
     * @param stream The ICYStream dispatching the event.
     * @param fourCC The multi media IO four character code.
     * @param buffer The data. 
     */
	void onAuxData(IBroadcastStream stream, String fourCC, IoBuffer buffer);
}
