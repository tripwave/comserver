package org.red5.server.plugin.shoutcast.parser;


import java.util.ArrayList;

/**
 * NSV constants and utility functions
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVStream {

	public static long NSV_MAX_AUDIO_LEN = 0x8000; // 32kb

	public static long NSV_MAX_VIDEO_LEN = 0x80000;// 512kb

	public static long NSV_MAX_AUX_LEN = 0x8000; // 32kb for each aux stream

	public static long NSV_MAX_AUXSTREAMS = 15; // 15 aux streams maximum

	public static long NSV_SYNC_HEADERLEN_BITS = 192;

	public static long NSV_NONSYNC_HEADERLEN_BITS = 56;

	public static long NSV_NONSYNC_WORD = 0xBEEF;

	public static long NSV_SYNC_DWORD = (makeType('N', 'S', 'V', 's'));

	public static long NSV_HDR_DWORD = (makeType('N', 'S', 'V', 'f'));

	private static int streamIds = 0;

	public static ArrayList<NSVStreamConfig> streams = new ArrayList<NSVStreamConfig>();

	public static double framerateToDouble(int fr) {
		double ret = 0;
		if ((fr & 0x80) == 0)
			return fr;
		double sc = 0;
		int d = fr & 0x7f >> 2;
		if (d < 16)
			sc = 1.0 / (d + 1);
		else
			sc = d - 15;
		int r = fr & 3;
		switch (r) {
			case 0:
				ret = 30.0 * sc;
				break;
			case 1:
				ret = 30.0 * 1000.0 / 1001.0 * sc;
				break;
			case 2:
				ret = 25.0 * sc;
				break;
			case 3:
				ret = 24.0 * 1000.0 / 1001.0 * sc;
				break;
		}
		return ret;
	}

	public static NSVStreamConfig create(String p_Vidtype, String p_Audtype, int p_width, int p_height,
			double p_framerate) {

		NSVStreamConfig newConfig = new NSVStreamConfig();
		newConfig.streamId = ++streamIds;
		newConfig.video_format = p_Vidtype;
		newConfig.audio_format = p_Audtype;
		newConfig.video_width = p_width;
		newConfig.video_height = p_height;
		newConfig.frame_rate = p_framerate;
		streams.add(newConfig);
		return newConfig;
	}

	public static long makeType(char A, char B, char C, char D) {
		return ((A) | ((B) << 8) | ((C) << 16) | ((D) << 24));
	}

}
