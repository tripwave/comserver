package org.red5.server.plugin.shoutcast.parser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.plugin.shoutcast.IICYHandler;
import org.slf4j.Logger;


/**
 * @author Wittawas Nakkasem (vittee@hotmail.com)
 * @author Andy Shaules
 *
 */
public class IcyConsumer implements Runnable {

	private final Logger log = Red5LoggerFactory.getLogger(this.getClass(), "shoutcast");

	private boolean mKeepRunning = false; 
	private String url;
	private IICYHandler handler;
	private Map<String, String> headers = new HashMap<String, String>();	
	private Map<String,Object> meta = new HashMap<String,Object>();
	private enum State {httpAbort, httpReply, httpHeader, httpBody,dnasPass};
	private State state = State.httpReply;
	private int metaInt = 0, metaAcc = 0;
	private int metaLen = 0;
	private boolean reconnectOnFail = true;
	private String metaCharset = "UTF-8"; 
	private Thread thread = new Thread(this);
	private Socket client;
	private InputStream in=null;
	private ServerSocket outSock;
	private boolean connected=false;	
	private boolean verified=true;
	private int _mode=0;
	private int port=8001;
	private String password="changeme";	
	private boolean itsok=false;
	private AdminHandler admin;
	private Thread adminThread;
	private long timoutTicks;
	
	public IcyConsumer(String aUrl, IICYHandler aHandler) 
	{
		url = aUrl;
		handler = aHandler;	
	}
	
	public void sendMeta() 
	{
		meta.putAll(headers);
		handler.onMetaData(meta);
	}
	
	public void setUrl(String url) {
		log.debug("set url {}", url);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}	

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void start() {
		mKeepRunning = true;
		thread.start();
	}

	public void stop() {
		mKeepRunning = false;
		connected=false;
		state = State.httpAbort;
		
		try{			
			if(client != null)
				client.close();	
		}
		catch (Exception e){}				
			
		try{	
			if(in != null)
				in.close();	
		}
		catch (Exception e){}	
		
		try{			
			if(outSock != null)
			outSock.close();
		}
		catch (Exception e){}
		
		try{
			if(admin != null)
				admin.stop();
		}
		catch (Exception e){}	
	
	}

	private void parseHeader(String s) {		
		
		
		itsok=true;
		String[] hdr =new String[2];//    s.split(":");		
		int idx=s.indexOf(":");
		hdr[0]=s.substring(0, idx);
		hdr[1]=s.substring(idx+1);
		hdr[0]=(hdr[0].replace("icy-","")).replace("ice-", "");
		hdr[1]=hdr[1].replace("ice-", "").trim();
		headers.put(hdr[0], hdr[1]);
	}

	private void parseHTTP(String s) 
	{		
		if (state == State.httpHeader) 
		{
			parseHeader(s);
			return;			
		}
		
		String[] tokens = s.split(" ");

		if( state == State.httpHeader)
			return;

	
		for(int i=0;i<tokens.length;i++)
		{

			if( (tokens[i].indexOf("200") > -1) )
			{				
			
				state = State.httpHeader;
			}
		}
		
		if(state != State.httpHeader)
		{
			state=State.httpAbort;
			try {
				in.close();
			} catch (IOException e) {
				
			}
		}
	}

	private void processHeaders() 
	{	
		
		
		if (headers.containsKey("Content-Type")) 
		{
			String[] ct = headers.get("Content-Type").split("/");
			if ((ct.length == 2) ) 
			{
				
				handler.reset(ct[0], ct[1]);
			} 
		}
		if (headers.containsKey("content-type")) 
		{
			String[] ct2 = headers.get("content-type").split("/");
			if ((ct2.length == 2))
			{
			
				handler.reset(ct2[0], ct2[1]);			
			} 
		}

		if (headers.containsKey("metaint")) 
		{
			try 
			{
				metaAcc = metaInt = Integer.parseInt(headers.get("metaint"));
			}
			catch (Exception e) 
			{

			}
		}
		
		meta.putAll(headers);
		
		handler.onMetaData(meta);
	}

	@Override
	public void run() {
		while (mKeepRunning) {			
			process();
			log.info("mKeepRunning {}", mKeepRunning);
			try { 
				Thread.sleep(10000);
			} catch (Exception e) {
				
			}
		}
	}


	private void process() {
		state = State.httpReply;
		verified=true;
		try {	
			
			DataOutputStream out =null;
			Socket socket=null;
			

			
			switch(_mode)
			{


				case 1:
					try {
						
						if(admin == null)
						{
							
							admin=new AdminHandler(this, password, port-1);
							adminThread=new Thread(admin);
							adminThread.start();
						}
						
						verified=false;
						state = State.dnasPass;
						outSock= new ServerSocket(port);
						client=	outSock.accept();
						in=client.getInputStream();
						connected=true;
						outSock.close();
					} 
					catch (IOException e)
					{
					}		
					break;

				default:
					itsok=true;
					URL _url = new URL(url);
					int port = _url.getPort();
					socket = new Socket(_url.getHost(), (port > 0) ? port : 80);		
					out = new DataOutputStream(socket.getOutputStream());
					log.info("Connected {}", url);
					out.writeBytes("GET " + _url.getPath() + " HTTP/1.0\r\n");
					out.writeBytes("Host: " +  _url.getHost() + "\r\n");
					out.writeBytes("User-Agent:Red5/1.0\r\n");
					out.writeBytes("Icy-MetaData:1\r\n");
					out.writeBytes("\r\n");
					in = socket.getInputStream();	
					connected=true;
					
					break;

			}

			try {



				while (connected && (state != State.httpAbort)) {							

					
					
					
					if(!verified || state == State.dnasPass )
					{	
						
						if(in.available() < password.length())
						{							

						}
						else
						{
							int[] bits=new int[password.length()];

							for(int m=0;m< bits.length;m++)
							{
								bits[m]=(in.read());
							}
							if(sample(password.length(),bits).equals(password))
							{								
								verified=true;
								client.getOutputStream().write("OK2\r\nicy-caps:11\r\n\r\n".getBytes());
								client.getOutputStream().flush();
								connected=true;
								state = State.httpHeader;
							}
							else
							{
								client.getOutputStream().write("invalid password\r\n".getBytes());
								client.getOutputStream().flush();
								client.close();
								in.close();
								return;

							}
						}
					}
					
					if (state == State.httpReply || state == State.httpHeader) 
					{						
						String line="";

						while(true)
						{

							char c=(char) in.read();
							line= line + c ;

							if(line.indexOf("\n")> -1)
							{

								break;	
							}
							else
							{
								if (line.getBytes().length > 1024 * 1024 * 24)
								{
									state = State.httpAbort;
								}
							}
						}
						line=line.replace("\r", "");
						line=line.replace("\n", "");

						if (line.length() > 0 ) 
						{
							parseHTTP(line);
						} 
						else
						{  
							
							
							if(itsok)
							{
								state = State.httpBody;
								processHeaders();
							}
						}
					}
					
					if(state== State.httpBody)
					{							
						if(in.available()>0)
						{	
							timoutTicks=0;
							int i=0;
							int ln=in.available();
							byte[] data=new byte[ln];

							while(ln-- > 0 )
							{

								data[i]=(byte) in.read();
								i++;

								if(metaInt > 0 )
								{	metaAcc--;

								if(metaInt > 0 && (metaAcc == 0) )
								{
									metaAcc = metaInt;
									metaLen = in.read() * 16;											

									if(metaLen > 0 )
									{
										if(in.available() >= metaLen)
										{	
											byte[] metaBits=new byte[metaLen];												
											in.read(metaBits, 0, metaLen) ;
											notifyMetaData(new String(metaBits, metaCharset).trim());
											metaLen=0;
											metaAcc = metaInt;													
										}
										else
										{
											state = State.httpAbort;
										}
									}

									metaLen=0;
									break;
								}
								}


							}
							

							int[] pushIt=new int[i];

							for(int j=0;j< i ; j++)
							{
								pushIt[j]=data[j]&0xff;
							}

							handler.onRawData(pushIt);							
						}
						else
						{
							timoutTicks++;
							if(timoutTicks > 10 * 10 )
							{
								itsok=false;
								timoutTicks=0;
								headers.clear();
								meta.clear();
								handler.onDisconnected();
								connected=false;
								state = State.httpAbort;
							}
						}
					}

					try 
					{
						if(state == State.httpBody)
							Thread.sleep(100);
						else
							Thread.sleep(2);
					} 
					catch (Exception e) 
					{
						
					}
				}			
			} 
			finally 
			{
				
				if(socket != null)
				{
					socket.close();
				}
				
				if(in != null)
				{
					in.close();
				}
				
				if(out != null)
				{
					out.close();
				}
				
			}				
		}
		catch(Exception e)
		{
			
		
		}


		if (!reconnectOnFail) 
			mKeepRunning = false;
	}

	private void notifyMetaData(String aMeta)
	{

		if (handler != null) 
		{

			List<String> metaTokens = Arrays.asList(aMeta.split(";"));	
			Iterator<String> metaIT = metaTokens.iterator();
			while (metaIT.hasNext()) 
			{
				String[] metaToken = metaIT.next().split("=");

				if (metaToken.length < 1) 
					continue;

				if (metaToken[1].startsWith("'") && metaToken[1].endsWith("'")) 
				{
					metaToken[1] = metaToken[1].substring(1, metaToken[1].length() - 1);
				}

				meta.put(metaToken[0], metaToken[1]);

			}	

			handler.onMetaData(meta);
		}
	}

	public void setMetaCharset(String metaCharset) {
		this.metaCharset = metaCharset;
	}

	public String getMetaCharset() {
		return metaCharset;
	}


	public int getBitRate() {
		try {
			return Integer.parseInt(this.getHeaders().get("icy-br")) * 1024;
		} catch(Exception e) {
			return 0;
		}
		//return 0;
	}
	private String sample(int passWordLength, int[] buffer){
		String pass="";		
		for(int g=0;g<passWordLength;g++)
		{
			pass=pass.concat(String.valueOf((char)buffer[g]));
		}
		
		return pass.trim();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int get_mode() {
		return _mode;
	}

	public void set_mode(int _mode) {
		this._mode = _mode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
