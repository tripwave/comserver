#pragma warning(disable:4244)
#pragma warning(disable:4711)
#define WIN32_LEAN_AND_MEAN//winsock =)
#include <streams.h>
#include <sstream>
#include <stdio.h>
#include <olectl.h>
#include <dvdmedia.h>
using namespace std;

#include "filters.h"
//
// 
//
//////////////////////////////////////////////////////////////////////////
//  CVCam is the source filter which masquerades as a capture device
//////////////////////////////////////////////////////////////////////////
CUnknown * WINAPI CVCam::CreateInstance(LPUNKNOWN lpunk, HRESULT *phr)
{
    ASSERT(phr);
    CUnknown *punk = new CVCam(lpunk, phr);
    return punk;
}

CVCam::CVCam(LPUNKNOWN lpunk, HRESULT *phr) : 
    CSource(NAME("Flex COM"), lpunk, CLSID_FlexCOM)
{
    ASSERT(phr);
    CAutoLock cAutoLock(&m_cStateLock);
    // Create the one and only output pin
    m_paStreams = (CSourceStream **) new CVCamStream*[1];
    m_paStreams[0] = new CVCamStream(phr, this, L"Flex COM");
}

HRESULT CVCam::QueryInterface(REFIID riid, void **ppv)
{
    //Forward request for IAMStreamConfig & IKsPropertySet to the pin
    if(riid == _uuidof(IAMStreamConfig) || 
		riid == _uuidof(IAMDroppedFrames)  ||
		riid == _uuidof(IKsPropertySet))
        return m_paStreams[0]->QueryInterface(riid, ppv);
    else
        return CSource::QueryInterface(riid, ppv);
}

//////////////////////////////////////////////////////////////////////////
// CVCamStream is the one and only output pin of CVCam which handles 
// all the stuff.
//////////////////////////////////////////////////////////////////////////
CVCamStream::CVCamStream(HRESULT *phr, CVCam *pParent, LPCWSTR pPinName) :
    CSourceStream(NAME("Flex COM"),phr, pParent, pPinName), m_pParent(pParent)
{
	NumDroppedFrames=0;
	NumFrames=0;
    GetMediaType(0, &m_mt);
	adc=new CAutoDC();
	r5c=new Red5Com();
	r5c->makeConnection();
}

CVCamStream::~CVCamStream()
{

} 

HRESULT CVCamStream::QueryInterface(REFIID riid, void **ppv)
{   
    // Standard OLE stuff
    if(riid == _uuidof(IAMStreamConfig))
        *ppv = (IAMStreamConfig*)this;
    else if(riid == _uuidof(IKsPropertySet))
        *ppv = (IKsPropertySet*)this;
	else if(riid == _uuidof(IAMDroppedFrames)  )
	*ppv = (IAMDroppedFrames*)this;
	else
        return CSourceStream::QueryInterface(riid, ppv);

    AddRef();
    return S_OK;
}


//////////////////////////////////////////////////////////////////////////
//  This is the routine where we create the data being output by the Virtual
//  Camera device.
//////////////////////////////////////////////////////////////////////////

HRESULT CVCamStream::FillBuffer(IMediaSample *pms)
{
    
    HRESULT hr=S_OK;
   	
	
	//create some working info
	REFERENCE_TIME rtNow,rtDelta ,rtDelta2=0;//delta for dropped, delta 2 for sleep.
	REFERENCE_TIME avgFrameTime = ((VIDEOINFOHEADER*)m_mt.pbFormat)->AvgTimePerFrame;

	//What TIme is iT REALLY???
	m_pParent->GetSyncSource(&m_pClock);
	
	m_pClock->GetTime(&refSync1);
	if(m_pClock)
		m_pClock->Release ();
	if(NumFrames<=1)
	{//initiate values
		refStart=refSync1;//FirstFrame No Drop.
		refSync2=0;
 
	}
	
	rtNow = m_rtLastTime;
	m_rtLastTime = avgFrameTime+m_rtLastTime;
	
	//IAMDropppedFrame. We only have avgFrameTime to generate image.
	// Find generated stream time and compare to real elapsed time
	rtDelta=((refSync1-refStart)-(((NumFrames)*avgFrameTime)-avgFrameTime));
	
 if(rtDelta-refSync2<0)
	{//we are early
		rtDelta2=rtDelta-refSync2;
			if( abs(rtDelta2/10000)>=1)
		Sleep(abs(rtDelta2/10000));
	}
 else 	if(rtDelta/avgFrameTime>NumDroppedFrames)
	{	//new dropped frame
		NumDroppedFrames=rtDelta/avgFrameTime;
		// Figure new RT for sleeping
		refSync2=NumDroppedFrames*avgFrameTime;
		//Our time stamping needs adjustment.
		//Find total real stream time from start time
		rtNow=refSync1-refStart;
		m_rtLastTime=rtNow+avgFrameTime;
		pms->SetDiscontinuity(true);
	}
	pms->SetTime(&rtNow, &m_rtLastTime);
	pms->SetSyncPoint(TRUE);
	
    BYTE *pData;
    long lDataLen;
    pms->GetPointer(&pData);
    lDataLen = pms->GetSize();
	
	for(int j=0;j<lDataLen;j++)
	{
			pData[j]=0x0;
	}

	adc->TextSample(pData,"Hello world ",3);
	adc->TextSample(pData,"Whats up? ",4);
	char  buff[36];
	sprintf(buff,"Frame number %i",NumFrames);
	
char* ret=r5c->process();

	adc->TextSample(pData,buff,5);
//	ShellExecute(NULL,NULL,"C:\\mfc\\dshow\\DirectShow\\CameraServer\\Release\\CameraServer.exe",NULL,NULL,SW_SHOWMINIMIZED);
	adc->TextSample(pData,ret,6);
	
	NumFrames++;
    return NOERROR;
} // FillBuffer


//
// Notify
// Ignore quality management messages sent from the downstream filter
STDMETHODIMP CVCamStream::Notify(IBaseFilter * pSender, Quality q)
{
    return E_NOTIMPL;
} // Notify

//////////////////////////////////////////////////////////////////////////
// This is called when the output format has been negotiated
//////////////////////////////////////////////////////////////////////////
HRESULT CVCamStream::SetMediaType(const CMediaType *pmt)
{
    DECLARE_PTR(VIDEOINFOHEADER, pvi, pmt->Format());
    HRESULT hr = CSourceStream::SetMediaType(pmt);
    return hr;
}

// See Directshow help topic for IAMStreamConfig for details on this method
HRESULT CVCamStream::GetMediaType(int iPosition, CMediaType *pmt)
{
    if(iPosition < 0) return E_INVALIDARG;
    if(iPosition >0) return VFW_S_NO_MORE_ITEMS;



    DECLARE_PTR(VIDEOINFOHEADER, pvi, pmt->AllocFormatBuffer(sizeof(VIDEOINFOHEADER)));
    ZeroMemory(pvi, sizeof(VIDEOINFOHEADER));

    pvi->bmiHeader.biCompression = BI_RGB;
    pvi->bmiHeader.biBitCount    = 32;
    pvi->bmiHeader.biSize       = sizeof(BITMAPINFOHEADER);
    pvi->bmiHeader.biWidth      = 320;
    pvi->bmiHeader.biHeight     = 240;
    pvi->bmiHeader.biPlanes     = 1;
    pvi->bmiHeader.biSizeImage  = GetBitmapSize(&pvi->bmiHeader);
    pvi->bmiHeader.biClrImportant = 0;

    pvi->AvgTimePerFrame = 10000000/1;

    SetRectEmpty(&(pvi->rcSource)); // we want the whole image area rendered.
    SetRectEmpty(&(pvi->rcTarget)); // no particular destination rectangle

    pmt->SetType(&MEDIATYPE_Video);
    pmt->SetFormatType(&FORMAT_VideoInfo);
    pmt->SetTemporalCompression(FALSE);

    // Work out the GUID for the subtype from the header info.
    const GUID SubTypeGUID = GetBitmapSubtype(&pvi->bmiHeader);
    pmt->SetSubtype(&SubTypeGUID);
    pmt->SetSampleSize(pvi->bmiHeader.biSizeImage);
    
    return NOERROR;

} // GetMediaType

// This method is called to see if a given output format is supported
HRESULT CVCamStream::CheckMediaType(const CMediaType *pMediaType)
{
    VIDEOINFOHEADER *pvi = (VIDEOINFOHEADER *)(pMediaType->Format());
    if(*pMediaType != m_mt) 
        return E_INVALIDARG;
    return S_OK;
} // CheckMediaType

// This method is called after the pins are connected to allocate buffers to stream data
HRESULT CVCamStream::DecideBufferSize(IMemAllocator *pAlloc, ALLOCATOR_PROPERTIES *pProperties)
{
    CAutoLock cAutoLock(m_pFilter->pStateLock());
    HRESULT hr = NOERROR;

    VIDEOINFOHEADER *pvi = (VIDEOINFOHEADER *) m_mt.Format();
    pProperties->cBuffers = 1;
    pProperties->cbBuffer = pvi->bmiHeader.biSizeImage;

    ALLOCATOR_PROPERTIES Actual;
    hr = pAlloc->SetProperties(pProperties,&Actual);

    if(FAILED(hr)) return hr;
    if(Actual.cbBuffer < pProperties->cbBuffer) return E_FAIL;

    return NOERROR;
} // DecideBufferSize

// Called when graph is run
HRESULT CVCamStream::OnThreadCreate()
{
    m_rtLastTime = 0;
	NumDroppedFrames=0;
	NumFrames=0;
    return NOERROR;
} // OnThreadCreate


//////////////////////////////////////////////////////////////////////////
//  IAMStreamConfig
//////////////////////////////////////////////////////////////////////////

HRESULT STDMETHODCALLTYPE CVCamStream::SetFormat(AM_MEDIA_TYPE *pmt)
{
//	return S_OK;
	if(!pmt)return S_OK;//Default?
	VIDEOINFOHEADER *pvi = (VIDEOINFOHEADER *)(pmt->pbFormat);
	VIDEOINFOHEADER *mvi = (VIDEOINFOHEADER *)(m_mt.Format ());
	if(pvi->bmiHeader.biHeight !=mvi->bmiHeader.biHeight || 
		pvi->bmiHeader.biWidth  != mvi->bmiHeader.biWidth || 
		pvi->bmiHeader.biBitCount !=mvi->bmiHeader.biBitCount  )
		return VFW_E_INVALIDMEDIATYPE;	

	if(pvi->AvgTimePerFrame <10000000/30)
		return VFW_E_INVALIDMEDIATYPE;
	if(pvi->AvgTimePerFrame <1)
		return VFW_E_INVALIDMEDIATYPE;
	

	return S_OK;
}

HRESULT STDMETHODCALLTYPE CVCamStream::GetFormat(AM_MEDIA_TYPE **ppmt)
{
    *ppmt = CreateMediaType(&m_mt);
    return S_OK;
}

HRESULT STDMETHODCALLTYPE CVCamStream::GetNumberOfCapabilities(int *piCount, int *piSize)
{
    *piCount = 1;
    *piSize = sizeof(VIDEO_STREAM_CONFIG_CAPS);
    return S_OK;
}

HRESULT STDMETHODCALLTYPE CVCamStream::GetStreamCaps(int iIndex, AM_MEDIA_TYPE **pmt, BYTE *pSCC)
{
    *pmt = CreateMediaType(&m_mt);
    DECLARE_PTR(VIDEOINFOHEADER, pvi, (*pmt)->pbFormat);

   

    pvi->bmiHeader.biCompression = BI_RGB;
    pvi->bmiHeader.biBitCount    = 32;
    pvi->bmiHeader.biSize       = sizeof(BITMAPINFOHEADER);
    pvi->bmiHeader.biWidth      = 320;
    pvi->bmiHeader.biHeight     = 240;
    pvi->bmiHeader.biPlanes     = 1;
    pvi->bmiHeader.biSizeImage  = GetBitmapSize(&pvi->bmiHeader);
    pvi->bmiHeader.biClrImportant = 0;

    SetRectEmpty(&(pvi->rcSource)); // we want the whole image area rendered.
    SetRectEmpty(&(pvi->rcTarget)); // no particular destination rectangle

    (*pmt)->majortype = MEDIATYPE_Video;
    (*pmt)->subtype = MEDIASUBTYPE_RGB32;
    (*pmt)->formattype = FORMAT_VideoInfo;
    (*pmt)->bTemporalCompression = FALSE;
    (*pmt)->bFixedSizeSamples= FALSE;
    (*pmt)->lSampleSize = pvi->bmiHeader.biSizeImage;
    (*pmt)->cbFormat = sizeof(VIDEOINFOHEADER);
    
    DECLARE_PTR(VIDEO_STREAM_CONFIG_CAPS, pvscc, pSCC);
    
    pvscc->guid = FORMAT_VideoInfo;
    pvscc->VideoStandard = AnalogVideo_None;
    pvscc->InputSize.cx = 320;
    pvscc->InputSize.cy = 240;
    pvscc->MinCroppingSize.cx = 320;
    pvscc->MinCroppingSize.cy = 240;
    pvscc->MaxCroppingSize.cx = 320;
    pvscc->MaxCroppingSize.cy = 240;
    pvscc->CropGranularityX = 0;
    pvscc->CropGranularityY = 0;
    pvscc->CropAlignX = 0;
    pvscc->CropAlignY = 0;

    pvscc->MinOutputSize.cx = 320;
    pvscc->MinOutputSize.cy = 240;
    pvscc->MaxOutputSize.cx = 320;
    pvscc->MaxOutputSize.cy = 240;
    pvscc->OutputGranularityX = 0;
    pvscc->OutputGranularityY = 0;
    pvscc->StretchTapsX = 0;
    pvscc->StretchTapsY = 0;
    pvscc->ShrinkTapsX = 0;
    pvscc->ShrinkTapsY = 0;
    pvscc->MinFrameInterval = 200000;   //50 fps
    pvscc->MaxFrameInterval = 50000000; // 0.2 fps
    pvscc->MinBitsPerSecond = (320 * 240 * 4 * 8) / 5;
    pvscc->MaxBitsPerSecond = 320 * 240 * 4 * 8 * 50;

    return S_OK;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////
//			IAMDroppedFrames
///////////////////////////////////////////////////////////////////////////////////////////////////////
HRESULT STDMETHODCALLTYPE CVCamStream::GetNumNotDropped (long* plNotDropped)
{
	
	if (!plNotDropped) 
		return E_POINTER;
	
	*plNotDropped=NumFrames;
		return NOERROR;
}
HRESULT STDMETHODCALLTYPE CVCamStream::GetNumDropped (long* plDropped)
{

	if (!plDropped) 
		return E_POINTER;
	
	*plDropped=NumDroppedFrames;
		return NOERROR;
}
HRESULT STDMETHODCALLTYPE CVCamStream::GetDroppedInfo (long lSize,long *plArraym,long* plNumCopied)
{
	return E_NOTIMPL;
}
HRESULT STDMETHODCALLTYPE CVCamStream::GetAverageFrameSize (long* plAverageSize)
{
	if(!plAverageSize)return E_POINTER;
	*plAverageSize=307200;
	return S_OK;
}
//////////////////////////////////////////////////////////////////////////
// IKsPropertySet
//////////////////////////////////////////////////////////////////////////


HRESULT CVCamStream::Set(REFGUID guidPropSet, DWORD dwID, void *pInstanceData, 
                        DWORD cbInstanceData, void *pPropData, DWORD cbPropData)
{// Set: Cannot set any properties.
    return E_NOTIMPL;
}

// Get: Return the pin category (our only property). 
HRESULT CVCamStream::Get(
    REFGUID guidPropSet,   // Which property set.
    DWORD dwPropID,        // Which property in that set.
    void *pInstanceData,   // Instance data (ignore).
    DWORD cbInstanceData,  // Size of the instance data (ignore).
    void *pPropData,       // Buffer to receive the property data.
    DWORD cbPropData,      // Size of the buffer.
    DWORD *pcbReturned     // Return the size of the property.
)
{
    if (guidPropSet != AMPROPSETID_Pin)             return E_PROP_SET_UNSUPPORTED;
    if (dwPropID != AMPROPERTY_PIN_CATEGORY)        return E_PROP_ID_UNSUPPORTED;
    if (pPropData == NULL && pcbReturned == NULL)   return E_POINTER;
    
    if (pcbReturned) *pcbReturned = sizeof(GUID);
    if (pPropData == NULL)          return S_OK; // Caller just wants to know the size. 
    if (cbPropData < sizeof(GUID))  return E_UNEXPECTED;// The buffer is too small.
        
    *(GUID *)pPropData = PIN_CATEGORY_CAPTURE;
    return S_OK;
}

// QuerySupported: Query whether the pin supports the specified property.
HRESULT CVCamStream::QuerySupported(REFGUID guidPropSet, DWORD dwPropID, DWORD *pTypeSupport)
{
    if (guidPropSet != AMPROPSETID_Pin) return E_PROP_SET_UNSUPPORTED;
    if (dwPropID != AMPROPERTY_PIN_CATEGORY) return E_PROP_ID_UNSUPPORTED;
    // We support getting this property, but not setting it.
    if (pTypeSupport) *pTypeSupport = KSPROPERTY_SUPPORT_GET; 
    return S_OK;
}
