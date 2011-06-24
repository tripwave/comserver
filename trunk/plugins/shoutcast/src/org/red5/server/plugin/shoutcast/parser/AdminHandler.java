package org.red5.server.plugin.shoutcast.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class AdminHandler implements Runnable
{

	public boolean doRun;
	public IcyConsumer handler;
	public int port;
	public String passWord;
	private ServerSocket outSock;
	
	private class Responder extends Thread
	{
		InputStream in ;
		public Responder(InputStream in )
		{
			
			this.in=in;
		}

		@Override
		public void run() 
		{
			
			
			String request="";
			BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
			try 
			{
				
				
				while(true)
				{
					request=inReader.readLine();
					
					if(request == null)
					{
						
						in.close();
						break;
					}

					if(request.length()==0)
					{
					
						in.close();
						break;	
					}
					
					String[] tokens= request.split(" ");
					
					if(tokens.length<2)
					{	

						in.close();
						return;
					}
					
					int i= tokens[1].indexOf("?");
					String payload= tokens[1].substring(i+1);

					
					if(payload.indexOf(passWord)<0)
					{					
						in.close();
						return;
					}

					String[] chunks= payload.split("&");
					
					for(int t=0;t< chunks.length;t++)
					{
						String[] vals=chunks[t].split("=");
						if(vals[0].equals("song")&& vals.length>1)
						{
							handler.getHeaders().put("StreamTitle",vals[1]);
						}
						if(vals[0].equals("url") && vals.length>1)
						{
							handler.getHeaders().put("StreamUrl",vals[1]);
						}				
					}
					
					in.close();
					handler.sendMeta();
					break;
				}
			} 
			catch (IOException e) 
			{

			}
			
		}
	}
	
	public AdminHandler(IcyConsumer handler, String pass, int port)
	{
		this.handler=handler;
		passWord=pass;
		this.port=port;
	}
	
	@Override
	public void run() 
	{
		doRun=true;
		try
		{		
			outSock= new ServerSocket(port);
			
			while(doRun)
			{
				
				Socket client = outSock.accept();
				
				if(!doRun)
					return;

				InputStream in = client.getInputStream();		
				Responder resp=new Responder(in);
				resp.start();
				
			} 

		}
		catch (IOException e)
		{
			
			
		}			
		
	}
	
	public void stop()
	{
		try {
			doRun=false;
			outSock.close();
		} catch (IOException e) 
		{		
		}
		
		
	}
	
}
