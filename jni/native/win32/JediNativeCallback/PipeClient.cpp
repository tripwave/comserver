#include "stdafx.h"

#include "pipeclient.h"


CPipeClient::CPipeClient(int chan,const char * service)
{
	// Try to open a named pipe; wait for it, if necessary. 
 memset(chBuf,0,BUFSIZE);
 
 lpszPipename=TEXT("\\\\.\\pipe\\mynamedpipe");

   while (1) 
   { 
      hPipe = CreateFile( 
         lpszPipename,   // pipe name 
         GENERIC_READ |  // read and write access 
         GENERIC_WRITE, 
         0,              // no sharing 
         NULL,           // default security attributes
         OPEN_EXISTING,  // opens existing pipe 
         0,              // default attributes 
         NULL);          // no template file 
 
   // Break if the pipe handle is valid. 
 
      if (hPipe != INVALID_HANDLE_VALUE) 
         break; 
 
      
 
      if (GetLastError() != ERROR_PIPE_BUSY) 
      {
         printf("Could not open pipe.\n\r"); 
       
		return;
      }
 
      // All pipe instances are busy, so wait for 1 millisecond. 3 tries. 
 
      if (!WaitNamedPipe(lpszPipename, 1)) 
      { 
	     if (!WaitNamedPipe(lpszPipename, 1)) 
	     { 
		   
			 if (!WaitNamedPipe(lpszPipename, 1)) 
		     { 				 
				 return;
		     }
	     }
      } 
   } 
 
// The pipe connected; change to message-read mode. 
 
   dwMode = PIPE_READMODE_BYTE ;
   fSuccess = SetNamedPipeHandleState( 
      hPipe,    // pipe handle 
      &dwMode,  // new pipe mode 
      NULL,     // don't set maximum bytes 
      NULL);    // don't set maximum time 
   if (!fSuccess) 
   {
      printf("SetNamedPipeHandleState failed\r\n"); 
      return ;
   }

	memset(rdBuf,0,BUFSIZE);

	rdBuf[0]=((chan>>24) &0xff);
	rdBuf[1]=((chan>>16)&0xff);
	rdBuf[2]=((chan>>8)&0xff);
	rdBuf[3]=((chan)&0xff);

	 printf("SetNamedPipeHandlechannel %d\r\n",chan); 
	sprintf((rdBuf + 4 ),service);


	printf("Writing %s\r\n",rdBuf );
 
 fSuccess = WriteFile( 
      hPipe,                  // pipe handle 
      rdBuf,             // message 
      (lstrlen(service)+4)*sizeof(CHAR), // message length 
      &cbWritten,             // bytes written 
      NULL);                  // not overlapped 
   if (!fSuccess) 
   {
      printf("WriteFile failed\r\n"); 
      return ;
   }


  printf("Write File success. wrote %d\r\n",cbWritten); 
  
 memset(chBuf,0,BUFSIZE);
  
  do 
   { 
   // Read from the pipe. 
 
      fSuccess = ReadFile( 
         hPipe,    // pipe handle 
         chBuf,    // buffer to receive reply 
         BUFSIZE*sizeof(TCHAR),  // size of buffer 
         &cbRead,  // number of bytes read 
         NULL);    // not overlapped 
 
      if (! fSuccess && GetLastError() != ERROR_MORE_DATA) 
         break; 
		//Use Data
      
	
	
	
   
   } while (!fSuccess);  // repeat loop if ERROR_MORE_DATA 
	
   printf("Read File success %s\r\n",chBuf); 

}
void CPipeClient::getData(BYTE * pBuffer)
{
	sprintf((char*)pBuffer,chBuf);	
}
CPipeClient::~CPipeClient(void)
{
	getch(); 
	CloseHandle(hPipe);
}
