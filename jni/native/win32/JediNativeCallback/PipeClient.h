#pragma once



#include <windows.h> 
#include <stdio.h>
#include <conio.h>
#include <tchar.h>

#define BUFSIZE 345600

class CPipeClient
{
public:
	CPipeClient(int chan,const char * service);
	~CPipeClient(void);
	void getData(BYTE *pBuffer);   
	
	HANDLE hPipe; 
  // LPTSTR lpvMessage; 
   CHAR chBuf[BUFSIZE];
   CHAR rdBuf[BUFSIZE]; 
   BOOL fSuccess; 
   DWORD cbRead, cbWritten, dwMode; 
   LPTSTR lpszPipename;



};
