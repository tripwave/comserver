
#pragma once
#include "wingdi.h"

struct BLTRECT{int Left; int Top; int Width; int Height;};
class CAutoDC 
{
public:
	
	void TextSample(void * pBuffer, char *text, int Linenum);
	void TextSample(void * pBuffer, char *text);
	void *Pip(void * Main, void * Pip);
	void *PipRect (void* Main, void* Pip, BLTRECT *rect,int Alpha);
	void *Logo(void * Main, void * Pip);
	void SetColor1(RGBQUAD * pQuad1);
	void SetColor2(RGBQUAD * pQuad2);
	void SetAlpha1(BYTE * pAlpha1);
	void SetAlpha2(BYTE * pAlpha2);
	void GetColor1(RGBQUAD * pQuad1);
	void GetColor2(RGBQUAD * pQuad2);
	void GetAlpha1(BYTE * pAlpha1);
	void GetAlpha2(BYTE * pAlpha2);
	char* Translate(char * Main,char * pBuffer);
	// the DC is hardwired to my media type, however parameterizing this would be easy!!
	// Bewarned IM a beginner and may do things the hard and wrong way!
	CAutoDC();
	~CAutoDC();


protected:
	BYTE alpha;
	BYTE alpha2;
	RGBQUAD		tColor;
	RGBQUAD		t2Color;
	CCritSec cSharedState;//Our critical section.
	void MakeMediaType();
	void MakeDC();
	HBITMAP		hDibSection,	hDibSection2;
	BITMAPINFO	m_bmpInfo,		m_bmpInfo2;
	HDC			m_dcPaint,		m_dcPaint2;
	void		*m_pPaintBuffer,*m_pPaintBuffer2;
	CMediaType	*pMediaType;
	

};