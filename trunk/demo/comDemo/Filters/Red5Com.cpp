
#include "red5com.h"

Red5Com::Red5Com(void)
{
	recvbuflen=DEFAULT_OUTBUFLEN;
    ConnectSocket = INVALID_SOCKET;
    result = NULL;
    ptr = NULL;
}
char * Red5Com::process()
{
	if(ConnectSocket == INVALID_SOCKET)
	{
		if(makeConnection() < 1)
			return NULL;
	}
	memset(recvbuf, 0, sizeof(recvbuf));
	
	iResult = send( ConnectSocket, "ping", (int)strlen("ping"), 0 );
			if (iResult == SOCKET_ERROR) 
			{
				printf("ping failed: %d\n", WSAGetLastError());
				closesocket(ConnectSocket);
				ConnectSocket = INVALID_SOCKET;
				return recvbuf;
			}
		
	



	iResult = recv(ConnectSocket, recvbuf, recvbuflen, 0);
		if ( iResult > 0 )
				{
								
				}
			
        
		else if ( iResult == 0 )
		{
			printf("Connection closed\n");
			closesocket(ConnectSocket);
			ConnectSocket = INVALID_SOCKET;

		}
        
		else
		{ 
			printf("recv failed: %d\n", WSAGetLastError());
			closesocket(ConnectSocket);
			ConnectSocket = INVALID_SOCKET;

		}	
	
	
	
	return recvbuf;
}
int Red5Com::makeConnection()
{
 iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
    if (iResult != 0) 
		{
			printf("WSAStartup failed: %d\n", iResult);
			return -1;
		}

    ZeroMemory( &hints, sizeof(hints) );
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
	 // Resolve the server address and port
    iResult = getaddrinfo("127.0.0.1", "2000" , &hints, &result);
    if ( iResult != 0 ) 
	{
        printf("getaddrinfo failed: %d\n", iResult);
        return -1;
    }
	 // Attempt to connect to an address until one succeeds
    for(ptr=result; ptr != NULL ;ptr=ptr->ai_next) {

        // Create a SOCKET for connecting to server
        ConnectSocket = socket(ptr->ai_family, ptr->ai_socktype, 
            ptr->ai_protocol);
        if (ConnectSocket == INVALID_SOCKET) 
		{
            printf("Error at socket(): %ld\n", WSAGetLastError());
            freeaddrinfo(result);
            return -1;
        }

        // Connect to server.
		
        iResult = connect( ConnectSocket, ptr->ai_addr, (int)ptr->ai_addrlen);
        if (iResult == SOCKET_ERROR) 
		{
            closesocket(ConnectSocket);
            ConnectSocket = INVALID_SOCKET;
            continue;
        }
        break;
    }

    freeaddrinfo(result);

    if (ConnectSocket == INVALID_SOCKET) 
	{
        printf("Unable to connect to server!\n");

        return -1;
	}


//////////////////////Make password/////////////////////////	
char outbuffer[3];/////////And StreamHeader/////////////////
sprintf(outbuffer, "GET" );


///////////Send Password//////////////////////////////////////
	iResult = send( ConnectSocket, outbuffer, (int)strlen(outbuffer), 0 );
			if (iResult == SOCKET_ERROR) 
			{
				printf("send failed: %d\n", WSAGetLastError());
				closesocket(ConnectSocket);
				ConnectSocket = INVALID_SOCKET;
				return -1;
			}
		
	memset(recvbuf, 0, sizeof(recvbuf));

//////////////GET OK2 ///////////////////////////////////////
	iResult = recv(ConnectSocket, recvbuf, recvbuflen, 0);
		if ( iResult > 0 )
				{	
					printf(recvbuf);
				}
        
		else if ( iResult == 0 )
		{
			printf("Connection closed\n");
			closesocket(ConnectSocket);
			ConnectSocket = INVALID_SOCKET;

		}
        
		else
		{ 
			printf("recv failed: %d\n", WSAGetLastError());
			closesocket(ConnectSocket);
			ConnectSocket = INVALID_SOCKET;

		}
			
	return iResult;
}
Red5Com::~Red5Com(void)
{
}
