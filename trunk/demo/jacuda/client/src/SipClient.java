

import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.net.rtmp.RTMPClient;



public class SipClient implements Runnable,ICallbackClient,IPendingServiceCallback{

	
	private RTMPClient client;

	private String address;
	private int startDelay;

	public SipClient(String add)
	{

		address=add;
	}

	@Override
	public void run() {
		
		try {
			Thread.sleep(startDelay);
		} catch (InterruptedException e) {
		}
		
		connect();
		
	}
	
	private void connect()
	{
		client=new RTMPClient();
				
		client.connect(address, 1935,"cuda" ,  this);
	
	}
	
	@Override
	public void resultReceived(IPendingServiceCall arg0) {
		System.out.println("connectCallback resultReceived ");

		

}	
	
	
	public void close(){
		client.disconnect();
	}
	@Override
	public void onResults(IPendingServiceCall arg0) {
		System.out.println("SIP result  "+ arg0.getServiceMethodName()+"  "+ arg0.getResult().toString());
		
		
		
	}	
	
	
	public class CallbackHandler implements IPendingServiceCallback,Runnable{

		private ICallbackClient owner;
		private RTMPClient rtmpClient;
		private Object[] params;
		private String serviceCall;
		
		public CallbackHandler(RTMPClient clt,String call,Object[] prms){
			serviceCall=call;
			params=prms;
			rtmpClient=clt;
		}

		@Override
		public void run() {			
			rtmpClient.invoke(serviceCall, params , this);
		}

		@Override
		public void resultReceived(IPendingServiceCall arg0) {
			
			System.out.println("CallbackHandler.resultReceived  ");
			System.out.println( arg0.getResult().toString());
			
			if(owner!= null)
				owner.onResults(arg0);
			else{
				System.out.println("Null ?");
			}
		}

		public void setClient(ICallbackClient owner) {
			this.owner = owner;
		}
	}
	

	public void setStartDelay(int i) {
		startDelay=i;
		
	}






	
	
}
