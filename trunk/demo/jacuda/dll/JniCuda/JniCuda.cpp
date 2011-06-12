// JniCuda.cpp : Defines the initialization routines for the DLL.
//
//(D[BJ)V
#include "stdafx.h"
#include "CAutoCap.h"
#include "JniCuda.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


BEGIN_MESSAGE_MAP(CJniCudaApp, CWinApp)
END_MESSAGE_MAP()


// CJniCudaApp construction

CJniCudaApp::CJniCudaApp()
{

}

JavaVM * javavm;

JNIEnv * environment;

jmethodID methId;

jobject handler;

CAutoCap * capper;

CJniCudaApp theApp;


// CJniCudaApp initialization
void CJniCudaApp::sampleEvent( double SampleTime, BYTE * pBuffer, long BufferLen)
{

}

BOOL CJniCudaApp::InitInstance()
{
	CWinApp::InitInstance();
	javavm=0;
	capper=0;
	environment=0;
	handler=0;
	

	CoInitialize(NULL);
	
	return TRUE;
}


	JNIEXPORT jint JNICALL Java_Capture_initiateCapture(JNIEnv * env, jobject jobj, jint i, jint capLen){
		
		if(capper){
			return 1;
		}

			
			capper= new CAutoCap();
			capper->CaptureVideo(i);

		
			handler=jobj;
			//environment=env;
			env->GetJavaVM(&javavm);	
			javavm->AttachCurrentThread((void **)&environment,0);
			jclass cls = environment->GetObjectClass( jobj);
			
			methId=environment->GetMethodID(cls,"bufferCallback","(D[BI)V");
			
			
			long now=0;
			while (now<capLen){
				
				do{
					Sleep(1);				
				
				}while(!capper->chanGrabber->hasSample);


				jdouble cTime=capper->chanGrabber->currentTime;
				now=capper->chanGrabber->currentTime;
				jlong t=capper->chanGrabber->len;

				jbyteArray jb;

				jb=environment->NewByteArray(t);
				environment->SetByteArrayRegion( jb, 0, 
					t, (jbyte *)capper->chanGrabber->buffer);


				environment->CallVoidMethod(handler,methId,cTime ,jb ,t);
				

				capper->chanGrabber->hasSample=false;
				capper->chanGrabber->clearedSample=true;

				environment->DeleteLocalRef(jb);
			}

	return 0;
}

JNIEXPORT jint JNICALL Java_Capture_stopCapture(JNIEnv  * env, jobject jobj){
	
	if(capper){
		capper->CloseInterfaces();
	}

	//javavm->DetachCurrentThread();
	
	return 0;
}



