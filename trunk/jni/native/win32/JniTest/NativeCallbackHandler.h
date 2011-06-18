
#include "stdafx.h"

#include "ICallbackCapable.h"

#include "pipeClient.h"
#include "ICallbackCapable.h"

#define BUFSIZE 345600
 

static ICallbackCapable   *rtmpHandler[128];



HANDLE hPipe, hThread; 

bool doRun;






DWORD WINAPI InstanceThread(LPVOID lpvParam) ;

DWORD WINAPI MyThreadProc( LPVOID pParam )
{
	
	
	
	
	
	BOOL fConnected; 

	DWORD dwThreadId; 
	
	LPTSTR lpszPipename = TEXT("\\\\.\\pipe\\mynamedpipe"); 
 
// The main loop creates an instance of the named pipe and 
// then waits for a client to connect to it. When the client 
// connects, a thread is created to handle communications 
// with that client, and the loop is repeated. 
	doRun=true;

   while (doRun) 
   { 
       printf(".\r\n"); 
	   hPipe = CreateNamedPipe( 
          lpszPipename,             // pipe name 
          PIPE_ACCESS_DUPLEX,       // read/write access 
          PIPE_TYPE_BYTE |       // message type pipe 
          PIPE_READMODE_BYTE |   // message-read mode 
          PIPE_WAIT,                // blocking mode 
          PIPE_UNLIMITED_INSTANCES, // max. instances  
          BUFSIZE,                  // output buffer size 
          BUFSIZE,                  // input buffer size 
          0,                        // client time-out 
          NULL);                    // default security attribute 

      if (hPipe == INVALID_HANDLE_VALUE) 
      {
          printf("CreatePipe failed"); 
           
		  return 0;
      }
 
      // Wait for the client to connect; if it succeeds, 
      // the function returns a nonzero value. If the function
      // returns zero, GetLastError returns ERROR_PIPE_CONNECTED. 
 
      fConnected = ConnectNamedPipe(hPipe, NULL) ?  TRUE : (GetLastError() == ERROR_PIPE_CONNECTED); 
	
      if (fConnected) 
      { 
		

		 // Create a thread for this client. 
         hThread = CreateThread( 
            NULL,              // no security attribute 
            0,                 // default stack size 
           InstanceThread,    // thread proc
           (LPVOID) hPipe,    // thread parameter 
            0,                 // not suspended 
            &dwThreadId);      // returns thread ID 

         if (hThread == NULL) 
         {
            printf("CreateThread failed"); 
              
			return 0;
         }
		 else
		 {
			 CloseHandle(hThread); 
		 }
      } 
	  else 
	  {
        // The client could not connect, so close the pipe. 
		  CloseHandle(hPipe); 
	  }
   } 
   
  Sleep(1000);
	
   CloseHandle(hPipe);

	return 0;   // thread completed successfully
}


DWORD WINAPI InstanceThread(LPVOID lpvParam) 
{ 
   
	
	
	

	TCHAR chRequest[BUFSIZE]; 
   TCHAR chReply[BUFSIZE]; 

   DWORD cbBytesRead, cbReplyBytes, cbWritten; 
   BOOL fSuccess; 
   HANDLE hPipe = (HANDLE) lpvParam; 
 
	memset(chRequest,0,BUFSIZE);
	memset(chReply,0,BUFSIZE);
	
	



// The thread's parameter is a handle to a pipe instance. 
//ThreadArguments args=*( ThreadArguments *)lpvParam;
// args.hndlr->processCall("Moo","Car");

   while (1) 
   { 
   // Read client requests from the pipe. 
      fSuccess = ReadFile( 
         hPipe,        // handle to pipe 
         chRequest,    // buffer to receive data 
         BUFSIZE*sizeof(TCHAR), // size of buffer 
         &cbBytesRead, // number of bytes read 
         NULL);        // not overlapped I/O 

      if (! fSuccess || cbBytesRead == 0) 
         break;

	printf("The request size 1s %d\r\n",cbBytesRead );
      
	int chan= chRequest[0]<<24 | chRequest[1]<<16 | chRequest[2]<<8 | chRequest[3];
	  
	printf("The request channel is %d\r\n",chan );
	
	_tprintf( TEXT("\r\nGet Answer To Request\r\n") );	
	
	if(strcmp(chRequest,"kill")!=0)	
	{

		cbReplyBytes=rtmpHandler[chan]->processCall((chRequest + 4),chReply);
	}else{
		sprintf(chReply,"killing");
		cbReplyBytes=strlen(chReply);
	}

   // Write the reply to the pipe. 
      fSuccess = WriteFile( 
         hPipe,        // handle to pipe 
         chReply,      // buffer to write from 
         cbReplyBytes, // number of bytes to write 
         &cbWritten,   // number of bytes written 
         NULL);        // not overlapped I/O 

      if (! fSuccess || cbReplyBytes != cbWritten) 
		  break; 
	} 
 
// Flush the pipe to allow the client to read the pipe's contents 
// before disconnecting. Then disconnect the pipe, and close the 
// handle to this pipe instance. 
	try
	{
		FlushFileBuffers(hPipe); 
		DisconnectNamedPipe(hPipe); 
		CloseHandle(hPipe); 
	}
	catch (...)
	{
 
	}

   return 0;
}

























class NativeCallbackHandler 
{

private:

	HANDLE hnd;
	
	ICallbackCapable *handler;

	DWORD   dwThread;
	
public:




	NativeCallbackHandler()
	{		
		hnd=0;
		dwThread=0;
		
		
	}
	void addCallbackHandler(ICallbackCapable  *hndlr)
	{
		handler=hndlr;

		int newId=hndlr->getId();

		rtmpHandler[newId]=hndlr;

		printf("Adding handler id :%d\r\n",rtmpHandler[newId]->getId());
	}

	const char * getVersion()
	{
		return "NativeCallbackHandler v-0.1";
	}

	int processCall(char * data,char * results )
	{
		_tprintf( TEXT("Process Call\r\n") );	
		
		if(handler->getId()>0){
			printf("Using handler id :%d\r\n",handler->getId());
			int res=handler->processCall(data,results);
			return res;
		}
		printf("ARGH! \r\n");
		
		return 0;

	}

	void start()
	{	


		hnd= ::CreateThread( 
            NULL,          // default security attributes
            0,             // use default stack size  
            MyThreadProc,  // thread function name
          (LPVOID) &(*this),          // argument to thread function 
            0,             // use default creation flags 
           &dwThread);    // returns the thread identifier 
	}

	void stop()
	{
		if(hnd)
		{	
			doRun=false;
						
			CloseHandle(hnd);

			hnd=0;
		}
	}

	~NativeCallbackHandler()
	{
		stop();
		
	
		
		
	}

};






