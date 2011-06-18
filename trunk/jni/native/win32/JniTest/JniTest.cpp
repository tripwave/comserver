// JniTest.cpp : Defines the entry point for the console application.

#include "stdafx.h"

#include "JediNativeInterface.h"

#include "Red5Server.h"






int _tmain(int argc, _TCHAR* argv[])
{		
	JediNativeInterface * test= new JediNativeInterface();
	
	test->createJNI();

	Red5Server * server= new Red5Server(test);

	test->addCallbackHandler(server);

	server->boot();

	Sleep(5000);
	printf(".\r\n");
	Sleep(1000);
	printf(".\r\n");
	Sleep(1000);
	server->shutdown();

	delete server;

	test->destroyJNI();
	printf(".\r\n");
	Sleep(1000);
	printf(".\r\n");
	Sleep(1000);
	printf(".\r\n");
	Sleep(1000);
	printf(".\r\n");
	Sleep(10000);
	printf(".\r\n");
	printf(".\r\n");


	delete test;

	return 0;
}

