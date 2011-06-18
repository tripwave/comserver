
package org.red5.server.plugin.jni.callback;

public class JNICallback {
	
	private static boolean isInitialized=false;
	
	private int id=0;
	
	private static synchronized void initialize(){		
		if(!isInitialized){
			String curDir = System.getProperty("user.dir");
			
			System.out.println("JNICallback initializing directory "+ curDir);
			
			
			isInitialized=true;
			System.load(curDir+"/JediNativeCallback.dll");
		}
	}
	
	public JNICallback(int channel){
		id=channel;
		initialize();
	}
	
	public native String call(String service, int params);

	public int getId() {
		return id;
	}

	public static void main(String args[])
	{
		JNICallback caller= new JNICallback(1);

		String ret = caller.call("foo", caller.getId());
		
		System.out.println("return was "+ ret);
			
	}
}