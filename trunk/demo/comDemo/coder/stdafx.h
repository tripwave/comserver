// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once
#define WIN32_MEAN_AND_LEAN
#include"windows.h"
#include "resource.h"
#include <stdio.h>
#include <iostream>
#include <tchar.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdarg.h>

#define VPX_CODEC_DISABLE_COMPAT 1
#include "vpx_encoder.h"
#include "vp8cx.h"
#include "enc_if.h"


#define interface (&vpx_codec_vp8_cx_algo)
#define fourcc    0x30385056
#define IVF_FILE_HDR_SZ  (32)
#define IVF_FRAME_HDR_SZ (12)


#define rgbtoy(b, g, r, y) \
y=(unsigned char)(((int)(30*r) + (int)(59*g) + (int)(11*b))/100)

#define rgbtoyuv(b, g, r, y, u, v) \
rgbtoy(b, g, r, y); \
u=(unsigned char)(((int)(-17*r) - (int)(33*g) + (int)(50*b)+12800)/100); \
v=(unsigned char)(((int)(50*r) - (int)(42*g) - (int)(8*b)+12800)/100)


extern "C" {

	int _stat32( const char *path, struct _stat *buffer ) 
	{
		return _stat(path, buffer); 
	}
	int _wstat32( const wchar_t *path, struct _stat *buffer ) 
	{ 
		return _wstat(path, buffer);
	}
	long _ftol( double );

	long _ftol2_sse( double dblSource ) 
	{ 
		return _ftol( dblSource ); 
	}
	int vsnprintf(char * c,size_t t,const char * g,va_list vL)
	{
		return _vsnprintf( c, t, g, vL);
	}
};