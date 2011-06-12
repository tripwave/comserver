// JniCuda.h : main header file for the JniCuda DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols




class CJniCudaApp : public CWinApp
{
public:
	
	void sampleEvent( double SampleTime, BYTE * pBuffer, long BufferLen);
	CJniCudaApp();



// Overrides
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};
