
#include "stdafx.h"

#include "jni.h"

#include "NativeCallbackHandler.h"

#define RED5_HOME "C:\\workspaces\\eclipse\\ComServer\\red5"

typedef jint (WINAPI * MYPROC)(void ** arg1, void ** arg2,void * arg3 );


class JediNativeInterface{
	
	
	int iTest;
	JNIEnv *env;
	JNIEnv *ref;
    JavaVM *jvm;
    jint res;
    jclass cls;

    jmethodID mid;
	jmethodID mid2;
	jmethodID mid3;
	jmethodID mid4;
	jmethodID mid5;
	jmethodID mid6;
	jmethodID passOwner;
	JavaVMInitArgs vm_args;
	JavaVMOption options[6];

	NativeCallbackHandler * callback;


public:

	JediNativeInterface()
	{
		printf("\r\n\r\n\t\t **  JediNativeInterface  **\r\n\r\nUsing:\r\n\r\n");
		
		callback=0;
		env=0;
		jvm=0;
		iTest=1;
		
		callback=new NativeCallbackHandler();
	}

	jclass getInstance()
	{
		return cls;
	}

	JNIEnv * getEnv()
	{
		return env;
	}

	void addCallbackHandler(ICallbackCapable * handler)
	{
		callback->addCallbackHandler( handler);
	}

	void createJNI()
	{

		
		
		options[0].optionString = "-Djava.compiler=NONE";      
		options[1].optionString = new char[MAX_PATH];
		sprintf(options[1].optionString , "-Djava.class.path=%s\\conf;%s\\plugins\\JediNativeInterface.jar",RED5_HOME,RED5_HOME,RED5_HOME);
		options[2].optionString = new char[MAX_PATH];
		sprintf(options[2].optionString , "-Djava.library.path=%s\\lib\\",RED5_HOME);  
		options[3].optionString = new char[MAX_PATH];
		sprintf(options[3].optionString,	"-Dlogback.ContextSelector=org.red5.logging.LoggingContextSelector -Dcatalina.useNaming=true");
		
		vm_args.version = JNI_VERSION_1_2;
		vm_args.options = options;
		vm_args.nOptions = 4;
		vm_args.ignoreUnrecognized = TRUE;

		char * pPath;
		
		::SetEnvironmentVariable("RED5_HOME",RED5_HOME);
		
		pPath = getenv ("JAVA_HOME");
		
		char javahome[MAX_PATH];

		memset(javahome,0,MAX_PATH);
	 	
		const char *orig = "\\jre\\bin\\client\\jvm.dll";
		sprintf(javahome,"%s",pPath);

		printf("JAVA_HOME : %s\r\n\r\n",pPath);
		
		sprintf(javahome,"%s%s",pPath,orig);
		
		HINSTANCE hJVM = LoadLibrary((LPCSTR) javahome );
		
		if (hJVM == NULL)
		{
			fprintf( stderr,"\n\n*****Failed Loading jvm.dll library******\n\n");
			return;
		} 

		FARPROC fp = 0;
		
		fp =  GetProcAddress(hJVM, "JNI_CreateJavaVM");
		
		if (fp == 0)
		{ 
			fprintf( stderr,"\n\n*****Failed GetProcAddress******\n\n");
			return ;
		}

		res = 	((MYPROC)fp) ((void**)&jvm, (void**)&env, &vm_args);
		
		if (res < 0)
		{      
			fprintf(stderr, "MYPROC?\n");
			return ;
		}

		fprintf(stderr, "Created Java VM\r\n\r\n");

		 cls = (*env).FindClass( "org/red5/server/plugin/jni/BootstrapJni");
		
		if (cls == NULL)
		{
			destroyJNI();
			return;
		}
		
		int ares=(*jvm).AttachCurrentThread((void ** )&ref,0);
		
		if (ares<0) 
		{
			fprintf(stderr, "Could not attach the thread\n");
			return;
		}

		fprintf(stderr, "Found Jedi Native Interface, creating jni arguments.\r\n\r\n");
		
		jobjectArray gts;	
		
		jstring pval =(*env).NewStringUTF(RED5_HOME);
		
		gts=(*env).NewObjectArray(1,env->FindClass("java/lang/String"), env->NewStringUTF(""));
		
		env->SetObjectArrayElement(gts,0,pval);

		mid = (*env).GetStaticMethodID( cls, "main","([Ljava/lang/String;)V");
		mid2 = (*env).GetStaticMethodID( cls, "initiate","(I)V");
		mid3 = (*env).GetStaticMethodID( cls, "stop","(I)V");
		mid4 = (*env).GetStaticMethodID( cls, "config","(ILjava/lang/String;Ljava/lang/String;)V");
		mid5 = (*env).GetStaticMethodID( cls, "createObject","(Ljava/lang/String;)Ljava/lang/Object;");
		mid6 = (*env).GetStaticMethodID( cls, "getReference","(Ljava/lang/String;)Ljava/lang/Object;");
		
		fprintf(stdout, "Calling Bootstrap..\r\n");

		(*env).CallStaticVoidMethod( cls, mid,gts);

		jclass cls2 = (*env).FindClass( "org/red5/server/Bootstrap");

		fprintf(stderr, "Starting Jedi Telepathy Handler.\r\n");

		callback->start();	
					
		(*env).CallStaticVoidMethod( cls, mid2,0);		
	}

	jmethodID getReferenceMethodId()
	{
		return mid6;
	}

	jmethodID getFactoryMethodId()
	{
		return mid5;
	}

	void destroyJNI()
	{
		if(env==0)
			return;

		if(callback)
		{
			callback->stop();
			delete callback;
			callback=0;
		}
		if ((*env).ExceptionOccurred()) 
		{
			(*env).ExceptionDescribe();
			Sleep(30000);
		}
		if(cls!=0)
		(*env).CallStaticVoidMethod( cls, mid3,0);

		(*jvm).DetachCurrentThread();
		(*jvm).DestroyJavaVM();	
		env=0;
		delete options[1].optionString;
		delete options[2].optionString;
		delete options[3].optionString;
	}

	~JediNativeInterface()
	{
		if(callback)
		{
			callback->stop();
			delete callback;
			callback=0;
		}
		
		if(env!=0)
			destroyJNI();

	}

};
