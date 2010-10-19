package comdemo.internal.feeds;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.mina.core.buffer.IoBuffer;

public class Data extends OutputStream{
	public IoBuffer data;
	
	public Data(){
		data=IoBuffer.allocate(320 * 240 * 3);
		data.setAutoExpand(true);
	}
	public Data(IoBuffer toWrap){
		data=toWrap;
	}	
	@Override
	public void write(int b) throws IOException {
		data.put((byte) b);	
	}
	public void dispose(){
		data.free();
	}
	
}
