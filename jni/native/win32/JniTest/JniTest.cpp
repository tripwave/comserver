// JniTest.cpp : Defines the entry point for the console application.

#include "stdafx.h"

#include "JediNativeInterface.h"

#include "RTMPClient.h"

int _tmain(int argc, _TCHAR* argv[])
{		
	JediNativeInterface * test= new JediNativeInterface();

	test->createJNI();

	RTMPClient * client=0;

	//client = new RTMPClient(test);
	//client->createClient();		
	//client->setModeVal(0);
	//client->setAppName("cuda");
	//client->setHostName("127.0.0.1");
	//client->setPortNum(1935);
	//client->setPublishedName("test");
	//client->initiateCapture();
	
	Sleep(10000);

	if(client){
		client->close();
		delete client;
	}

	test->destroyJNI();
	
	Sleep(10000);
	
	delete test;

	return 0;
}

