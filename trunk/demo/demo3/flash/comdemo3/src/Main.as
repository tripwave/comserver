import com.thebitstream.comserver.stream.ComEvent;
import com.thebitstream.comserver.stream.IClient;

private var client:ComClient;


private function boot():void{
	
	client=new ComClient(this,'client1');
	client.openResource("rtmp://localhost/comdemo",'soDemo:one');
}

public function runTime(obj:Object):void{	
	trace(obj.message);
}

public function otherRunTime( e :ComEvent):void{
	trace(e.data[0].message);
}

public function proxyReady():void{
	
	client['runTime']= runTime; 
	client['otherRunTime']= otherRunTime; 
	
	client.mySO.send('runTime',{message:'run time ready'});
	client.proxy.otherRunTime({message:'other Run Time ready'})


}

