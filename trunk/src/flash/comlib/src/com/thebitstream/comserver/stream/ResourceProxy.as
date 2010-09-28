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
package com.thebitstream.comserver.stream
{
	import flash.display.IBitmapDrawable;
	import flash.events.NetStatusEvent;
	import flash.net.NetConnection;
	import flash.net.NetStream;
	import flash.net.ObjectEncoding;
	import flash.net.Responder;
	import flash.utils.Proxy;
	import flash.utils.flash_proxy;
	import flash.utils.setInterval;
	/**
	 * 
	 * @author Andy Shaules
	 * @version 1.0 
	 */	
	public dynamic class ResourceProxy extends Proxy
	{
		private var _nc:NetConnection;
		private var _ns:NetStream;
		private var _client:IClient;
		private var _resource:Resource;
		private var _closed:Boolean=false;
		private var _currentMethod:QName;
		private var _clientId:*;
		private var _data:Object={x:0,y:0}
		private var _setter:Boolean=false;
		public static var BUFFER:Number=.5;

		/**
		 * Represents a presence on the Red5 server with an id and attributes made of name value pairs.
		 * <p>The Main purpose is to inject data and events into an flv stream that is being shared by all clients using the same Resource destination.</p> 
		 * @param resource The object representation of a Red5 server ip , scope, and stream name. 
		 * @param handler The object processing the shared flv script data events.
		 * @param clientId The id of this connecting client. The server can allow multiple connections from a single id.
		 * @param clientData The object containing name-value pairs that is associated with the client id.
		 * 
		 */		
		public function ResourceProxy(resource:Resource,handler:IClient,clientId:*,clientData:Object )
		{
			_clientId=clientId;
			_data=clientData;
			_resource=resource;
			_client=handler;
			super();
			
			NetConnection.defaultObjectEncoding=ObjectEncoding.AMF3;
			
			_nc=new NetConnection();
			_nc.addEventListener(NetStatusEvent.NET_STATUS,onStat);
			_nc.client=this;
			_nc.connect(_resource.root + _resource.contextPath,clientId,clientData);				
		}
		
		public function closeProxy():void
		{
			
			_closed=true;
			
			if( _nc != null && _nc.connected)
			{
				if(_ns){
					_ns.removeEventListener(NetStatusEvent.NET_STATUS,onStat);
					_ns.close();
				}
				_nc.removeEventListener(NetStatusEvent.NET_STATUS,onStat);
				_nc.close();

			}			
		}
		
		flash_proxy override function isAttribute(name:*):Boolean
		{
			
			return true;
		}
		
		flash_proxy override function hasProperty(name:*):Boolean
		{
			
			return true;
		}
		
		flash_proxy override function callProperty(name:*, ...rest):*
		{
			callExternal(name,rest);

		}
		
		flash_proxy override function getProperty(name:*):*
		{
			_currentMethod=name;
			return callInternal ;
		}
		
		private function callInternal(data:*):*
		{
			try{
			_client[_currentMethod](new ComEvent(_currentMethod,data));
			}
			catch (e :Error ){
				trace(e.name);
				trace(e.message);
				trace(e.getStackTrace());
			}
		}
		
		private function callExternal(name:QName, rest:Array):*
		{
		
			if( _nc != null && _nc.connected)
			{
			
				var method:String=name.localName;
				var obj:Object={};
				var i:int=0;
				for each(var param:Object in rest){
					obj[(i++ ).toString()]=param;
				}
				
				var rpc:RPCReturn=new RPCReturn(_client,name.localName,obj);
				
				if(!_setter)
				_nc.call( _resource.resourceName+ "."+ "sendEvent" , new Responder(rpc.onResult ),name.localName,obj  );
				else
				_nc.call( _resource.resourceName+ "."+ "setEvent" , new Responder(rpc.onResult ),name.localName,obj ,_data );
				
				_setter=false;
			}		
		}
		
		private function doClose():void
		{
			if(_nc)
			{
				_nc.removeEventListener(NetStatusEvent.NET_STATUS,onStat);
				_nc.close();
			}
		}
		
		private function onStat(nse:NetStatusEvent):void
		{
			
			if( nse.info.code === 'NetStream.Buffer.Full' )
			{
				_ns.bufferTime=30;
			}
			else if( nse.info.code === 'NetStream.Buffer.Empty' )
			{
				_ns.bufferTime=BUFFER;
			}
			if( nse.info.code === 'NetConnection.Connect.Success' )
			{
				//closed beore opened?
				if(_closed)
				{
					setInterval(doClose,250);
					return;	
				}
				if( _resource.isEventSink )
				{
					//Do nothing. Server invokes on netconnection.
					return;
				}
				else
				{
					_ns=new NetStream(_nc);
					_ns.client=this;
					_ns.bufferTime=BUFFER;
					_ns.addEventListener(NetStatusEvent.NET_STATUS,onStat);
					_ns.play(_resource.resourceName);					
				}
			}
			if(_client){
				_client.onNetStatus(nse.info.code);
			}
		}
		public function get data():Object
		{
			return _data;
		}
		
		public function set data(value:Object):void
		{
			if(_nc && _nc.connected && ! _setter)
			{	
				_nc.call( _resource.resourceName+ "."+ "setData" , new Responder(onSetDataResult ),_data );
			}
			else
			{	
				_data = value;	
			}
			
			function onSetDataResult(obj:Object):void
			{
				if(obj == 1)
				{
					_data = value;
				}
			}
		}

		public function get connetion():NetConnection
		{
			return _nc;
		}
		
		public function get stream():NetStream
		{
			return _ns;
		}		
		public function get clientId():*{
			return _clientId;
		}
		/**
		 * If true, shared data will be updated during next out going call. 
		 * @return 
		 * 
		 */
		public function get setter():Boolean
		{
			return _setter;
		}

		/**
		 * Set to true to update shared data values during next outgoing remote call. 
		 * @param value
		 * 
		 */
		public function set setter(value:Boolean):void
		{
			_setter = value;
		}
	}	
}