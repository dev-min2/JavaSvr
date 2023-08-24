package SockNet;

import java.nio.ByteBuffer;

public class RingBuffer {	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 10; 

	private ByteBuffer byteBuffer;
	private int remainLen = 0;
	
	public RingBuffer()
	{
		byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
		//buffer  = new byte[DEFAULT_BUFFER_SIZE];
	}
	
	public RingBuffer(final int bufferSize)
	{
		byteBuffer = ByteBuffer.allocate(bufferSize);
	}
	
	public ByteBuffer getBuffer()
	{
		return byteBuffer.slice();
	}

	public void readBuffer(int readDataSize)
	{
		byteBuffer.get(new byte[readDataSize]);
	}
	
	public void clean()
	{
		byteBuffer.clear();
	}
	public void compact()
	{
		byteBuffer.compact();
	}
	
	public int getPosition()
	{
		return byteBuffer.position();
	}
	
	public void setPosition(int newPos)
	{
		byteBuffer.position(newPos);
	}
	
	public void setRemainLen(int remainLen)
	{
		this.remainLen = remainLen;
	}
	
	public int getRemainLen()
	{
		return remainLen;
	}
}
