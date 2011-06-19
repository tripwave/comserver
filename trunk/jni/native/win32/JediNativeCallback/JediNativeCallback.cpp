// JediNativeCallback.cpp : Defines the initialization routines for the DLL.
//

#include "stdafx.h"
#include "JediNativeCallback.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif



BEGIN_MESSAGE_MAP(CJediNativeCallbackApp, CWinApp)
END_MESSAGE_MAP()


// CJediNativeCallbackApp construction

CJediNativeCallbackApp::CJediNativeCallbackApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CJediNativeCallbackApp object

CJediNativeCallbackApp theApp;


// CJediNativeCallbackApp initialization

BOOL CJediNativeCallbackApp::InitInstance()
{
	CWinApp::InitInstance();

	return TRUE;
}


JNIEXPORT jstring JNICALL Java_org_red5_server_plugin_jni_callback_JNICallback_call
		(JNIEnv * env, jobject obj, jstring service, jint params)

{
	JavaVM * vm;

	(*env).GetJavaVM(&vm);
	
	vm->AttachCurrentThread((void **)&env,0);
	
	BYTE buffer[1024];

	BYTE buffer2[1024];

	memset(buffer,0,1024);

	memset(buffer2,0,1024);

	sprintf((char *)buffer,env->GetStringUTFChars(service, false));
	
	printf("sending over %s\r\n",buffer);

	jint chan=(jint)params;

	CPipeClient *client=new CPipeClient( chan,(const char *)buffer);
	
	client->getData(buffer2);	

	jstring pval =(*env).NewStringUTF((const char *)buffer2);

	delete client;

	vm->DetachCurrentThread();

	return pval;

}








