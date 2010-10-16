
#pragma once
#include "streams.h"
#include "stdio.h"
#include <tchar.h>

#include "CAutoDC.h"
#include<iostream>
#define CSAMP 307200
using namespace std;


char *CAutoDC::Translate(char * Main ,char *pBuffer)
{
	//Clear DC paintbuffer
	PatBlt(m_dcPaint,0,0,320,240,BLACKNESS);
	
	//Copy Sample to DC paintbuffer
	CopyMemory(m_pPaintBuffer,pBuffer,307200);
	//Set up Blend function
	StretchBlt(m_dcPaint2,0,239,320,-240,m_dcPaint,0,0,320,240,SRCCOPY);
	CopyMemory(Main,m_pPaintBuffer2,abs(m_bmpInfo.bmiHeader.biWidth*m_bmpInfo.bmiHeader.biHeight*m_bmpInfo.bmiHeader.biBitCount/8));
	return Main;
}
void CAutoDC::GetColor1(RGBQUAD *pQuad1)
{
	CAutoLock lock(&cSharedState);
	*pQuad1=tColor;
	return ;
}
void CAutoDC::GetColor2(RGBQUAD *pQuad2)
{
		CAutoLock lock(&cSharedState);
	*pQuad2=t2Color;
	return ;
}
void CAutoDC::GetAlpha1( BYTE * Alpha1)
{
	CAutoLock lock(&cSharedState);
	*Alpha1=alpha;
	return;
}
void CAutoDC::GetAlpha2( BYTE * Alpha2)
{
		CAutoLock lock(&cSharedState);
	*Alpha2=alpha2;
	return;
}
void CAutoDC::SetColor2 (RGBQUAD *pQuad2)
{//State changing method
	CAutoLock lock(&cSharedState);
	t2Color=*pQuad2;
}
void CAutoDC::SetColor1 (RGBQUAD *pQuad1)
{//State changing method
	CAutoLock lock(&cSharedState);
	tColor=*pQuad1;
}
void CAutoDC::SetAlpha2 (BYTE * pAlpha2)
{//State changing method
	CAutoLock lock(&cSharedState);
	alpha2=*pAlpha2;
}
void CAutoDC::SetAlpha1 (BYTE * pAlpha1)
{//State changing method
	CAutoLock lock(&cSharedState);
	alpha=*pAlpha1;
}
void *CAutoDC::Logo (void* Main, void* Pip)
{//Streaming method
	CAutoLock lock(&cSharedState);
//Picture in Picture
	//Clear DC paintbuffer
	PatBlt(m_dcPaint,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	PatBlt(m_dcPaint2,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	//Copy Sample to DC paintbuffer
	CopyMemory(m_pPaintBuffer,Main,CSAMP);
	CopyMemory(m_pPaintBuffer2,Pip,CSAMP);
	//Set up Blend function
	BLENDFUNCTION blend;
	blend.BlendOp =AC_SRC_OVER;
	blend.BlendFlags =0;
	blend.SourceConstantAlpha =alpha;
	
//	AlphaBlend(m_dcPaint,140,110,160,120,m_dcPaint2,0,0,320,240,blend);
	CopyMemory(Main,m_pPaintBuffer,CSAMP);
	return Main;
}

void *CAutoDC::Pip (void* Main, void* Pip)
{//Streaming method
	
	//Picture in Picture
	//Clear DC paintbuffer
	PatBlt(m_dcPaint,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	PatBlt(m_dcPaint2,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	//Copy Sample to DC paintbuffer
	CopyMemory(m_pPaintBuffer,Main,CSAMP);
	CopyMemory(m_pPaintBuffer2,Pip,CSAMP);
	//Set up Blend function
	BLENDFUNCTION blend;
	blend.BlendOp =AC_SRC_OVER;
	blend.BlendFlags =0;
	blend.SourceConstantAlpha =alpha;
	
//	AlphaBlend(m_dcPaint,140,110,160,120,m_dcPaint2,0,0,320,240,blend);
	CopyMemory(Main,m_pPaintBuffer,CSAMP);
	return Main;
}
void *CAutoDC::PipRect (void* Main, void* Pip, BLTRECT *Rect, int Alpha)
{//Streaming method
	
	//Picture in Picture
	//Clear DC paintbuffer
	PatBlt(m_dcPaint,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	PatBlt(m_dcPaint2,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	//Copy Sample to DC paintbuffer
	CopyMemory(m_pPaintBuffer,Main,CSAMP);
	CopyMemory(m_pPaintBuffer2,Pip,CSAMP);
	//Set up Blend function
	BLENDFUNCTION blend;
	blend.BlendOp =AC_SRC_OVER;
	blend.BlendFlags =0;
	blend.SourceConstantAlpha =Alpha;
	
//	AlphaBlend(m_dcPaint,Rect->Left ,Rect->Top ,Rect->Width ,Rect->Height ,m_dcPaint2,0,0,320,240,blend);
	CopyMemory(Main,m_pPaintBuffer,CSAMP);
	return Main;
}


void CAutoDC::TextSample (void * pBuffer,char * text)
{//Streaming method
	CAutoLock lock(&cSharedState);
	//Clear DC paintbuffer
	PatBlt(m_dcPaint,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	//Copy Sample to DC paintbuffer
	CopyMemory(m_pPaintBuffer,pBuffer,CSAMP);
	CHAR szText[256];
	sprintf( szText,text);
	//Text background color
	SetBkMode(m_dcPaint,TRANSPARENT);
	SetTextColor(m_dcPaint,RGB(0 ,0 ,0 ));
	//Do the text
	if( !TextOut( m_dcPaint, (m_bmpInfo.bmiHeader.biWidth/2-((strlen(text)/2)*8))+1, m_bmpInfo.bmiHeader.biHeight-19, szText,_tcslen( szText ) ) )
		return ;
	// Text Color

	//SetTextColor(m_dcPaint,(COLORREF)tColor);
	SetTextColor(m_dcPaint,RGB(t2Color.rgbRed ,t2Color.rgbGreen ,t2Color.rgbBlue ));
	
	//Do the text
	if( !TextOut( m_dcPaint, (m_bmpInfo.bmiHeader.biWidth/2-((strlen(text)/2)*8)), m_bmpInfo.bmiHeader.biHeight-20, szText,_tcslen( szText ) ) )
		return ;
	//Copy sample back to original buffer
	CopyMemory(pBuffer,m_pPaintBuffer,CSAMP);
}
void CAutoDC::TextSample (void * pBuffer,char * text, int Linenum)
{//Streaming method
	CAutoLock lock(&cSharedState);
	Linenum++;
	//Clear DC paintbuffer
	PatBlt(m_dcPaint,0,0,m_bmpInfo.bmiHeader.biWidth,m_bmpInfo.bmiHeader.biHeight,BLACKNESS);
	//Copy Sample to DC paintbuffer
	CopyMemory(m_pPaintBuffer,pBuffer,CSAMP);
	CHAR szText[256];
	sprintf( szText,text);
	//Text background color
	SetBkMode(m_dcPaint,TRANSPARENT);
	// Text Color
	SetTextColor(m_dcPaint,RGB(0 ,0 ,0 ));
	//Do the text
	if( !TextOut( m_dcPaint, 4, (((m_bmpInfo.bmiHeader.biHeight-18)/12) * Linenum)-19, szText,_tcslen( szText ) ) )
		return ;
	//SetTextColor(m_dcPaint,(COLORREF)tColor);
	SetTextColor(m_dcPaint,RGB(tColor.rgbRed ,tColor.rgbGreen ,tColor.rgbBlue));
	//Do the text
	if( !TextOut( m_dcPaint, 3, (((m_bmpInfo.bmiHeader.biHeight-20)/12) * Linenum)-20, szText,_tcslen( szText ) ) )
		return ;
	//Copy sample back to original buffer
	CopyMemory(pBuffer,m_pPaintBuffer,CSAMP);
}
void CAutoDC::MakeMediaType()
{
	ZeroMemory(pMediaType, sizeof(CMediaType));
		VIDEOINFO *pvi = (VIDEOINFO *)pMediaType->AllocFormatBuffer(sizeof(VIDEOINFO));
		if (NULL == pvi) 
			return ;//E_OUTOFMEMORY;

		ZeroMemory(pvi, sizeof(VIDEOINFO));

		pvi->bmiHeader.biCompression	= BI_RGB;
		pvi->bmiHeader.biBitCount		= 32;
		pvi->bmiHeader.biSize			= sizeof(BITMAPINFOHEADER);
		pvi->bmiHeader.biWidth			= 320;
		pvi->bmiHeader.biHeight			= 240;
		pvi->bmiHeader.biPlanes			= 1;
		pvi->bmiHeader.biSizeImage		= GetBitmapSize(&pvi->bmiHeader);
		pvi->bmiHeader.biClrImportant	= 0;

		SetRectEmpty(&(pvi->rcSource));	// we want the whole image area rendered.
		SetRectEmpty(&(pvi->rcTarget));	// no particular destination rectangle

		pMediaType->SetType(&MEDIATYPE_Video);
		pMediaType->SetFormatType(&FORMAT_VideoInfo);
		pMediaType->SetTemporalCompression(FALSE);

		const GUID SubTypeGUID = GetBitmapSubtype(&pvi->bmiHeader);
		pMediaType->SetSubtype(&MEDIASUBTYPE_RGB32);
		pMediaType->SetSampleSize(pvi->bmiHeader.biSizeImage);

		m_bmpInfo.bmiHeader = pvi->bmiHeader;
	

	return ;
}
void CAutoDC::MakeDC()
{
	HBITMAP hDibSection = CreateDIBSection(NULL, (BITMAPINFO *) &m_bmpInfo, DIB_RGB_COLORS,&m_pPaintBuffer, NULL, 0);
	HBITMAP hDibSection2 = CreateDIBSection(NULL, (BITMAPINFO *) &m_bmpInfo2, DIB_RGB_COLORS,&m_pPaintBuffer2, NULL, 0);
	
	HDC hDC = GetDC(NULL);
	
	m_dcPaint = CreateCompatibleDC(hDC);
	m_dcPaint2 = CreateCompatibleDC(hDC);

	SetMapMode(m_dcPaint, GetMapMode(hDC));	
	SetMapMode(m_dcPaint2, GetMapMode(hDC));	
	
	HGDIOBJ OldObject = SelectObject(m_dcPaint,hDibSection);
	HGDIOBJ OldObject2 = SelectObject(m_dcPaint2,hDibSection2);
}
CAutoDC::CAutoDC()
{
CAutoLock lock(&cSharedState);
alpha=0;
alpha2=0;
pMediaType= new CMediaType;
this->MakeMediaType ();
this->MakeDC ();
t2Color.rgbBlue =t2Color.rgbGreen =t2Color.rgbRed =255;
tColor.rgbBlue =255;
tColor.rgbGreen =255;
tColor.rgbRed=255;
tColor.rgbReserved =0;
t2Color.rgbReserved =0;
}
CAutoDC::~CAutoDC()
{
DeleteDC(m_dcPaint);
DeleteDC(m_dcPaint2);

}

