

#include "stdafx.h"

#include "jni.h"

#include "ICallbackCapable.h"

	

#include "Shellapi.h"

class Red5Server : public ICallbackCapable
{
public:
	JediNativeInterface * pJni;
	jmethodID mid ;
	jobject client; 

	jclass cls;

	Red5Server(JediNativeInterface * jni)
	{		
		client=0;

		this->pJni=jni;
		
	}
	void boot()
	{
		cls = (*pJni->getEnv()).FindClass( "org/red5/server/Bootstrap");
		
		mid = (*pJni->getEnv()).GetStaticMethodID( cls, "main","([Ljava/lang/String;)V");
		
		(*pJni->getEnv()).CallStaticVoidMethod( cls, mid,0);	


	}
	void shutdown()
	{
		::ShellExecute(0,0,"red5-shutdown.bat",0,RED5_HOME,0);
		
	}

	int processCall(char * data,char * results)
	{
		int i= this->getId();
		sprintf(results,"Processed by %d",i);
		
		printf("The request is %s\r\n",data);

		return (lstrlen(results))*sizeof(CHAR);
	}

};