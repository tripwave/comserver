package comserver.demo.four
{
	import com.thebitstream.comserver.stream.IClient;
	import com.thebitstream.comserver.stream.Resource;
	import com.thebitstream.comserver.stream.ResourceProxy;
	
	import flash.events.NetStatusEvent;
	import flash.events.StatusEvent;
	import flash.media.Camera;
	import flash.media.Microphone;
	import flash.media.Video;
	import flash.net.NetStream;
	import flash.net.SharedObject;

	public class Data
	{
		private var _cam:Camera;
		private var _mic:Microphone;
		private var _nsOut:NetStream;
		private var _comServ:ResourceProxy;
		private var _resource:Resource;
		private var _user:String;
		
		private var _videoOut:Video;
		private var _videoIn:Video;
		
		private var _so:SharedObject;
		
		public function Data(user:String,host:String,room:String,group:String)
		{
			_user=user;
			var dest:String=room+":"+group;
			_resource=new Resource(host,dest);
		}
		
		public function init(client:IClient,data:Object):void{
			_comServ=new ResourceProxy(_resource,client,_user ,data);
			_videoOut=new Video();
			_videoIn=new Video();
		}
		
		public function initAVStream():void{
			_cam=Camera.getCamera();
			
			if(_cam.muted){
				trace('no cam, play only');
				return;
			}
			
			_cam.setMode(320,240,15);
			_cam.setQuality(0,70);
			_mic=Microphone.getMicrophone();
			
			_mic.setSilenceLevel(0);
			_mic.rate=22;
			_mic.gain=33;
			
			
			_nsOut=new NetStream(comServ.connetion);
			_nsOut.addEventListener(NetStatusEvent.NET_STATUS,onAvStat);
			_nsOut.attachAudio(_mic);
			_nsOut.attachCamera(_cam);
			
			_nsOut.publish(_user,"live");
			_videoOut.attachCamera(_cam);
	
			_videoIn.attachNetStream(comServ.stream);
		}
		
		public function closeAv():void{
			if(_nsOut){
				_nsOut.close();
			}
		}
		private function onAvStat(se : NetStatusEvent):void{
			trace("onAvStat :"+se.info.code);
		}
		public function get cam():Camera
		{
			return _cam;
		}

		public function get mic():Microphone
		{
			return _mic;
		}

		public function get nsOut():NetStream
		{
			return _nsOut;
		}

		public function get comServ():ResourceProxy
		{
			return _comServ;
		}

		public function get resource():Resource
		{
			return _resource;
		}

		public function set resource(value:Resource):void
		{
			_resource = value;
		}

		public function get videoOut():Video
		{
			return _videoOut;
		}

		public function set videoOut(value:Video):void
		{
			_videoOut = value;
		}

		public function get videoIn():Video
		{
			return _videoIn;
		}

		public function set videoIn(value:Video):void
		{
			_videoIn = value;
		}

		public function get so():SharedObject
		{
			return _so;
		}

		public function set so(value:SharedObject):void
		{
			_so = value;
		}


	}
}