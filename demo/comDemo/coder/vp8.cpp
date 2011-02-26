
#include "stdafx.h"

vpx_codec_enc_cfg_t config;
vpx_rational rat;
vpx_codec_ctx_t      cod;
int prfl;

HINSTANCE g_hinst;

BOOL WINAPI DllMain(
  HINSTANCE hinstDLL, 
  DWORD fdwReason,     
  LPVOID lpvReserved  
)
{
  g_hinst=hinstDLL;
  
  if (fdwReason == DLL_PROCESS_ATTACH ) 
  {	  
	  
	int ret= vpx_codec_enc_config_default(interface, &config, 0);
	if(ret)
	{
		MessageBox(NULL,"DllMain vpx_codec_enc_config_default","Error",0);
	}


	ret=vpx_codec_enc_init(&cod, interface, &config, 0)  ;

	if(ret) 
	{
		MessageBox(NULL,"dll Error calling vpx_codec_eec_init","Error",0);

	}
	prfl=VPX_DL_REALTIME;
	config.g_threads=1;
	config.kf_max_dist=300;
	  return TRUE;
  }
  return FALSE;
}




class VideoCoderVp8 : public VideoCoder
{
public:
	vp8e_encoding_mode mode;
	vpx_codec_ctx_t      codec;
	vpx_codec_enc_cfg_t  cfg;
	int                  frame_cnt;
	vpx_image_t          raw;
	vpx_codec_err_t      res;
	int profl				;
	int                  frame_avail;
	int                  got_data;
	int                  flags;

	VideoCoderVp8()
	{
		
	}

	int initiate(int w, int h, vpx_rational rate, int threads, int bitrate, int minkf, int maxkf, int profile, vpx_rc_mode usage, int overShoot, int underShoot)
	{
		profl=profile;
		flags=0;
		got_data=0;
		frame_cnt=0;

		if(w % 16 || h % 16)
		{
			MessageBox(NULL,"Sizes must be multiples of 16","Error",0);
			return EXIT_FAILURE;
		}


		
		
		if(!vpx_img_alloc(&raw, VPX_IMG_FMT_YV12, w, h, 1))
		{
			MessageBox(NULL,"Error calling vpx_img_alloc","Error",0);
			return EXIT_FAILURE;		
		
		}

		res = vpx_codec_enc_config_default(interface, &cfg, 0);

		
		if(res) 
		{
			MessageBox(NULL,"Error calling vpx_codec_enc_config_default","Error",0);
			return EXIT_FAILURE;
		}

	  cfg.g_timebase=rate;
	  
	  cfg.g_h=h;
	  cfg.g_w=w;

	  cfg.g_threads=threads;
	
	  cfg.g_pass = VPX_RC_ONE_PASS;

	  //cfg.rc_end_usage= VPX_VBR;//VPX_CBR
	  
	  cfg.rc_end_usage=usage; 

	  cfg.kf_mode=VPX_KF_AUTO;
	  cfg.kf_max_dist=maxkf;
	  cfg.kf_min_dist=minkf;
	  cfg.rc_target_bitrate=bitrate;
	  cfg.rc_undershoot_pct=underShoot;
	  cfg.rc_overshoot_pct=overShoot;

		res= vpx_codec_enc_init(&codec, interface, &cfg, 0);
		
		switch(res) 
		{
			//VPX_CODEC_OK,VPX_CODEC_INVALID_PARAM ,VPX_CODEC_INCAPABLE 
			case VPX_CODEC_OK:	
			return 0;
			
			case VPX_CODEC_INVALID_PARAM:
			MessageBox(NULL,"Invalid Parameter","Error",0);			
			return EXIT_FAILURE;
			
			case VPX_CODEC_INCAPABLE:
			MessageBox(NULL,"Invalid Capability","Error",0);				
			return EXIT_FAILURE;	
								
			
		}
		return 0;
	}

	int Encode(void *in, void *out, int *is_kf)
	{
		if(res)
			return 0;

		int ret=0;
		
		if(*is_kf)
			flags |= VPX_EFLAG_FORCE_KF;
		else
			flags &= ~VPX_EFLAG_FORCE_KF;

		*is_kf=0;
		
		vpx_codec_iter_t iter = NULL;
		
		const vpx_codec_cx_pkt_t *pkt;
		
		int sze=cfg.g_w*cfg.g_h*3/2;
		//codec requires yuv420. enc_if currently defines argb input.
		RGBtoYUV420PSameSize((const unsigned char *)in,raw.img_data,4,0,cfg.g_w,cfg.g_h);
		
		// VPX_DL_REALTIME , VPX_DL_GOOD_QUALITY, VPX_DL_BEST_QUALITY.

		res= vpx_codec_encode(&codec,  &raw , frame_cnt++, 1, flags, profl);
			
		if( res ) 
		{	
			MessageBox(NULL,"Error calling vpx_codec_encode","Error",0);
			return EXIT_FAILURE;
		}
		
			while( (pkt = vpx_codec_get_cx_data(&codec, &iter)) ) 
			{	
				
				switch(pkt->kind) 
				{
					case VPX_CODEC_CX_FRAME_PKT:                                 
						memcpy((char *)out + ret, pkt->data.frame.buf, pkt->data.frame.sz);
						ret += pkt->data.frame.sz;

						break;

					default:
						break;
				}

				if (pkt->kind == VPX_CODEC_CX_FRAME_PKT && pkt->data.frame.flags & VPX_FRAME_IS_KEY)
				{	
					*is_kf=1;
				}

			}

		return ret;
	}

	~VideoCoderVp8()
	{
	}
private:

	void RGBtoYUV420PSameSize (const unsigned char * rgb,
		unsigned char * yuv,
		unsigned rgbIncrement,
		unsigned char flip,
		int srcFrameWidth, int srcFrameHeight)
	{
		unsigned int planeSize;
		unsigned int halfWidth;

		unsigned char * yplane;
		unsigned char * uplane;
		unsigned char * vplane;
		const unsigned char * rgbIndex;

		int x, y;
		unsigned char * yline;
		unsigned char * uline;
		unsigned char * vline;

		planeSize = srcFrameWidth * srcFrameHeight;
		halfWidth = srcFrameWidth >> 1;

		// get pointers to the data
		yplane = yuv;
		uplane = yuv + planeSize;
		vplane = yuv + planeSize + (planeSize >> 2);
		rgbIndex = rgb;

		for (y = 0; y < srcFrameHeight; y++)
		{
			yline = yplane + (y * srcFrameWidth);
			uline = uplane + ((y >> 1) * halfWidth);
			vline = vplane + ((y >> 1) * halfWidth);

			if (flip)
				rgbIndex = rgb + (srcFrameWidth*(srcFrameHeight-1-y)*rgbIncrement);

			for (x = 0; x < (int) srcFrameWidth; x+=2)
			{
				rgbtoyuv(rgbIndex[2], rgbIndex[1], rgbIndex[0], *yline, *uline, *vline);
				rgbIndex += rgbIncrement;
				yline++;
				rgbtoyuv(rgbIndex[2], rgbIndex[1], rgbIndex[0], *yline, *uline, *vline);
				rgbIndex += rgbIncrement;
				yline++;
				uline++;
				vline++;
			}
		}
	}
};



extern "C" {

	unsigned int __declspec(dllexport) GetVideoTypes3(int idx, char *desc)
	{
		if (idx==0)
		{
			sprintf(desc,cod.name);
			
			return fourcc;
		}
		return 0;
	}

	VideoCoder __declspec(dllexport)  *CreateVideo3(int w, int h, double frt, unsigned int pixt, unsigned int *outt, char *configfile)
	{
		
			
		
		VideoCoderVp8 *vidCoder=new VideoCoderVp8();
			
			
			vpx_rational rt;

			rt.num=1;
			rt.den=30;


			if(vidCoder->initiate(w,h,rt,1,config.rc_target_bitrate,config.kf_min_dist,config.kf_max_dist,prfl,config.rc_end_usage,config.rc_overshoot_pct,config.rc_undershoot_pct) == EXIT_FAILURE)
			{
				delete vidCoder;
				return NULL;
			}
			
			return vidCoder;
		

	}
	BOOL CALLBACK DlgProc(HWND hwndDlg, UINT uMsg, WPARAM wParam,LPARAM lParam) 
	{
		int t=config.rc_target_bitrate;
		int hr=0;
		
		char uBuf[6];
		if (uMsg == WM_INITDIALOG)
		{ 
			SetWindowLong(hwndDlg,GWL_USERDATA,lParam);
			
			vpx_codec_enc_cfg_t *wc;
			if (lParam)
			{
			  wc=(vpx_codec_enc_cfg_t*)lParam;
			}
			
			memset(uBuf,0,6);
			
			char* old=itoa(t,uBuf,10);

			::SetDlgItemText(hwndDlg,IDC_EDIT1,uBuf );
			::SetDlgItemText(hwndDlg,IDC_EDIT2, "Bitrate");
			::SetDlgItemText(hwndDlg,IDC_EDIT3, "kb/s");
			itoa(config.rc_undershoot_pct,uBuf,10);
			::SetDlgItemText(hwndDlg,IDC_EDIT4,uBuf );
			 itoa(config.rc_overshoot_pct,uBuf,10);
			::SetDlgItemText(hwndDlg,IDC_EDIT5,uBuf);
			
			

			::SendDlgItemMessage(hwndDlg,IDC_COMBO1,CB_ADDSTRING,0,(LPARAM) "Real time" );
			::SendDlgItemMessage(hwndDlg,IDC_COMBO1,CB_ADDSTRING,0,(LPARAM) "Good" );
			::SendDlgItemMessage(hwndDlg,IDC_COMBO1,CB_ADDSTRING,0,(LPARAM) "Best" );

			switch(prfl)
			{
			case VPX_DL_REALTIME:
				::SendDlgItemMessage(hwndDlg,IDC_COMBO1,CB_SETCURSEL,(WPARAM )0,(LPARAM)0 );
				break;
			case VPX_DL_GOOD_QUALITY:
				::SendDlgItemMessage(hwndDlg,IDC_COMBO1,CB_SETCURSEL,(WPARAM )1,(LPARAM)0 );
				break;
			case VPX_DL_BEST_QUALITY:
				::SendDlgItemMessage(hwndDlg,IDC_COMBO1,CB_SETCURSEL,(WPARAM )2,(LPARAM)0 );
				break;
			}
			::SendDlgItemMessage(hwndDlg,IDC_COMBO2,CB_ADDSTRING,0,(LPARAM) "CBR" );	
			::SendDlgItemMessage(hwndDlg,IDC_COMBO2,CB_ADDSTRING,0,(LPARAM) "VBR" );
			switch(config.rc_end_usage)
			{
			case VPX_VBR:
				::SendDlgItemMessage(hwndDlg,IDC_COMBO2,CB_SETCURSEL,(WPARAM )1,(LPARAM)0 );
				break;
			case VPX_CBR:
				::SendDlgItemMessage(hwndDlg,IDC_COMBO2,CB_SETCURSEL,(WPARAM )0,(LPARAM)0 );
				break;
			}

			
			
			::SetDlgItemText(hwndDlg,IDC_EDIT7,  itoa(config.kf_min_dist,uBuf,10));
			::SetDlgItemText(hwndDlg,IDC_EDIT8, itoa(config.kf_max_dist,uBuf,10));

			::SetDlgItemText(hwndDlg,IDC_EDIT9, "Thread count");
			::SetDlgItemText(hwndDlg,IDC_EDIT10, itoa(config.g_threads,uBuf,10));

			::SetDlgItemText(hwndDlg,IDC_EDIT11, "Thanks WebM Project !");
		}
		if (uMsg == WM_COMMAND)
		{
			LRESULT isChecked=0;
			LRESULT isChecked2=0;

			if (LOWORD(wParam) == IDC_CHECK1)
			{	//auto



				HWND han= ::GetDlgItem(hwndDlg,IDC_CHECK1);
				isChecked=::SendMessage(han,BM_GETCHECK,(WPARAM )0,0 );
				

				
				if(isChecked)
				{
					

					::GetDlgItemText(hwndDlg,IDC_EDIT8, uBuf,6);
					int cnt=atoi(uBuf);
					if(!cnt)
					{
						::SetDlgItemText(hwndDlg,IDC_EDIT7, "1");
						::SetDlgItemText(hwndDlg,IDC_EDIT8, "1");
						return FALSE;
					}
					
					::SetDlgItemText(hwndDlg,IDC_EDIT7, uBuf);
				}
				else
				{
					::SetDlgItemText(hwndDlg,IDC_EDIT7, "0");
				}
			
			}
			if (LOWORD(wParam) == IDC_EDIT8)
			{

				HWND han3= ::GetDlgItem(hwndDlg,IDC_CHECK1);
				
				int isChecked3=::SendMessage(han3,BM_GETCHECK,(WPARAM )0,0 );

				if(isChecked3)
				{
					::GetDlgItemText(hwndDlg,IDC_EDIT8, uBuf,6);
					::SetDlgItemText(hwndDlg,IDC_EDIT7, uBuf);
				}
			
			}
		}

		if (uMsg == WM_DESTROY)
		{
			char brBuf[6];
			//BR
			::GetDlgItemText(hwndDlg,IDC_EDIT1, brBuf,6);
			config.rc_target_bitrate= atoi(brBuf);
			::GetDlgItemText(hwndDlg,IDC_EDIT4, brBuf,6);
			config.rc_undershoot_pct = atoi(brBuf);
			::GetDlgItemText(hwndDlg,IDC_EDIT5, brBuf,6);
			config.rc_overshoot_pct = atoi(brBuf);

			
			
			//Threads
			::GetDlgItemText(hwndDlg,IDC_EDIT10, brBuf,6);
			config.g_threads = atoi(brBuf);
			
			HWND handle= ::GetDlgItem(hwndDlg,IDC_COMBO2);
			LRESULT index =::SendMessage(handle,CB_GETCURSEL,(WPARAM )0,0 );
			config.rc_end_usage=(index)?VPX_VBR:VPX_CBR;




			handle= ::GetDlgItem(hwndDlg,IDC_COMBO1);
			index =::SendMessage(handle,CB_GETCURSEL,(WPARAM )0,0 );
			// VPX_DL_REALTIME , VPX_DL_GOOD_QUALITY, VPX_DL_BEST_QUALITY.
			prfl=(index == 0 )? VPX_DL_REALTIME :(index == 1 )? VPX_DL_GOOD_QUALITY : VPX_DL_BEST_QUALITY; 

			
			
			//KF
			::GetDlgItemText(hwndDlg,IDC_EDIT7, brBuf,6);
			config.kf_min_dist= atoi(brBuf);
			
			::GetDlgItemText(hwndDlg,IDC_EDIT8, brBuf,6);
			config.kf_max_dist= atoi(brBuf);
			
	
			 vpx_codec_enc_cfg_t *kc=(vpx_codec_enc_cfg_t*)SetWindowLong(hwndDlg,GWL_USERDATA,0);
			 if(kc){

			 }
		}


		return FALSE;
	}
	HWND __declspec(dllexport) ConfigVideo3(HWND hwndParent, HINSTANCE hinst, unsigned int outt, char *configfile)
	{
		if(outt == fourcc){
			 return CreateDialogParam(hinst,MAKEINTRESOURCE(IDD_DIALOG1),hwndParent,DlgProc,(LPARAM)&config);
		}
		return NULL;
	}
};

