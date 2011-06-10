package
{
	import flash.display.Sprite;
	import flash.events.NetStatusEvent;
	import flash.media.Video;
	import flash.net.NetConnection;
	import flash.net.NetStream;
	
	public class Jacuda extends Sprite
	{
		var nc:NetConnection;
		var ns:NetStream;
		
		public function Jacuda()
		{
			nc=new NetConnection();
			nc.addEventListener(NetStatusEvent.NET_STATUS,onStat);
			nc.connect("rtmp://localhost/cuda");
		}
		public function onMetaData(obj:Object):void
		{
			
		}
		private function onStat(nse:NetStatusEvent):void
		{
			ns=new NetStream(nc);
			ns.client=this;
			ns.bufferTime=4;
			ns.play("cuda");
			
			var vid:Video=new Video();
			vid.attachNetStream(ns);
			addChild(vid);
		}
	}
}