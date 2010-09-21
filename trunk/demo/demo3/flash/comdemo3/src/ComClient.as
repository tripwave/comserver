package
{
	import com.thebitstream.comserver.stream.*;
	
	import flash.events.SyncEvent;
	import flash.net.SharedObject;
	
	public dynamic class ComClient implements IClient
	{
		private var _owner:Object;
		private var _resource:Resource;
		private var _proxy:ResourceProxy;
		private var _id:String;
		private var _data:SharedObject;
		
		
		public function ComClient(owner:Object, clientId:String)
		{
			_id=clientId;
			_owner=owner;
			
		}
		
		public function get proxy():ResourceProxy{
			return _proxy;
		}
		
		public function openResource(server:String,dest:String):void{
			_resource=new Resource(server,dest);
			
			if(_proxy == null ){
				ResourceProxy.BUFFER=.2;
				_proxy=new ResourceProxy(_resource,this,_id ,{});
			}
		}
		
		public function closeResource():void{
			
		}
		
		public function openSO(name:String):void{
			
			_data=SharedObject.getRemote(name,_proxy.connetion.uri,true);
			_data.addEventListener(SyncEvent.SYNC, onSync);
			_data.fps=10;
			_data.client=this;
			_data.connect(_proxy.connetion);			
		
		}
		
		public function closeSO(name:String):void{
			
		}
		public function get mySO():SharedObject{
			return _data;
		}
		private function onSync( e :SyncEvent ):void{
			trace( _data.data.name);
			
			if(e.target === _data ){
				if( !_data.data.hasOwnProperty('name')){
					_data.setProperty('name',_id);
					_data.setProperty('x',500);
					_data.setProperty('y',500);
				
				}else{
					
					trace(_data.data.name);
					trace(_data.data.x);
					trace(_data.data.y);
					
					_owner.proxyReady();
					
				}
			}
		}
		
		public function onTick(obj:Object):void{
			
		}
		
		public function onComResult(obj:RPCReturn):void
		{
		}
		
		public function onMetaData(obj:ComEvent):void
		{
		}
		
		public function onNetStatus(code:String):void
		{
			trace(code);
			if(code=="NetStream.Play.Start"){
				openSO(_id);
			}
		}
	}
}