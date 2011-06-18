
#include "stdafx.h"
#pragma once

int nextId=1;




class ICallbackCapable
{
private:
	int id;
public :

	ICallbackCapable()
	{
		id=getCallbackCapableId();
	}

	virtual int processCall(char * data,char * results ) 
	{

		sprintf(results,"ICallbackCapable",16);
		int i=strlen(results);
		return i;
	}
	
	void setId(int i)
	{
		id=i;
	}

	int getId()
	{
		return id;
	}

private:

	static int getCallbackCapableId()
	{
		printf("getCallbackCapableId %d\r\n",nextId);
		return nextId++;
	}
};
