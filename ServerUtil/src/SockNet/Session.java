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
	private Queue<Packet> messages = new LinkedList<Packet>();
	
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
					int recvLen = 0; // 패킷(1개 기준)수신한 사이즈
					attachment.flip();
					while(true)
					{
						byte[] buffer = attachment.array(); // 원본 버퍼이므로 수정조심.
						recvLen = attachment.limit() - readSize;
						
						if(recvLen <= 0)
							break;
						
						// 최소 4바이트는 왔는지.
						if(recvLen < Packet.PACKET_MIN_LEN)
							break;

						int packetLenIndex = attachment.position() + Packet.PACKET_CHECK;
						short packetLen = 0;
						
						packetLen |= (((short) buffer[packetLenIndex]) << 8) & 0xFF00;
						packetLen |= (((short) buffer[packetLenIndex + 1])) & 0xFF;
						// 패킷길이보다 작다면.
						if(recvLen < packetLen)
							break;
						
						byte[] packetBuffer = new byte[packetLen];
						attachment.get(packetBuffer); // 한 버퍼의 여러개 패킷대응을 위해 position 이동.
						recvBuffer.readBuffer(packetLen); // 실제 버퍼의 position또한 이동.
						
						Packet packet = PacketUtil.convertPacketFromBytes(packetBuffer);
						
						DispatchMessageManager.getInstance().addRecvPacket(sessionId, packet);
						readSize += packetLen;
					}
					if(recvLen <= 0)
						recvBuffer.clean();
					
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
