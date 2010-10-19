/**
 * 
 */
package edu.mit.star.flv.impl;

import java.io.IOException;
import java.util.zip.Deflater;

import org.apache.mina.core.buffer.IoBuffer;

class ScreenVideoDataImageblock implements DataWriter
{
	public int dataSize; // 16bit
	public byte[] data; // zlib compressed BGR
	
	public ScreenVideoDataImageblock(byte[] uncompressed)
    {
		Deflater compressor = new Deflater();
		compressor.setInput(uncompressed);
		compressor.finish();
		byte[] output = new byte[ uncompressed.length ] ;
		dataSize = compressor.deflate(output);
		data = new byte[ dataSize ] ;
		System.arraycopy(output, 0, data, 0, dataSize ) ;
    }





	@Override
	public void write(IoBuffer out, boolean isKey) throws IOException {
		out.putShort((short) dataSize);
		
				
		out.put(data);	
		
	}
}