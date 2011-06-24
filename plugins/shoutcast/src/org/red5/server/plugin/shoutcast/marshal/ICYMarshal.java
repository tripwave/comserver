package org.red5.server.plugin.shoutcast.marshal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.event.IEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.plugin.Shoutcast;
import org.red5.server.plugin.shoutcast.IICYEventSink;
import org.red5.server.plugin.shoutcast.IICYMarshal;
import org.red5.server.plugin.shoutcast.IStatusListener;
import org.red5.server.plugin.shoutcast.marshal.transpose.AudioFramer;
import org.red5.server.plugin.shoutcast.marshal.transpose.VideoFramer;
import org.red5.server.plugin.shoutcast.parser.IParse;
import org.red5.server.plugin.shoutcast.parser.NSVParser;
import org.red5.server.plugin.shoutcast.stream.ICYStream;
import org.red5.server.stream.BroadcastScope;
import org.red5.server.stream.IBroadcastScope;
import org.red5.server.stream.IProviderService;
import org.slf4j.Logger;

/**
 * This class registers the stream name in the provided scope and packages the buffers into rtmp events.
 *  @author Wittawas Nakkasem (vittee@hotmail.com)
 * 	@author Andy Shaules (bowljoman@hotmail.com)
 */
public class ICYMarshal implements IICYMarshal,IICYEventSink {

	private static Logger log = Red5LoggerFactory.getLogger(ICYMarshal.class, "ICYMarshal");
	
	private static enum TYPES{VOID,MPEG,MPA,NSV,FLV,OGG };

	private TYPES streamType=TYPES.VOID;

	private AudioFramer audioFramer;

	private VideoFramer videoFramer;

	private IScope _scope;

	private String _name;

	private ICYStream _stream;

	private String _content;

	private String _type;
	
	private IParse parser;
	
	private String _fourCCAudio;

	private String _fourCCVideo;

	private Map<String, Object> _metaData=new HashMap<String,Object>();;

	private List<IStatusListener> listeners=new ArrayList<IStatusListener>();

	private boolean _keepNal;
	private boolean _videoIsReady;
	private boolean _audioIsReady;


	public ICYMarshal(IScope outputScope, String outputName,boolean keepMp4) {
		_scope = outputScope;
		_keepNal=keepMp4;
		_name = outputName;
		_stream = new ICYStream(_name, true, true,this);
		_stream.setScope(outputScope);

		IContext context = outputScope.getContext();
		IProviderService providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
		if (providerService.registerBroadcastStream(outputScope, _stream.getPublishedName(), _stream)) {
			IBroadcastScope bsScope = (BroadcastScope) providerService.getLiveProviderInput(outputScope, _stream
					.getPublishedName(), true);
			bsScope.setAttribute(IBroadcastScope.STREAM_ATTRIBUTE, _stream);
		}
		
		audioFramer = new AudioFramer(this);
		_stream.audioFramer = audioFramer;
		videoFramer = new VideoFramer(this,_keepNal);
		_stream.videoFramer=videoFramer;
	}

	@Override
	public void onRawData(int[] data) 
	{

		switch(streamType)
		{
			case MPEG:
				audioFramer.onMP3Data(data, 0, 0);
				break;
				
			case MPA:
				audioFramer.onAACData(data, 0, 0);
				break;
				
			case NSV:
				if(parser != null)
					parser.parse(data);
				break;						
		}
	}	
	public void addStatusListener(IStatusListener listener){
		listeners.add(listener);
	}
	public boolean removeStatusListener(IStatusListener listener){
		return listeners.remove(listener);
	}
	private void processIStatusListeners(String status)
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onStatus(this._stream,status);
		}
	}
	public AudioFramer getAudioFramer() {
		return audioFramer;
	}

	public VideoFramer getVideoFramer() {
		return videoFramer;
	}

	public IScope getScope() {
		return _scope;
	}

	public ICYStream getStream() {
		return _stream;
	}

	public String getContentType() {
		return _type;
	}

	public String getAudioType() {
		if(_content.equals("audio"))
		{
			if(_type.startsWith("aac"))
			{
				return "AAC ";
			}else
			{
				return "MP3 ";
			}
		}
		return _fourCCAudio;
	}

	public String getVideoType() {
		return _fourCCVideo;
	}

	public void reset(String content, String type) {
		
		log.debug("reset {}  {}",content,type);
		_content = content;
		_type = type;

		if(_type.startsWith("aac"))
		{	
			streamType=TYPES.MPA;
			onConnected("NONE","AAC");
		}
		if(_type.equals("mpeg"))
		{	
			streamType=TYPES.MPEG;	
			onConnected("NONE","MP3");
		}
		if(_type.equals("x-flv"))
		{
			streamType=TYPES.FLV;
		}

		if(_type.trim().equals("nsv"))
		{
			parser=new NSVParser(this);
			streamType=TYPES.NSV;
		}
	}

	public void onAuxData(String fourCC, IoBuffer buffer) {
		log.debug("onAuxData {} {}",fourCC,buffer.toString());
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onAuxData(this._stream,fourCC, buffer);
		}
	}

	public void onConnected(String vidType, String audioType) {
		
		log.debug("onConnected {} {}",vidType,audioType);
		_fourCCAudio = audioType;
		_fourCCVideo = vidType;
		processIStatusListeners(IStatusListener.EVENT_CONNECTED);
	}

	public void onAudioData(int[] data, int timestamp,int offset) {

		if(_content == null )
			return;
		if (_content.equals("audio")) 
		{
			if (_type.startsWith("aac")) 
			{
				audioFramer.onAACData(data,timestamp,offset);
			}
			else
			{
				if (_type.startsWith("mpg"))
					audioFramer.onMP3Data(data, timestamp, offset);
			}
		}
		else
		{
			if(_fourCCAudio.startsWith("AAC"))
			{
				audioFramer.onAACData(data, timestamp, offset);
			}
			else if(_fourCCAudio.startsWith("MP3"))
			{
				audioFramer.onMP3Data(data, timestamp, offset);
			}
		}		
	}

	public void onDisconnected() 
	{
		
	
		_videoIsReady=false;
		_audioIsReady=false;
		
		audioFramer = new AudioFramer(this);
		_stream.audioFramer = audioFramer;
		videoFramer = new VideoFramer(this,_keepNal);
		_stream.videoFramer=videoFramer;
		_content=null;
		_type=null;
		_fourCCAudio=null;
		_fourCCVideo=null;

		processIStatusListeners(IStatusListener.EVENT_DISCONNECTED);
	}

	public void onMetaData(Map<String, Object> metaData) {
		
		_metaData.putAll(metaData);

		Notify event = getMetaDataEvent();
		
		if (event != null)
		{			
			_stream.setMetaDataEvent(event);
			_stream.dispatchEvent(event);	
		}
	}

	public Notify getMetaDataEvent() {
		if (_metaData == null) {
			return null;
		}
		IoBuffer buf = IoBuffer.allocate(1024);
		buf.setAutoExpand(true);
		Output out = new Output(buf);
		out.writeString("onMetaData");

		Map<Object, Object> props = new HashMap<Object, Object>();
		props.putAll(_metaData);
		props.put("transcoderBuild", Shoutcast.version);
		props.put("transcoder", "http://thebitstream.com");
		props.put("canSeekToEnd", false);
		out.writeMap(props, new Serializer());
		buf.flip();

		return new Notify(buf);
	}

	public void onVideoData(int[] buffer, int timestamp,int offset) {

		log.debug("onVideoData");
		if(_fourCCVideo == null)
		{	
			return;
		}
		else if (_fourCCVideo.startsWith("VP6"))
			videoFramer.pushVP6Frame(buffer, timestamp);
		else if (_fourCCVideo.startsWith("H264") || 
				_fourCCVideo.startsWith("x264") || 
				_fourCCVideo.startsWith("AVC") )
			videoFramer.pushAVCFrame(buffer, timestamp);
	}

	public void start(){
	}

	public void stop(){
		IContext context = _scope.getContext();
		IProviderService providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
		if (providerService.registerBroadcastStream(_scope, _stream.getPublishedName(), _stream)) {
			IBroadcastScope bsScope = (BroadcastScope) providerService.getLiveProviderInput(_scope, _stream
					.getPublishedName(), true);
			bsScope.setAttribute(IBroadcastScope.STREAM_ATTRIBUTE, _stream);
		}
		providerService.unregisterBroadcastStream(_scope, _stream.getPublishedName());	
	}

	public Map<String, Object> getMetadata() {	
		return _metaData;
	}
	
	
	public void dispatchEvent(IEvent event) {
		_videoIsReady = _audioIsReady=true;
		_stream.dispatchEvent(event);
	}
	
	public void reset() {
		this.videoFramer.reset();
		this.audioFramer.reset();
	}
	
	public boolean isConnected(){
		return _videoIsReady && _audioIsReady ;
	} 
}
