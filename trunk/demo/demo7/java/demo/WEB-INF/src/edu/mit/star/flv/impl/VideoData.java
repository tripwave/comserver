/**
 * 
 */
package edu.mit.star.flv.impl;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;


public class VideoData implements DataWriter
{
	final static byte KEYFRAME = 1 * 16;
	final static byte INTERFRAME = 2 * 16;
	final static byte DISPOSABLEINTERFRAME = 3 * 16;
	final static byte GENERATEDKEYFRAME = 4 * 16;
	final static byte INFOCOMMAND = 5 * 16;

	final static byte JPEG_CODEC = 1;
	final static byte H263_CODEC = 2;
	final static byte SCREENVIDEO_CODEC = 3;
	final static byte On2VP6_CODEC = 4;
	final static byte On2VP6_Alpha_CODEC = 5;
	final static byte SCREENVIDEO2_CODEC = 6;
	final static byte AVC = 7;

	byte FrameAndCodec;
	DataWriter videoData ;

	
	public VideoData(Image image)
	{
		
		FrameAndCodec = KEYFRAME | SCREENVIDEO_CODEC;
		videoData = new ScreenVideoData((BufferedImage) image, 5);
	}



	@Override
	public void write(IoBuffer out,boolean isKey) throws IOException {
		out.put(FrameAndCodec);
		videoData.write(out, isKey);
		out.flip();
		
	}
	
}