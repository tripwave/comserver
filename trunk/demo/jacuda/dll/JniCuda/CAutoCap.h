
#include <atlbase.h>
#include <windows.h>
#include <dshow.h>
#include <stdio.h>
#include <atldebugapi.h>
#include "qedit.h"
#include "jni.h"
//#include "streams.h"


class CChanGrabber : public ISampleGrabberCB
{
public:
	//CCritSec m_cSharedState;
	double currentTime;
	bool hasSample;
	bool running;
	bool clearedSample;
	BYTE * buffer;
	long len;
	CChanGrabber()
	{	
		len=0;
		running=true;
		currentTime=0;
		buffer=0;
		hasSample=false;
		clearedSample=false;
	}

	
	
	STDMETHODIMP_(ULONG) AddRef()  { return 2; }

    STDMETHODIMP_(ULONG) Release() { return 1; }
   
	STDMETHODIMP QueryInterface(REFIID riid, void ** ppv)
    {        
        if (riid == IID_ISampleGrabberCB || riid == IID_IUnknown) 
        {
            *ppv = (void *) static_cast<ISampleGrabberCB *>(this);
            return NOERROR;
        }    
        return E_NOINTERFACE;
    }

    STDMETHODIMP SampleCB( double SampleTime, IMediaSample * pSample )
    {		
		return 0;
    }

    STDMETHODIMP BufferCB( double SampleTime, BYTE * pBuffer, long BufferLen )
    {	

		
		
		currentTime=SampleTime;
		
		len=BufferLen;
		
		buffer=pBuffer;
		
		clearedSample=false;
		
		hasSample=true;

		while(!clearedSample && running)
		{
			Sleep(1);
		}

		hasSample=false;
		clearedSample=false;
		return 0;
	}

	~CChanGrabber(void)
	{
	}
};











class CAutoCap
{
public:
	enum PLAYSTATE {Stopped, Paused, Running, Init};
	HRESULT GetInterfaces(void);
	HRESULT CaptureVideo(int index);
	HRESULT FindCaptureDevice(IBaseFilter ** ppSrcFilter, int index);
	HRESULT SetupVideoWindow(void);
	HRESULT ChangePreviewState(int nShow);
	HRESULT HandleGraphEvent(void);
	HRESULT HandleSampleEvent(void);
	void Msg(TCHAR *szFormat, ...);
	void CloseInterfaces(void);
	void ResizeVideoWindow(void);
	HRESULT AddGraphToRot(IUnknown *pUnkGraph, DWORD *pdwRegister);
	void RemoveGraphFromRot(DWORD pdwRegister);
	//////////////////////Data///////////////
	HWND ghApp;
	DWORD g_dwGraphRegister;
	IBaseFilter *pGrabberF ;
	ISampleGrabber *pGrabber;
	IVideoWindow  * g_pVW ;
	IMediaControl * g_pMC ;
	IMediaEventEx * g_pME ;
	IGraphBuilder * g_pGraph;
	IAMStreamConfig * g_pscfg;
	ICaptureGraphBuilder2 * g_pCapture ;
	PLAYSTATE g_psCurrent ;
	int width;
	int height;
	CChanGrabber * chanGrabber;
	bool attached;



};