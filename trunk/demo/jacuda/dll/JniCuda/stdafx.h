// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently

#pragma once

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN		// Exclude rarely-used stuff from Windows headers
#endif

// Modify the following defines if you have to target a platform prior to the ones specified below.
// Refer to MSDN for the latest info on corresponding values for different platforms.
#ifndef WINVER				// Allow use of features specific to Windows 95 and Windows NT 4 or later.
#define WINVER 0x0400		// Change this to the appropriate value to target Windows 98 and Windows 2000 or later.
#endif

#ifndef _WIN32_WINNT		// Allow use of features specific to Windows NT 4 or later.
#define _WIN32_WINNT 0x0400	// Change this to the appropriate value to target Windows 2000 or later.
#endif						

#ifndef _WIN32_WINDOWS		// Allow use of features specific to Windows 98 or later.
#define _WIN32_WINDOWS 0x0410 // Change this to the appropriate value to target Windows Me or later.
#endif

#ifndef _WIN32_IE			// Allow use of features specific to IE 4.0 or later.
#define _WIN32_IE 0x0400	// Change this to the appropriate value to target IE 5.0 or later.
#endif

#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS	// some CString constructors will be explicit

#include <afxwin.h>         // MFC core and standard components
#include <afxext.h>         // MFC extensions

#ifndef _AFX_NO_OLE_SUPPORT
#include <afxole.h>         // MFC OLE classes
#include <afxodlgs.h>       // MFC OLE dialog classes
#include <afxdisp.h>        // MFC Automation classes
#endif // _AFX_NO_OLE_SUPPORT

#ifndef _AFX_NO_DB_SUPPORT
#include <afxdb.h>			// MFC ODBC database classes
#endif // _AFX_NO_DB_SUPPORT

#ifndef _AFX_NO_DAO_SUPPORT
#include <afxdao.h>			// MFC DAO database classes
#endif // _AFX_NO_DAO_SUPPORT

#include <afxdtctl.h>		// MFC support for Internet Explorer 4 Common Controls
#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h>			// MFC support for Windows Common Controls
#endif // _AFX_NO_AFXCMN_SUPPORT



#include <atlbase.h>
#include <windows.h>
#include <dshow.h>
#include <stdio.h>
#include <atldebugapi.h>

#include "qedit.h"
//#include "INVVESetting.h"

#include "jni.h"
#include "Capture.h"

#include "NVEncodeDataTypes.h"
#include "VideoFrame.h"

//
// Macros
//

#define SAFE_RELEASE(x) { if (x) x->Release(); x = NULL; }

#define JIF(x) if (FAILED(hr=(x))) \
    {Msg(TEXT("FAILED(hr=0x%x) in ") TEXT(#x) TEXT("\n\0"), hr); return hr;}

#define WM_GRAPHNOTIFY  WM_APP+1


static const GUID CLSID_NVIDIA_VideoEncoderFilter= {0xb63e31d0, 0x87b5, 0x477f, 0xb2, 0x24, 0x4a, 0x35, 0xb6, 0xbe, 0xce,0xd6};
static const GUID IID_INVVESetting = {0x4597f768, 0xf60, 0x4e5b, 0xb6, 0x97, 0x67, 0xeb, 0x26, 0x14, 0xdc, 0xb5};
static const GUID GUID_Microsoft_Video_Decoder = { 0x212690FB, 0x83E5, 0x4526, { 0x8F, 0xD7, 0x74, 0x47, 0x8B, 0x79, 0x39, 0xCD } };
static const GUID MEDIASUBTYPE_h264 = { 0x34363268, 0x0000, 0x0010, 0x80, 0x00, 0x00, 0xaa, 0x00, 0x38, 0x9b, 0x71};

// 
// INVVESetting interface Functions
// Interface used to set/get capability of the video encoder
//
DECLARE_INTERFACE_(INVVESetting, IUnknown)
{
// Function :
//      IsSupportedCodec
// Description:
//      Query if the codec format is supported by the encoder
// Parameter:
//      dwCodecType ( I )
// Return:
//      S_OK                : The format is supported
//      E_NOINTERFACE_FAIL  : The format is not supported
//      E_FAIL              : No CUDA capability present
//
    STDMETHOD(IsSupportedCodec)(THIS_ DWORD dwCodecType) PURE;

// Function :
//      IsSupportedCodecProfile
// Description:
//      Query if the profile for codec format is supported by the encoder
// Parameter:
//      dwCodecType ( I )
//      dwProfileType ( I )
// Return:
//      S_OK                : The profile is supported
//      E_NOINTERFACE_FAIL  : The profile is not supported
//      E_FAIL              : No CUDA capability present
//
    STDMETHOD(IsSupportedCodecProfile)(THIS_ DWORD dwCodecType, DWORD dwProfileType) PURE;

// Function :
//      SetCodecType
// Description:
//      Set encoder codec format
// Parameter:
//      dwCodecType( I )
// Return:
//      S_OK                : Successful
//      E_FAIL              : Fail
//
    STDMETHOD(SetCodecType)(THIS_ DWORD dwCodecType) PURE;

// Function :
//      GetCodecType
// Description:
//      Get the current encoding format
// Parameter:
//      pwdCodecType( O )
// Return:
//      S_OK                : Successful
//      E_FAIL              : The encoding format is not initialized
//      E_POINTER           : pwdCodecType is NULL pointer.
//
    STDMETHOD(GetCodecType)(THIS_ DWORD *pdwCodecType) PURE;

// Function :
//      IsSupportedParam
// Description:
//      Query if the parameter type is supported. This depends on the encoding 
//      format as well as the version of the codec library
// Parameter:
//      dwParamType ( I )
// Return:
//      S_OK                : The parameter is supported
//      E_FAIL              : The parameter is not supported
//
    STDMETHOD(IsSupportedParam)(THIS_ DWORD dwParamType) PURE;

// Function :
//      SetParamValue
// Description:
//      Set the value of the specified parameter type. The pData points to a 
//      memory region storing the value of the parameter. The parameter can be 
//      a data structure, which must match the size of the parameter type
// Parameter:
//      dwParamType( I )
//      pData( I )
// Return:
//      S_OK                : Successful
//      E_FAIL              : Fail to set the value
//      E_NOTIMPL           : Parameter is not adjustable
//      E_UNEXPECTED        : The encoding format is not initialized yet
//      E_POINTER           : pData is NULL pointer
//
    STDMETHOD(SetParamValue)(THIS_ DWORD dwParamType, LPVOID pData) PURE;

// Function :
//      GetParamValue
// Description:
//      Query the current value of the specified parameter type
// Parameter:
//      dwParamType( I )
//      pData( O )
// Return:
//      S_OK                : Successful
//      E_NOTIMPL           : The parameter is not supported
//      E_UNEXPECTED        : The encoding format is not initialized
//      E_POINTER           : pData is NULL pointer
//
    STDMETHOD(GetParamValue)(THIS_ DWORD dwParamType, LPVOID pData) PURE;

// Function :
//      SetDefaultParam
// Description:
//      Applies default settings of the encoding format
// Parameter:
//      None
// Return:
//      S_OK                : Successful
//      E_UNEXPECTED        : The encoding format is not set yet
//
    STDMETHOD(SetDefaultParam)(THIS_ void) PURE;

// Function :
//      GetSPSPPS
// Description:
//      Fetches the buffer containing SPS and PPS
// Parameter:
//      pSPSPPSbfr( O )
//      nSizeSPSPPSbfr ( I )
//      pDatasize( O )
// Return:
//      S_OK                : Successful
//      E_UNEXPECTED        : The encoder is not initialized
//      E_POINTER           : NULL input pointer
//
    STDMETHOD(GetSPSPPS)(THIS_ unsigned char *pSPSPPSbfr, int nSizeSPSPPSbfr, int *pDatasize) PURE;
};



