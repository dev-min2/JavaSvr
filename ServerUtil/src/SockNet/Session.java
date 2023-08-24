package SockNet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.*;

import PacketUtils.Packet;
import PacketUtils.PacketUtil;

public class Session {
	
	private int sessionId = 0;
	private AsynchronousSocketChannel socketChannel;
	private RingBuffer recvBuffer = new RingBuffer();
	private RingBuffer sendBuffer = new RingBuffer();
	private Object sendLock = new Object();
	
	public void Init(AsynchronousSocketChannel socketChannel, int sessionId)
	{
		this.socketChannel = socketChannel;
		this.sessionId = sessionId;
		registerReceive(); // 초기화하자마자 receive 걸어두기
	}
	//클라이언트로부터 데이터 받기
	// 현재 데이터가 정상적으로 오는경우는 잘 동작함.(패킷1개. 혹은 여러개가 한꺼번에 수신된 경우에서 모든 패킷이 올바르게 왔을 때.)
	// 이제 패킷1개는 제대로오고 2번째 패킷이 덜온경우를 체크해보자.
	void registerReceive()
	{
		ByteBuffer byteBuffer = recvBuffer.getBuffer();
		socketChannel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>(){
			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				try {
					int readSize = 0; // 실제 읽은 총량(all amount)사이즈
					int recvLen = 0; // 남은 패킷 길이.
					attachment.flip();
					
					// 이전에 남은 데이터가 있다면
					int pos = recvBuffer.getPosition(); // 현재 읽어야할 위치.
					int remainLen = recvBuffer.getRemainLen(); // 이전에 남은 데이터가 있는지 체크
					byte[] buffer = attachment.array(); // 원본 버퍼.
					while(true)
					{
						// 수신한 버퍼 데이터의 크기.(이미 읽은건 뺴준다)
						recvLen = attachment.limit() - readSize;
						
						// 더이상 읽을게 없다면.
						if(recvLen <= 0)
							break;

						// 최소 4바이트는 왔는지.
						if((remainLen + recvLen) < Packet.PACKET_MIN_LEN)
							break;

						//int packetLenIndex = attachment.position() + Packet.PACKET_CHECK;
						int packetLenIndex = pos + Packet.PACKET_CHECK; 
						short packetLen = 0;
						
						packetLen |= (((short) buffer[packetLenIndex]) << 8) & 0xFF00;
						packetLen |= (((short) buffer[packetLenIndex + 1])) & 0xFF;
						// 패킷길이보다 작다면.
						if((recvLen + remainLen) < packetLen)
							break;
						
						byte[] packetBuffer = new byte[packetLen];
						//attachment.get(packetBuffer); // 한 버퍼의 여러개 패킷대응을 위해 position 이동.
						recvBuffer.readBuffer(packetLen - remainLen); // 실제 버퍼의 position이동.
						
						System.arraycopy(buffer, pos, packetBuffer, 0, packetLen);
						
						Packet packet = PacketUtil.convertPacketFromBytes(packetBuffer);
						if(packet != null)
							DispatchMessageManager.getInstance().addRecvPacket(sessionId, packet);
						
						readSize += packetLen;
						pos += packetLen;
						
						if(remainLen > 0)
						{
							remainLen = 0;
						}
					}
					if(recvLen <= 0)
						recvBuffer.clean();
					else
					{
						recvBuffer.setPosition(readSize); // 실제 읽은 데이터만큼만 이동.
						recvBuffer.setRemainLen(recvLen); // clean처리하지않고, recvLen(수신한 데이터)을 더해준다.
					}
					
					ByteBuffer byteBuffer2 = recvBuffer.getBuffer();
					socketChannel.read(byteBuffer2, byteBuffer2, this); //데이터 다시 읽기
				}catch(Exception e) {
					e.printStackTrace();
					closeSession();
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				try{
					socketChannel.close();
				}catch(IOException e) {
					e.printStackTrace();
				}
				
			}
			
		});
	}
	
	public int getSessionId()
	{
		return sessionId;
	}

	
	// send는 언제든 호출될 수 있음.
	public void send(Packet packet) {
		//Charset charset = Charset.forName("utf-8");
		//ByteBuffer byteBuffer = charset.encode(data);
		//socketChannel.write(byteBuffer, null, new CompletionHandler<Integer, Void>(){
			//@Override
			//public void completed(Integer result, Void attachment) {
				// TODO Auto-generated method stub
				
			//}

			//@Override
			//public void failed(Throwable exc, Void attachment) {
				//try{
					//String message = "[클라이언트 통신 안됨: "+socketChannel.getRemoteAddress()+" : " + Thread.currentThread().getName() + "]";
					//Platform.runLater(()->displayText(message));
					//connections.remove(Client.this);
					//socketChannel.close();
				//}catch(IOException e) {
					
				//}					
			//}
			
		//});
		
		synchronized(sendLock)
		{
			
		}
		
	}
	
	public void closeSession()
	{
		try
		{
			socketChannel.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
