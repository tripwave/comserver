/**
 * 
 */
package edu.mit.star.flv.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

class ScreenVideoData implements DataWriter
{
	int BlockWidth = 16; // 16 - 256
	int ImageWidth; // 12bit
	int BlockHeight = 16; // 16 - 256
	int ImageHeight; // 12 bit
	int compression = 0;
	int blocksX;
	int blocksY;
	ScreenVideoDataImageblock[] imageBlocks;

	public ScreenVideoData(BufferedImage image, int compression)
	{
		ImageWidth = image.getWidth();
		ImageHeight = image.getHeight();
		generateImageBlocks(image);
	}
	


	private void generateImageBlocks(BufferedImage image)
	{
		blocksX = (int) Math.ceil(1.0d * ImageWidth / BlockWidth);
		blocksY = (int) Math.ceil(1.0d * ImageHeight / BlockHeight);
		imageBlocks = new ScreenVideoDataImageblock[blocksX * blocksY];
		for (int i = 0; i < imageBlocks.length; i++)
		{
			imageBlocks[i] = addBlock(i,image);
		}
	}

	ScreenVideoDataImageblock addBlock(int block, BufferedImage image)
	{

		int x = block % blocksX;
		int y = block / blocksX;

		int x0 = x * BlockWidth;
		int y0 = ImageHeight - y * BlockHeight - BlockHeight;

		byte[] data = new byte[ BlockHeight * BlockWidth * 3 ] ;
		int pos = 0 ;
		for (int offsetY = 0; offsetY < BlockHeight; offsetY++)
		{
			for (int offsetX = 0; offsetX < BlockWidth; offsetX++)
			{
			
				int rgb = image.getRGB(offsetX+x0, (BlockHeight-1-offsetY)+y0);
				data[pos++]=(byte)(rgb>>0&0xff);
				data[pos++]=(byte)(rgb>>8&0xff);
				data[pos++]=(byte)(rgb>>16&0xff);
			}
		}		
		return new ScreenVideoDataImageblock(data);
	}

	@Override
	public void write(IoBuffer out, boolean isKey) throws IOException {
		out.putShort ( (short) (((BlockWidth/16)-1)<<12 | ImageWidth) );
		out.putShort( (short) (((BlockHeight/16)-1)<<12 | ImageHeight) );
	
		for( ScreenVideoDataImageblock block : imageBlocks )
		{
			
			block.write( out,true ) ;
			
		}		
		
	}


	
	

}