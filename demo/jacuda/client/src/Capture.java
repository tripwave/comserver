

public class Capture {
	
	static{		
	    System.loadLibrary("JniCuda");
	   
	  
	}
	
	public IGForce framer=new VideoFramer();
	
	public native int initiateCapture(int deviceiD, int length);  
	
	public native int stopCapture();
	
	public void bufferCallback(double time, byte buffer[], int size){
	
		framer.pushAVCFrame(buffer,(int) (time*1000));

	}
	
	public static void main(String args[])
	{
		Capture capture= new Capture();
		
		capture.initiateCapture(0,10);

		capture.stopCapture();
	}
}
