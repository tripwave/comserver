package org.red5.server.plugin.shoutcast;


import org.red5.server.api.event.IEvent;

/**
 * 
 * @author Andy Shaules (bowljoman@hotmail.com)
 *
 */
public interface IICYEventSink {
	public void dispatchEvent(IEvent event);

	public void reset();
}
