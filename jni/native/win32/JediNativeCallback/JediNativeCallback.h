// JediNativeCallback.h : main header file for the JediNativeCallback DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols


// CJediNativeCallbackApp
// See JediNativeCallback.cpp for the implementation of this class
//

class CJediNativeCallbackApp : public CWinApp
{
public:
	CJediNativeCallbackApp();

// Overrides
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};
