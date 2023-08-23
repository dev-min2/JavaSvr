package SockNet;

import java.nio.ByteBuffer;

public class RingBuffer {	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 10; 
	
	private int readPos = 0;
	private int writePos = 0;
	private int offset = 0; // 현재 버퍼안의 데이터 시작위치
	private byte[] buffer;
	private ByteBuffer byteBuffer;
	private ByteBuffer btMark = null;
	
	public RingBuffer()
	{
		byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
		//buffer  = new byte[DEFAULT_BUFFER_SIZE];
	}
	
	public RingBuffer(final int bufferSize)
	{
		byteBuffer = ByteBuffer.allocate(bufferSize);
		//buffer = new byte[bufferSize];
	}
	
	public int getCurrentBufferDataSize()
	{
		return writePos - readPos;
	}
	
	public int getBufferFreeSize()
	{
		return buffer.length - writePos;
	}
	
	public ByteBuffer getBuffer()
	{
		return byteBuffer.slice();
	}
	
	public boolean writeBuffer(byte[] dataBuffer)
	{
		byteBuffer.put(dataBuffer);
		
		//System.arraycopy(dataBuffer, 0, buffer, offset + writePos, dataBuffer.length);
		//writePos += dataBuffer.length;
		return true;
	}
	
	public boolean readBuffer(int readDataSize)
	{
		byteBuffer.get(new byte[readDataSize]);
		return true;
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
	
	public void mark()
	{
		btMark = byteBuffer.mark();
	}
	
	public ByteBuffer checkMarkAndGetPosition()
	{
		ByteBuffer ret = null;
		if(btMark != null)
		{
			ret = btMark;
			btMark = null;
		}
		return ret;
	}
	
	public boolean hasRemanining()
	{
		return byteBuffer.hasRemaining();
	}
	
	public void setPosition(int newPosition)
	{
		byteBuffer.position(newPosition);
	}
}
