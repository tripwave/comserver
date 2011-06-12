

#include "stdafx.h"
#include "CAutoCap.h"

IPin *GetPin(IBaseFilter *pFilter, PIN_DIRECTION PinDir)
{
    BOOL       bFound = FALSE;
    IEnumPins  *pEnum;
    IPin       *pPin;

    HRESULT hr = pFilter->EnumPins(&pEnum);
    if (FAILED(hr))
    {
        return NULL;
    }
    while(pEnum->Next(1, &pPin, 0) == S_OK)
    {
        PIN_DIRECTION PinDirThis;
        pPin->QueryDirection(&PinDirThis);
        if (bFound = (PinDir == PinDirThis))
            break;
        pPin->Release();
    }
    pEnum->Release();
    return (bFound ? pPin : NULL);  
}


HRESULT CAutoCap::CaptureVideo(int index)
{
    HRESULT hr;
    IBaseFilter *pSrcFilter=NULL;
	// Get DirectShow interfaces
    hr = GetInterfaces();
 
    // Attach the filter graph to the capture graph
    hr = g_pCapture->SetFiltergraph(g_pGraph);
    if (FAILED(hr))
    {
        Msg(TEXT("Failed to set capture filter graph!  hr=0x%x"), hr);
        return hr;
    }

    // Use the system device enumerator and class enumerator to find
    // a video capture/preview device, such as a desktop USB video camera.
    hr = FindCaptureDevice(&pSrcFilter,index);//indexed
	if (FAILED(hr))
    {
       
        return hr;
    }
   
    // Add Capture filter to our graph.
    hr = g_pGraph->AddFilter(pSrcFilter, L"cam");
    if (FAILED(hr))
    {
        Msg(TEXT("Couldn't add A particular capture filter to the graph!  hr=0x%x\r\n\r\n") 
            TEXT("It must be in use.\r\n")
             , hr);
        pSrcFilter->Release();
        return hr;
    }


    // Add our graph to the running object table, which will allow
    // the GraphEdit application to "spy" on our graph
    hr = AddGraphToRot(g_pGraph, &g_dwGraphRegister);
    if (FAILED(hr))
    {
        Msg(TEXT("Failed to register filter graph with ROT!  hr=0x%x"), hr);
        g_dwGraphRegister = 0;
    }


	hr=g_pCapture->FindInterface(&PIN_CATEGORY_CAPTURE,&MEDIATYPE_Video,pSrcFilter, IID_IAMStreamConfig, (void**)&g_pscfg);	

	if (FAILED(hr))
    {
        Msg(TEXT("No IAMStreamConfig!  hr=0x%x"), hr);
      
    }

	AM_MEDIA_TYPE *pCapmt;
	hr=g_pscfg->GetFormat (&pCapmt);

// Modify the format block.
    VIDEOINFOHEADER *pVih = reinterpret_cast<VIDEOINFOHEADER*>(pCapmt->pbFormat);
    pVih->bmiHeader.biWidth = 320;
    pVih->bmiHeader.biHeight = 240;
	pVih->AvgTimePerFrame=1000000/15;
	pCapmt->subtype=MEDIASUBTYPE_IYUV;

	hr =g_pscfg->SetFormat (pCapmt);
	if (FAILED(hr))
    {
        Msg(TEXT("No MEDIASUBTYPE_IYUV or framerate!  hr=0x%x"), hr);
		return hr;
	}


	// Create the Cuda.
	IBaseFilter *pCompressorF=0 ;
	    
	hr = CoCreateInstance (CLSID_NVIDIA_VideoEncoderFilter , NULL, CLSCTX_INPROC,IID_IBaseFilter, (void **) &pCompressorF);
	if (FAILED(hr))
    {
        Msg(TEXT("No NVIDIA Video Encoder Filter!  hr=0x%x"), hr);
		return hr;
    }

	INVVESetting *pCompressor; 
	hr =pCompressorF->QueryInterface(IID_INVVESetting, (void**)&pCompressor);
	
	if (FAILED(hr))
    {
		Msg(TEXT("No NVIDIA INVVESetting Encoder settings!  hr=0x%x"), hr);
		return hr;
	}

	//NVVE_PRESETS
	long format=0;

	pCompressor->SetCodecType(NV_CODEC_TYPE_H264);
	pCompressor->SetParamValue(NVVE_PRESETS,&format);
	pCompressor->Release();

	hr = g_pGraph->AddFilter(pCompressorF,L"Video Encoder");


	// Create the Sample Grabber.
	hr = CoCreateInstance(CLSID_SampleGrabber, NULL, CLSCTX_INPROC_SERVER,
    IID_IBaseFilter, (void**)&pGrabberF);
	hr = g_pGraph->AddFilter(pGrabberF, L"Sample Grabber");
	pGrabberF->QueryInterface(IID_ISampleGrabber, (void**)&this->pGrabber);

	//Config
	AM_MEDIA_TYPE pmt;
	ZeroMemory(&pmt, sizeof(AM_MEDIA_TYPE));
	pmt.majortype = MEDIATYPE_Video;   
    pmt.formattype = FORMAT_VideoInfo;
	
	chanGrabber=new CChanGrabber();
	hr = pGrabber->SetMediaType(&pmt);
	hr=pGrabber->SetBufferSamples(false);

	((ISampleGrabber*)pGrabber)->SetCallback(chanGrabber,1);

	hr = g_pCapture->RenderStream (&PIN_CATEGORY_CAPTURE, &MEDIATYPE_Video,
                                  pSrcFilter, pCompressorF, pGrabberF);

	if (FAILED(hr))
    {	
		Msg(TEXT("RenderStream fail!  hr=0x%x"), hr);
		 return hr;
	}



// Null renderer
	IBaseFilter *pNullF = NULL;
	
	hr = CoCreateInstance(CLSID_NullRenderer, NULL, CLSCTX_INPROC_SERVER,
		IID_IBaseFilter, (void**)&pNullF);
	
	hr = g_pGraph->AddFilter(pNullF, L"nuller");
	
	pNullF->Release();

	IPin * pCompOut = NULL;

	pCompOut = GetPin(pGrabberF, PINDIR_OUTPUT);	
	
	hr = g_pGraph->Render(pCompOut);
	
	pCompOut->Release();




	if (FAILED(hr))
    {
        Msg(TEXT("Couldn't Render the pins.  hr=0x%x\r\n"), hr);
        pSrcFilter->Release();
        return hr;
    }

	
	// Now that the filter has been added to the graph and we have
    // rendered its stream, we can release this reference to the filter.
    pSrcFilter->Release();




    // Start previewing video data
    hr = g_pMC->Run();
    if (FAILED(hr))
    {
        Msg(TEXT("Couldn't run the graph!  hr=0x%x"), hr);
        return hr;
    }

    // Remember current state
    g_psCurrent = Running;
        
    return S_OK;
}
HRESULT CAutoCap::FindCaptureDevice(IBaseFilter ** ppSrcFilter, int iDeviceIndex)
{	
	HRESULT hr;
	IBaseFilter * pSrc = NULL;
	hr = 0;
	int index=0;
	ICreateDevEnum *pSysDevEnum = NULL;
hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER,
    IID_ICreateDevEnum, (void **)&pSysDevEnum);
if (FAILED(hr))
{	hr= 1;
    return hr;
}

// Obtain a class enumerator for the video compressor category.
IEnumMoniker *pEnumCat = NULL;
hr = pSysDevEnum->CreateClassEnumerator(CLSID_VideoInputDeviceCategory, &pEnumCat, 0);

if (hr == S_OK) 

{
	// Enumerate the monikers.
    IMoniker *pMoniker = NULL;
    ULONG cFetched;
    while(pEnumCat->Next(1, &pMoniker, &cFetched) == S_OK)
    {
        IPropertyBag *pPropBag;
        hr = pMoniker->BindToStorage(0, 0, IID_IPropertyBag, 
            (void **)&pPropBag);
        if (SUCCEEDED(hr))
        {	
			if (index==iDeviceIndex)
			{
            
				// To retrieve the filter's friendly name, do the following:
				VARIANT varName;
				
				VariantInit(&varName);
				hr = pPropBag->Read(L"FriendlyName", &varName, 0);
				if (SUCCEEDED(hr))
				{
					hr = pMoniker->BindToObject(NULL, NULL, IID_IBaseFilter,
					(void**)&pSrc);
					*ppSrcFilter = pSrc;
					VariantClear(&varName);
					pPropBag->Release();
					pMoniker->Release();
					return hr;
				}
				VariantClear(&varName);
			}
            index=index+1;
		}
	
	}
}

	return hr;

}
HRESULT CAutoCap::GetInterfaces(void)
{
    HRESULT hr;

    // Create the filter graph
    hr = CoCreateInstance (CLSID_FilterGraph, NULL, CLSCTX_INPROC,
                         IID_IGraphBuilder, (void **) &g_pGraph);
    if (FAILED(hr))
        return hr;

    // Create the capture graph builder
    hr = CoCreateInstance (CLSID_CaptureGraphBuilder2 , NULL, CLSCTX_INPROC,
                           IID_ICaptureGraphBuilder2, (void **) &g_pCapture);
    if (FAILED(hr))
        return hr;
    
    // Obtain interfaces for media control and Video Window
    hr = g_pGraph->QueryInterface(IID_IMediaControl,(LPVOID *) &g_pMC);
    if (FAILED(hr))
        return hr;

    return hr;
}


void CAutoCap::CloseInterfaces(void)
{

	

	this->chanGrabber->running=false;
    // Stop previewing data
    if (g_pMC)
        g_pMC->StopWhenReady();

    g_psCurrent = Stopped;

    // Stop receiving events
    if (g_pME)
        g_pME->SetNotifyWindow(NULL, WM_GRAPHNOTIFY, 0);

    // Relinquish ownership (IMPORTANT!) of the video window.
    // Failing to call put_Owner can lead to assert failures within
    // the video renderer, as it still assumes that it has a valid
    // parent window.
    if(g_pVW)
    {
        g_pVW->put_Visible(OAFALSE);
        g_pVW->put_Owner(NULL);
    }

#ifdef REGISTER_FILTERGRAPH
    // Remove filter graph from the running object table   
    if (g_dwGraphRegister)
        RemoveGraphFromRot(g_dwGraphRegister);
#endif

    // Release DirectShow interfaces
    SAFE_RELEASE(g_pMC);
    SAFE_RELEASE(g_pME);
    SAFE_RELEASE(g_pVW);
    SAFE_RELEASE(g_pGraph);
    SAFE_RELEASE(g_pCapture);
	SAFE_RELEASE(pGrabberF);
	SAFE_RELEASE(pGrabber);
	
	if(chanGrabber)
		delete chanGrabber;

}
HRESULT CAutoCap::SetupVideoWindow(void)
{
	return 0;
   
}


void CAutoCap::ResizeVideoWindow(void)
{
    // Resize the video preview window to match owner window size
    if (g_pVW)
    {
        RECT rc;
        
        // Make the preview video fill our window
        GetClientRect(ghApp, &rc);
        g_pVW->SetWindowPosition(10, 10, 320, 240);
    }
}


HRESULT CAutoCap::ChangePreviewState(int nShow)
{
    HRESULT hr=S_OK;
    
    // If the media control interface isn't ready, don't call it
    if (!g_pMC)
        return S_OK;
    
    if (nShow)
    {
        if (g_psCurrent != Running)
        {
            // Start previewing video data
            hr = g_pMC->Run();
            g_psCurrent = Running;
        }
    }
    else
    {
        // Stop previewing video data
        hr = g_pMC->StopWhenReady();
        g_psCurrent = Stopped;
    }

    return hr;
}
HRESULT CAutoCap::AddGraphToRot(IUnknown *pUnkGraph, DWORD *pdwRegister) 
{
    IMoniker * pMoniker;
    IRunningObjectTable *pROT;
    WCHAR wsz[128];
    HRESULT hr;

    if (!pUnkGraph || !pdwRegister)
        return E_POINTER;

    if (FAILED(GetRunningObjectTable(0, &pROT)))
        return E_FAIL;

    wsprintfW(wsz, L"FilterGraph %08x pid %08x\0", (DWORD_PTR)pUnkGraph, 
              GetCurrentProcessId());

    hr = CreateItemMoniker(L"!", wsz, &pMoniker);
    if (SUCCEEDED(hr)) 
    {
        hr = pROT->Register(ROTFLAGS_REGISTRATIONKEEPSALIVE, pUnkGraph, 
                            pMoniker, pdwRegister);
        pMoniker->Release();
    }

    pROT->Release();
    return hr;
}


// Removes a filter graph from the Running Object Table
void CAutoCap::RemoveGraphFromRot(DWORD pdwRegister)
{
    IRunningObjectTable *pROT;

    if (SUCCEEDED(GetRunningObjectTable(0, &pROT))) 
    {
        pROT->Revoke(pdwRegister);
        pROT->Release();
    }
}
void CAutoCap::Msg(TCHAR *szFormat, ...)
{
    TCHAR szBuffer[1024];  // Large buffer for long filenames or URLs
    const size_t NUMCHARS = sizeof(szBuffer) / sizeof(szBuffer[0]);
    const int LASTCHAR = NUMCHARS - 1;

    // Format the input string
    va_list pArgs;
    va_start(pArgs, szFormat);

    // Use a bounded buffer size to prevent buffer overruns.  Limit count to
    // character size minus one to allow for a NULL terminating character.
    _vsntprintf(szBuffer, NUMCHARS - 1, szFormat, pArgs);
    va_end(pArgs);

    // Ensure that the formatted string is NULL-terminated
    szBuffer[LASTCHAR] = TEXT('\0');

    MessageBox(NULL, szBuffer, TEXT("PlayCap Message"), MB_OK | MB_ICONERROR);
}


HRESULT CAutoCap::HandleGraphEvent(void)
{
    
	
	LONG evCode, evParam1, evParam2;
    HRESULT hr=S_OK;

    if (!g_pME)
        return E_POINTER;

    while(SUCCEEDED(g_pME->GetEvent(&evCode, (LONG_PTR *) &evParam1, 
                   (LONG_PTR *) &evParam2, 0)))
    {
        //
        // Free event parameters to prevent memory leaks associated with
        // event parameter data.  While this application is not interested
        // in the received events, applications should always process them.
        //
        hr = g_pME->FreeEventParams(evCode, evParam1, evParam2);
        
        // Insert event processing code here, if desired
    }

    return hr;
}

main()
{
	//Test
	CoInitialize(NULL);
	CAutoCap * capper= new CAutoCap();
	
	capper->CaptureVideo(0);
	
	for(;;){
		Sleep(10 );
	}

	delete capper;

}
