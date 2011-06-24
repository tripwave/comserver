package org.red5.server.plugin.shoutcast.parser;




/**
 * @author Andy Shaules (bowljoman@hotmail.com)
 *
 */
public class NSVFrame {

	public long frame_type = 0x8080;//empty	

	public long frame_number = 0;

	public int parser_info = 0;

	public String vid_type;

	public String aud_type;

	public int width = 0;

	public int height = 0;

	public double framerate = 0;

	public int frame_rate_encoded = 0x0;

	public int offset_current = 0;

	public int[] vid_data;

	public int[] aud_data;

	public int vid_len = 0;

	public int aud_len = 0;

	public int num_aux = 0;

	public long timeStamp = 0;

	public NSVFrame(NSVStreamConfig id, long type) {
		frame_type = type;
		aud_type = id.audio_format;
		vid_type = id.video_format;
		width = id.video_width;
		height = id.video_height;
		framerate = id.frame_rate;
		frame_rate_encoded = id.frame_rate_encoded;

	}

	/**
	 * For output back to shoutcast server. 
	 * 
	 */
	public int[] toBitStream() {
		int[] ret = new int[1];
		ret[0] = 0;
		int length = 0;
		NSVBitStream bs = new NSVBitStream();
		switch ((frame_type == NSVStream.NSV_SYNC_DWORD) ? 1 : 2) {
			case 1:
				length = (24) + (vid_len) + (aud_len);
				ret = new int[length];
				ret[0] = 'N';
				ret[1] = 'S';
				ret[2] = 'V';
				ret[3] = 's';
				ret[4] =  vid_type.charAt(0);
				ret[5] = vid_type.charAt(1);
				ret[6] =  vid_type.charAt(2);
				ret[7] =  vid_type.charAt(3);
				ret[8] =  aud_type.charAt(0);
				ret[9] =  aud_type.charAt(1);
				ret[10] = aud_type.charAt(2);
				ret[11] = aud_type.charAt(3);
				ret[12] = ((width ) & 0xff);
				ret[13] = ((width) >> 8) & 0xff ;
				ret[14] = ((height )  & 0xff);
				ret[15] = ((height) >> 8) & 0xff ;
				ret[16] = frame_rate_encoded;//frame rate
				ret[17] = ((offset_current )  & 0xff);
				ret[18] = ((offset_current) >> 8) & 0xff ;
				
				bs = new NSVBitStream();
				bs.putBits(4, num_aux);
				bs.putBits(20, vid_len);
				bs.putBits(16, aud_len);

				for (int i = 0; i < 5; i++) {
					ret[19 + i] = bs.getbits(8);
				}
				for (int i = 0; i < vid_len; i++) {
					ret[24 + i] = vid_data[i];
				}
				for (int i = 0; i < aud_len; i++) {
					ret[(24 + vid_len + i)] = aud_data[i];
				}

				break;
			case 2:
				length = (7) + (vid_len) + (aud_len);
				ret = new int[length];

				ret[0] = 0xef;
				ret[1] = 0xbe;
				bs = new NSVBitStream();

				bs.putBits(4, num_aux);
				bs.putBits(20, vid_len);
				bs.putBits(16, aud_len);

				for (int i = 0; i < 5; i++) {
					ret[2 + i] = bs.getbits(8);
				}
				for (int i = 0; i < vid_len; i++) {
					ret[7 + i] = vid_data[i];
				}
				for (int i = 0; i < aud_len; i++) {
					ret[(7 + vid_len + i)] = aud_data[i];
				}
				break;
		}
		return ret;
	}
	

}
