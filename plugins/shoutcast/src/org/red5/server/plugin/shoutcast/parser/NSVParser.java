package org.red5.server.plugin.shoutcast.parser;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.server.plugin.shoutcast.IICYHandler;

public class NSVParser implements IParse{
	public IICYHandler handler;

	public  NSVBitStream stream=new NSVBitStream();;
	public boolean gotFrame=false;

	public int limiter=0;
	
	private NSVStreamConfig config;

	private long frames;

	private int[] prev_bits=new int[0];

	public NSVParser(IICYHandler hand)
	{
		handler=hand;
	}

	public void parse(int[] dta)
	{
		limiter++;
				
		
		
		int data[]=new int[prev_bits.length+dta.length];
		
		
		
		for(int g=0 ; g < prev_bits.length ; g++ )
		{
			data[g]= prev_bits[g];
		}
		
		for(int m = 0 ; m<dta.length ;m++ )
		{
			data[ m +prev_bits.length ]=dta[m];
		}
		
		prev_bits=new int[0];
		
		int k=0;
		int h=0;
		for( h=0;h<data.length; h++)
		{
			if(h < data.length -1 )
			{
				if(gotFrame&& (data[h] & 0xff ) == 0xef )
				{
					if((data[h+1] & 0xff)== 0xbe )
					{
						k=chnkFrame(h,data);
						
						if(k == -1)
						{	
							save(data.length - h  , h, data );
							return;
						}
						else
						{
							h=k-1;
						}
					}
				}
			}
			else
			{
				save(2 , data.length - 2 , data );
				return;
			}
			
			if(h < data.length -3)
			{
				if(((data[h]) | ((data[h+1])<<8) | ((data[h+2])<<16) | ((data[h+3])<<24))== NSVStream.NSV_SYNC_DWORD)
				{
					k = syncFrame(h,data);
					
					if(k == -1)
					{
						save(data.length - h , h, data);
						return;
					}
					else
					{
						h=k-1;
					}
				}
			}
		}

	}

	private int syncFrame(int offset,int[]data)
	{
		
		int total_aux_used=0;
		int limit=data.length-offset;	
		
		if(limit<24)
			return -1;

		offset+=4;//NSVs;

		if(!gotFrame)
		{	
			String p_Vidtype=String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++]);		
			String p_Audtype=String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++]);

			int p_width				=	(data[offset++]) | (data[offset++] )<<8 ;
			int p_height			=	(data[offset++]) | (data[offset++] )<<8 ;
			int frame_rate_encoded 	=  	data[offset++];
			double p_framerate		=NSVStream.framerateToDouble(frame_rate_encoded);
			
			config=NSVStream.create(p_Vidtype, p_Audtype, p_width, p_height, p_framerate);	

			config.frame_rate_encoded=frame_rate_encoded;
			
			Map <String,Object> metaData=new HashMap<String,Object>();
			
			metaData.put("width", config.video_width );
			metaData.put("height", config.video_height );
			metaData.put("frameRate", config.frame_rate);
			metaData.put("videoCodec", config.video_format);
			metaData.put("audioCodec", config.audio_format);

			handler.onMetaData(metaData);
			
			handler.onConnected(p_Vidtype, p_Audtype);
			
			gotFrame=true;
		}
		else
		{
			offset+=4;//vid
			offset+=4;//aud
			offset+=2;//width ;
			offset+=2;//height ;
			offset+=1;//framerate ;
		}

		int aOffset=data[offset++]|data[offset++]<<8;


		NSVBitStream bs0=new NSVBitStream();
		bs0.putBits(8, data[offset++]);
		bs0.putBits(8,  data[offset++]);
		bs0.putBits(8,  data[offset++]);
		int num_aux=bs0.getbits(4);
		int vid_len=bs0.getbits(20);

		bs0.putBits(8,  data[offset++]);
		bs0.putBits(8,  data[offset++]);
		int aud_len=bs0.getbits(16);

		int bytesNeeded=(24)+(vid_len)+(aud_len);
		if (limit < bytesNeeded)
		{
			return -1;
		}
		
		NSVFrame frame=new NSVFrame(config,NSVStream.NSV_SYNC_DWORD);//;NSVStream.stream(config,);

		if(frames == Long.MAX_VALUE)
		{
			frames=0;
		}
		frame.offset_current=aOffset;
		frame.frame_number=frames++;
		frame.vid_len=vid_len;
		frame.aud_len=aud_len;
		frame.vid_data=new int[vid_len];
		frame.aud_data=new int [aud_len];

		if(num_aux>0)
		{

			for(int a=0;a<num_aux;a++)		
			{
				int aux_len= data[offset++]  | data[offset++]<<8 ;
				
				String aux_type=String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++]);
				
				total_aux_used+= (aux_len+6);
				
				IoBuffer buffer=IoBuffer.allocate(aux_len);	

				for(int b=0;b<aux_len;b++)
				{
					buffer.put( (byte) data[ offset++]);
				}
				buffer.flip();
				buffer.position(0);
				handler.onAuxData(aux_type, buffer);
				
				if(aux_type.equals("ASYN"))
				{
					
					return offset;
				}				
			}		
		}	

		for(int vids=0;vids<vid_len-total_aux_used;vids++)
		{
			frame.vid_data[vids]=data[offset++];
		}	
		for(int auds=0;auds<aud_len;auds++)
		{
			frame.aud_data[auds]=data[offset++];
		}
		
		frame.timeStamp=(long)  ( frame.frame_number *   (1.0 / config.frame_rate)  * 1000 );
		handler.onVideoData(frame.vid_data,  (int) frame.timeStamp , 0);
		handler.onAudioData(frame.aud_data, (int) frame.timeStamp, frame.offset_current);
		return offset;
	}
	
	private int chnkFrame(int poffset,int[]data)
	{
		int offset=poffset;
		int total_aux_used=0;
		int limit=data.length-poffset;
		if(limit<7)
			return -1;
		
		offset++;
		offset++;

		NSVBitStream bs0=new NSVBitStream();
		bs0.putBits(8, data[offset++]);
		bs0.putBits(8,  data[offset++]);
		bs0.putBits(8,  data[offset++]);
		int num_aux=bs0.getbits(4);
		int vid_len=bs0.getbits(20);
		bs0.putBits(8,  data[offset++]);
		bs0.putBits(8,  data[offset++]);
		int aud_len=bs0.getbits(16);
		bs0.destroy();

		int bytesNeeded=(7)+(vid_len)+(aud_len);
		if (limit < bytesNeeded)
		{			
			return -1;
		}
		
		NSVFrame frame=new NSVFrame(config,NSVStream.NSV_NONSYNC_WORD);
		if(frames == Long.MAX_VALUE)
		{
			frames=0;
		}
		
		frame.frame_number=frames++;		
		frame.vid_len=vid_len;
		frame.aud_len=aud_len;
		frame.vid_data=new int[vid_len];
		frame.aud_data=new int [aud_len];
		
		if(num_aux>0)
		{
			for(int a=0;a<num_aux;a++)
			{
				int aux_len=( data[offset++]&0xff)   | (data[offset++]&0xff)  <<8;
				String aux_type=String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++])+String.valueOf((char) data[offset++]);
				
				total_aux_used+= (aux_len+6);
				
				IoBuffer buffer=IoBuffer.allocate(aux_len);	
				
				for(int b=0;b<aux_len;b++)
				{  
					buffer.put((byte)data[offset++]);
				}
				buffer.flip();
				buffer.position(0);
				handler.onAuxData(aux_type, buffer);
				if(aux_type.equals("ASYN"))
				{					
					return offset;
				}				
			}		
		}		
		for(int vids=0;vids<vid_len-total_aux_used;vids++)
		{
			frame.vid_data[vids]=data[offset++]& 0xff;;
		}		
		for(int auds=0;auds<aud_len;auds++)
		{
			frame.aud_data[auds]=data[offset++]& 0xff;;
		}
		frame.timeStamp=(long)  ( frame.frame_number *   (1.0 / config.frame_rate)  * 1000 );
		
		handler.onVideoData(frame.vid_data,  (int) frame.timeStamp , 0);
		handler.onAudioData(frame.aud_data, 0, 0);
	
		return offset;
	}

	private void save(int num,int offSet,int[] pbits)
	{
		prev_bits=new int[num];

		for(int i=0;i<num;i++)
		{
			prev_bits[i]= pbits[i+offSet] & 0xff;
		}		
	}	

}
