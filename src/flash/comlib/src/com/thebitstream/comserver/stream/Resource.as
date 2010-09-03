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
	/**
	 * 
	 * @author Andy Shaules
	 * @version 1.0
	 */
	public class Resource
	{
		
		private var _serialized:String;
		private var _contextPath:String="";
		private var _resourceName:String;
		private var _root:String;
		/**
		 * Object representation of rtmp address, scope, and stream resource.
		 * <p>Typically a server will provide multiple resources at several scopes.
		 * The collection of resources is stored as the serialized String within a database, asset, or any other suitable storage medium.</p>
		 * <code>new Resource("rtmp://localhost/appName","room:subRoom:streamName");</code>
		 * <p>Targeting presence within a scope without requesting a stream or flv resource.</p>
		 * <code>new Resource("rtmp://localhost/appName","room:subRoom:");</code>
		 * @param root RTMP uri with application name.
		 * @param serialized IScope destination with or without a stream name.
		 * 
		 */				
		public function Resource(root:String,serialized:String)
		{
			_root=root;
			_serialized=serialized;
			
			if(! _serialized )
			{
				throw new Error("Null resource.");
			}
			var parts:Array=_serialized.split(":");
					
			if( parts[0].length == _serialized.length)
			{
				throw new Error("Not a serialized resource.");
			}
			for (var depth:int=0; depth< ( parts.length ) ;depth++ )
			{
				if( depth < ( parts.length-1 ) )
				{
					_contextPath=_contextPath+"/";
					_contextPath=_contextPath + parts[depth];
				}
				else
				{
					_resourceName=parts[depth];
				}
			}	
		}
		
		public function get isEventSink():Boolean
		{
			return (resourceName.length==0);
		}
		
		public function get contextPath():String
		{
			return _contextPath;
		}
		
		public function get resourceName():String
		{
			return _resourceName;
		}
		
		private static function createResource(target:Resource):void
		{	
			target._contextPath="";
			target._resourceName="";
		}

		public function get root():String
		{
			return _root;
		}

	}
}