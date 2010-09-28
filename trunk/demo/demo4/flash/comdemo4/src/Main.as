import com.thebitstream.comserver.stream.*;

import comserver.demo.four.Data;

import flash.events.MouseEvent;
import flash.events.SyncEvent;
import flash.media.Video;
import flash.system.Security;
import flash.system.SecurityPanel;

import mx.collections.ArrayCollection;

public static const CLOSE:String="Close";
public static const CONNECT:String="Connect";

public var appData:Data;



public function boot():void{
	flash.system.Security.showSettings(SecurityPanel.DEFAULT);
	idForm.idConnect.addEventListener(MouseEvent.CLICK, click);
}



public function changeChannel(obj:ComEvent):void{
	trace(obj.data.channel);
	idChan.text="Now playing "+obj.data.channel;
	if(obj.data.channel == appData.comServ.clientId){
		appData.so.setProperty(appData.comServ.clientId,{stat:false,user:idForm.document.user});
	}
}
public function blankChannel(obj:ComEvent):void{
	
	idChan.text="No stream at this time.. ";
	appData.videoIn.clear();
}

public function click(e : MouseEvent):void{
	
if(	idForm.idConnect.label == CLOSE){
	appData.so.close();
	appData.closeAv();
	appData.comServ.closeProxy();
	idForm.idConnect.label=CONNECT;
	return 

}
	
	var data:Object=idForm.document;
	appData=new Data(data.user,data.host,data.room,data.group);
	appData.init(this,data);
	idForm.idConnect.label=CLOSE;
}

private function setRandom():void{
	if(appData && appData.so){
		appData.so.setProperty("channel","random");
		appData.so.setDirty("channel");
	}
}

private function setMe():void{
	if(appData && appData.so){
		appData.so.setProperty("channel",appData.comServ.clientId);
		appData.so.setDirty("channel");

		
	}	
}

public function onNetStatus(code:String):void{
	if(code=="NetConnection.Connect.Success"){
		appData.initAVStream();
	}
	if(code=="NetStream.Play.Start"){
		appData.videoIn=new Video(320,240);
		appData.videoIn.attachNetStream(appData.comServ.stream);
		idContainerIn.uiContainer.addChild(appData.videoIn);
		idContainerOut.uiContainer.addChild(appData.videoOut);
		
		appData.so=SharedObject.getRemote( idForm.document.group ,appData.comServ.connetion.uri);
		appData.so.client=this;
		appData.so.addEventListener(SyncEvent.SYNC, onSync);
		appData.so.connect(appData.comServ.connetion);
	}
}




public function onComResult(obj:RPCReturn):void{
	
}

public function onMetaData(obj:ComEvent):void{
	
}

public function onSync(se:SyncEvent):void{
	trace("onSync "+appData.so.data.channel);
	
	if(! appData.so.data.hasOwnProperty(appData.comServ.clientId)){
			
		appData.so.setProperty(appData.comServ.clientId,{user:idForm.document.user});
		
		return;
	}

}
