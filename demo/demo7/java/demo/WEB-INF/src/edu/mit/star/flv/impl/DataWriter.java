package edu.mit.star.flv.impl;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

public interface DataWriter
{
	public void write(IoBuffer out,boolean isKey) throws IOException;
}
