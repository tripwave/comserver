#pragma once


#define VIDEOFRAMESIZE 16384
#define VIDEOFRAMESIZE_DV 20480

class VideoFrame
{
    	
public:
char data[VIDEOFRAMESIZE];
double time;
int size;
int isKey;

	VideoFrame()
	{
		reset();
	}
	void copy(VideoFrame  frame)
	{
		reset();

		if(frame.size< 0 || frame.size > VIDEOFRAMESIZE)
		{
			return;
		}
		this->isKey=frame.isKey;
		this->size=frame.size;
		this->time=frame.time;
		memcpy(this->data,frame.data,frame.size);
	}
	void copy(VideoFrame * frame)
	{
		reset();

		if(frame->size< 0 || frame->size > VIDEOFRAMESIZE)
		{
			return;
		}
		this->isKey=frame->isKey;
		this->size=frame->size;
		this->time=frame->time;
		memcpy(this->data,frame->data,frame->size);
	}
	void copy(void * buff, int size)
	{
		reset();
		if(size<0 || size > 16384)
		{
			return;
		}
		this->size=size;
		memcpy(this->data,buff,size);
	}
	void copy(void * buff, int size,double timeStamp)
	{
		reset();
		if(size<0 || size > VIDEOFRAMESIZE)
		{
			return;
		}
		this->time=timeStamp;
		this->size=size;
		memcpy(this->data,buff,size);
	}
	void copy(void * buff, int size,double timeStamp, int isKey)
	{
		reset();
		if(size<0 || size > VIDEOFRAMESIZE)
		{
			return;
		}
		this->time=timeStamp;
		this->size=size;
		this->isKey=isKey;
		memcpy(this->data,buff,size);
	}
	void reset()
	{
		this->isKey=0;
		this->size=0;
		this->time=0;
		memset(this->data,0,VIDEOFRAMESIZE);
	}

	~VideoFrame(void)
	{
		memset(this->data,0,VIDEOFRAMESIZE);
	}
};


class VideoFrameBuffer
{
    	
public:
	VideoFrame frames[72];

	int limit;
	int numFrames;
	int current;
	int currentRead;	
	int writingTo;	
	double startTime;
	double duration;

	VideoFrameBuffer(void)
	{	
		limit=31;
		reset();
	}
	VideoFrame * getCurrent()
	{
		return &frames[writingTo];
	}
void pushBackReadable()
{
	currentRead--;
}
	VideoFrame * peekNextReadable()
	{

		if(currentRead >= numFrames )
		return NULL;
		
		return &frames[currentRead];
	}

	VideoFrame * getNextReadable()
	{

		if(currentRead >= numFrames )
		return NULL;
		
		return &frames[currentRead++];
	}

	VideoFrame * getNextWritable()
	{

		if(current >= limit )
		return NULL;
		
		numFrames++;

		writingTo=current;	

		return &frames[current++];
	}

	int findLowestKey()
	{
		int lowKey=0;
		for(int p=0; p< numFrames; p++)
		{
			if(frames[p].isKey==1)
				return p;
			
		}
		return -1;
	}
	void shrink()
	{
		if(currentRead<1)
			return;

		int numToSave=current-currentRead;
		

        
		for(int p=0; p< numToSave; p++)
		{
			frames[p].copy( frames[ p+currentRead] );
		}

		 
		current -= currentRead;
		currentRead=0;
		writingTo=current-1;
		numFrames=numToSave;
		startTime=frames[0].time;
	
	}


	void reset()
	{
		
		currentRead=0;
		current=0;
		duration=0;
		startTime=0;
		writingTo=0;
		numFrames=0;
	}
	void flip()
	{
		currentRead=0;
	}
	int getNumFrames()
	{
		return numFrames;
	}
	~VideoFrameBuffer(void)
	{
	}

	

};


class VideoBufferChunks
{
public:
	VideoFrameBuffer buffers[4];
	int current;
	int currentRead;

int getCurrentWriteIndex()
{
	int ret=current-1;
	
	ret=ret<0?3:ret;

	return ret;

}
VideoFrameBuffer * getNextWritable()
{

	if(current>3)
		current=0;

	return &buffers[current++];
}
VideoFrameBuffer * getNextReadable(int fromIndex)
{
	currentRead= fromIndex;
	
	if(currentRead>3)
		currentRead=0;

	return &buffers[currentRead++];
}
VideoFrameBuffer * getNextReadable()
{

	if(currentRead>3)
		currentRead=0;

	return &buffers[currentRead++];
}
int highest(double * time)
{
	int ret=0;
	double retTime=0;
	for(int y=0; y<4 ;y++)
	{
		
		
			if(buffers[y].startTime > retTime )
			{
				*time=buffers[y].startTime;
				retTime=buffers[y].startTime;
				ret=y;
			}
		
	}
	return ret;
}
int lowest(double * time)
{
	int ret=0;
	double high=-1;
	//ret=highest(&high);
	
	for(int y=0;y<4;y++){
		
		if(high ==-1)
		{
				*time=buffers[y].startTime;
				high=buffers[y].startTime;
				ret=y;
		}
		else 
			if(buffers[y].startTime < high )
			{
				*time=buffers[y].startTime;
				high=buffers[y].startTime;
				ret=y;
			}
		
	}
	return ret;
}


};



