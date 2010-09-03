/*
* COMSERVER Open Source Application Framework - http://www.thebitstream.com
*
* Copyright (c) 2009-2010 by Andy Shaules. All rights reserved.
*
* This library is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* This library is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with this library; if not, write to the Free Software Foundation, Inc.,
* 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package
{
	import com.thebitstream.comserver.stream.*;
	
	import flash.display.Sprite;
	import flash.media.Video;
	/**
	 * @version 1.0 
	 * */
	public class comdemo extends Sprite implements IClient
	{
		public var id:String="id"+int(Math.random()*100000);
		public var data:Object={};
		public var comServer:ResourceProxy;
		public var vid:Video=new Video(360,240);
		
		public function comdemo()
		{
			var resource:Resource=new Resource("rtmp://localhost/comdemo","room:room:stream");
			
			comServer= new ResourceProxy(resource,this,id,data);	
		}
		
		public function onNetStatus(code:String):void{
			trace(code);
			if(code == "NetStream.Play.Start"){
				
				vid.attachNetStream(comServer.stream);
				addChild(vid);
			}
		}
		
		public function onMetaData(obj:ComEvent):void{

		}
		
		public function onComResult(event:RPCReturn):void{
		
		}
	}	
}