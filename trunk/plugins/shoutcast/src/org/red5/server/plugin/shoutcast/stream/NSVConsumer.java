package org.red5.server.plugin.shoutcast.stream;


import org.red5.server.plugin.shoutcast.IICYMarshal;
import org.red5.server.plugin.shoutcast.parser.IcyConsumer;


/**
 *  The NSVConsumer will open a port and wait for the Winamp Shoutcast DSP or NSV encoder. 
 *	Or it will connect to a Shoutcast server and subscribe.
 * @author Wittawas Nakkasem (vittee@hotmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)

 */
public class NSVConsumer implements  Runnable {

	public static int SERVER_MODE = 1;

	public static int CLIENT_MODE = 0;

	private IICYMarshal handler;

	private Thread thread = new Thread(this);


	private int _port;

	public int getPort() {
		return _port;
	}

	private String _pass = "changeme";

	private IcyConsumer icy;
	
	private int _mode = 1;

	private String _host;

	private String metaCharset;

	public NSVConsumer(int serverType, IICYMarshal pHandler, String host) {
		handler = pHandler;
		_host = host;
		_mode = serverType;
	}

	public NSVConsumer(int serverType, IICYMarshal pHandler) {
		handler = pHandler;
		_host = "";
		_mode = serverType;
	}

	public IICYMarshal getMarshal() {
		return handler;
	}


	public void setHost(String val) {
		_host = val;
	}
	/**
	 * Returns the {@code ServerTypes} value of current running status.
	 * @return int
	 */
	public int getMode() {
			return _mode;
	}

	public void setPort(int val) {
		_port = val;
	}

	public void setPassword(String val) {
		_pass = val;
	}
	
	public void run() {
		icy=new IcyConsumer(_host, handler);
		icy.setPort(_port);
		icy.setPassword(_pass);
		icy.set_mode(_mode);
		icy.setMetaCharset(metaCharset);
		icy.start();
	}

	public void start() {
		
		
		thread.start();
	}

	public void stop() {
		if(icy != null)
		{
			icy.stop();
		}
		
		if(handler != null)
		{
			handler.stop();
		}		
		
	
	}

	public boolean isConnected() {

		if (icy != null)
			return handler.isConnected();
		else
			return false;
	}

	public void setMetaCharset(String charSet) {
		
		this.metaCharset=charSet;
	}



}
