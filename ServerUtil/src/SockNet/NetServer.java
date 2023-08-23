package SockNet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

/*
 소켓 Async Server
 */

public class NetServer {
	
	private InetSocketAddress inetAddress;
	private AsynchronousChannelGroup channelGroup;
	
	//클라이언트 연결 수락
	private AsynchronousServerSocketChannel asyncServerSocketChannel;
	private final AtomicInteger sessionCnt = new AtomicInteger(0);
	private HashMap<Integer,Session> sessionByID = new HashMap<Integer,Session>();
	
	public NetServer(InetSocketAddress inetAddress, boolean config) throws IOException
	{
		// 비동기 채널 그룹, 쓰레드 수는 cpu 코어 * 2 
		channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, Executors.defaultThreadFactory());
		// 서버 소켓 채널 열기
		asyncServerSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
		
		// config값 설정 시.
		if(config) {
			
		}
		else {
			this.inetAddress = inetAddress;
			asyncServerSocketChannel.bind(inetAddress);
		}
		
		DispatchMessageManager.getInstance().init( this );
	}
	
	public boolean startServer()
	{
		if(asyncServerSocketChannel == null || !asyncServerSocketChannel.isOpen())
			return false;

		//콜백 메소드 CompletionHandler 구현
		asyncServerSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>(){
			@Override
			public void completed(AsynchronousSocketChannel result, Void attachment) {
				try {
					Session session = new Session();
					int sessionid = sessionCnt.incrementAndGet();
					sessionByID.put(sessionid, session);
					session.Init(result, sessionid);
					
					DispatchMessageManager.getInstance().connectSession(sessionid);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					asyncServerSocketChannel.accept(null, this); // 다음 accept.
				}
			}
 
			@Override
			public void failed(Throwable exc, Void attachment) {
				if(asyncServerSocketChannel.isOpen()) {
					try {
						stopServer();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}); //accept()종료
		
		return true;
	}
	
	public boolean stopServer() throws IOException
	{
		if(asyncServerSocketChannel != null && asyncServerSocketChannel.isOpen())
			asyncServerSocketChannel.close();
		
		return true;
	}
	
	public Session getSession(int sessionId)
	{
		return sessionByID.get(sessionId);
	}
}
