

#include "stdafx.h"

#include "jni.h"

#include "ICallbackCapable.h"


class RTMPClient : public ICallbackCapable
{
protected:

	JediNativeInterface * pJni;

	jobject client; 
	jmethodID initCapture;
	jmethodID setHost;
	jmethodID setApp;
	jmethodID setPort;
	jmethodID setMode;
	jmethodID start;
	jmethodID stopCapture;
	jmethodID pushAVCFrame;
	jmethodID setPublishName;
	jmethodID setId;
	jmethodID mid;
	jclass cls;

public:


	RTMPClient(JediNativeInterface * jni)
	{		
		client=0;

		this->pJni=jni;
		
	}

	int processCall(char * data,char * results)
	{
		int i= this->getId();
		sprintf(results,"Processed by %d",i);
		
		printf("The request is %s\r\n",data);

		return (lstrlen(results))*sizeof(CHAR);
	}

	void createClient()
	{
		
		jstring pval =(*pJni->getEnv()).NewStringUTF("org.red5.server.plugin.jni.objects.net.ChannelOut");
		
		jmethodID mid5 = pJni->getFactoryMethodId();
		jmethodID mid6 =pJni->getReferenceMethodId();

		// Avoid using 'FindClass' by creating registered handlers on the java side.
		// Request a class from the jni bootstrap interface.
		// Get the registered java class by qName.
		cls = (jclass)(*pJni->getEnv()).CallStaticObjectMethod( pJni->getInstance(), mid6,pval);
		// Request the class instance from the jni interface by qName.
		client = (*pJni->getEnv()).CallStaticObjectMethod( pJni->getInstance(), mid5,pval);
		
		initCapture = (*pJni->getEnv()).GetMethodID( cls, "initCapture","()V");
		setApp = (*pJni->getEnv()).GetMethodID( cls, "setApp","(Ljava/lang/String;)V");
		setHost = (*pJni->getEnv()).GetMethodID( cls, "setHost","(Ljava/lang/String;)V");
		setPort = (*pJni->getEnv()).GetMethodID( cls, "setPort","(I)V");
		setMode = (*pJni->getEnv()).GetMethodID( cls, "setMode","(I)V");
		stopCapture= (*pJni->getEnv()).GetMethodID( cls, "stopCapture","()V");
		setPublishName=(*pJni->getEnv()).GetMethodID( cls, "setPublishName","(Ljava/lang/String;)V");
		pushAVCFrame=(*pJni->getEnv()).GetMethodID( cls, "pushAVCFrame","([BI)V");		
		
		setId = (*pJni->getEnv()).GetMethodID( cls, "setId","(I)V");
		(*pJni->getEnv()).CallVoidMethod(client,setId,getId());
		mid = (*pJni->getEnv()).GetStaticMethodID( cls, "main","([Ljava/lang/String;)V");
	
	}
	void pushFrame(void * pBuffer, long size, long time )
	{
		jbyteArray jb;
		jdouble cTime=time;		
		jb=(*pJni->getEnv()).NewByteArray(size);
		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,pushAVCFrame,jb,cTime);
		(*pJni->getEnv()).DeleteLocalRef(jb);
	}

	void initiateCapture()
	{
		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,initCapture);
	}

	void setModeVal(int i)
	{
		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,setMode,i);
	}

	void setPublishedName(const char * app)
	{
		jstring pval =(*pJni->getEnv()).NewStringUTF(app);
		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,setPublishName,pval);
	}
	void setAppName(const char * app)
	{
		jstring pval =(*pJni->getEnv()).NewStringUTF(app);
		 
		
		
		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,setApp,pval);
	}

	void setHostName(const char * host)
	{
		jstring pval =(*pJni->getEnv()).NewStringUTF(host);

		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,setHost,pval);
	}

	void setPortNum(int port)
	{
		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,setPort,port);
	}

	void close()
	{
		if(client)
		(*pJni->getEnv()).CallVoidMethod(client,stopCapture);
		
		client=0;
	}

	~RTMPClient()
	{
		if(client) 
			close();
	}

};

