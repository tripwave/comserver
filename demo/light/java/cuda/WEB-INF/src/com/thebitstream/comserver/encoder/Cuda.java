package com.thebitstream.comserver.encoder;


import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.net.rtmp.message.Constants;

import com.thebitstream.comserver.app.light.ApplicationLight;
//com.thebitstream.comserver.encoder.Cuda
public class Cuda extends ApplicationLight {

	

	

	
	public void streamPublishStart(IBroadcastStream stream)
	{
		stream.addStreamListener(new StreamHandler());
	}
	
	private class StreamHandler implements IStreamListener{

		
		public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
			System.out.println(packet.getTimestamp());
			
			if(Constants.TYPE_AUDIO_DATA == packet.getDataType()){
				
				
			}else if(Constants.TYPE_VIDEO_DATA == packet.getDataType()){
				
				System.out.println(stream.getCodecInfo().getVideoCodec().getName());
				
			}
			
		}	
	}
	
}
