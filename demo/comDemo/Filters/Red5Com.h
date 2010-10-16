#pragma once
#define WIN32_LEAN_AND_MEAN//winsock =)
#define DEFAULT_OUTBUFLEN 2048
#define DEFAULT_OUTPORT "8001"	
#define DEFAULT_ADDRESS "127.0.0.1" //Port out.
#define DEFAULT_PASSWORD "changeme" // Port out.
#include <winsock2.h>
#include <ws2tcpip.h>
class Red5Com
{

public:
	Red5Com(void);
	~Red5Com(void);

	int makeConnection();
	
	char * process();

	WSADATA			wsaData;
    SOCKET			ConnectSocket;
	struct			addrinfo *result ,
                    *ptr ,
                    hints;
    char			*sendbuf,
					recvbuf[DEFAULT_OUTBUFLEN];
	int				iResult, iRecResult;
    int				recvbuflen, sendbufl;

};
