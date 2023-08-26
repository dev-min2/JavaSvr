package SockNet;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import PacketUtils.Packet;

public class DispatchMessageManager 
{
	public static final short ACCEPT_PROTOCOL = -9999;
	
	private static DispatchMessageManager instance = null;
	private static Object lock = new Object();
	private NetServer netServer = null;
	
	private Thread dispatchSendThread = null; 	
	
	private HashMap<Integer,ArrayList<Packet>> recvPacketBySessionId = new HashMap<Integer,ArrayList<Packet>>();
	private HashMap<Integer,ArrayList<Packet>> sendPacketBySessionId = new HashMap<Integer,ArrayList<Packet>>();
	
	private AtomicBoolean threadStopVal = new AtomicBoolean(false);
	
	private DispatchMessageManager() { }
	
	public void init(NetServer netServer)
	{
		this.netServer = netServer;
		dispatchSendThread = new Thread()
		{
			@Override
			public void run()
			{
				try {
					DispatchMessageManager.getInstance().sendRun();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
	}
	
	public void stopThread()
	{
		threadStopVal.compareAndSet(false, true);
	}
	
	public static DispatchMessageManager getInstance() {
		if(instance == null)
		{
			synchronized(lock) 
			{
				if(instance == null)
					instance = new DispatchMessageManager();
			}
		}
		return instance;
	}
	
	public boolean connectSession(int sessionId)
	{
		boolean ret = false;
		synchronized(lock)
		{
			if(recvPacketBySessionId.containsKey(sessionId) == false )
			{
				recvPacketBySessionId.put(sessionId, new ArrayList<Packet>(30));
				sendPacketBySessionId.put(sessionId, new ArrayList<Packet>(30));
				
				Packet packet = new Packet();
				packet.setPacketInfo(ACCEPT_PROTOCOL, ACCEPT_PROTOCOL);
				recvPacketBySessionId.get(sessionId).add(packet); // 접속처리를 위한 더미패킷 넣어준다.
				
				ret = true;
			}
		}
		return ret;
	}
	
	public void addRecvPacket(int sessionId, Packet packet )
	{
		if(sessionId < 0 || packet == null)
			return;
		
		synchronized(lock)
		{
			if(recvPacketBySessionId.containsKey(sessionId) == true )
			{
				var list = recvPacketBySessionId.get(sessionId);
				if(list != null)
					list.add(packet);
			}
		}
	}
	
	public void addSendPacket(int sessionId, Packet packet)
	{
		if(sessionId < 0 || packet == null)
			return;
		
		synchronized(lock)
		{
			if(sendPacketBySessionId.containsKey(sessionId) == true )
			{
				var list = sendPacketBySessionId.get(sessionId);
				if(list != null)
					list.add(packet);
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer,ArrayList<Packet>> flushRecvPacket()
	{
		HashMap<Integer,ArrayList<Packet>> ret = null;
		synchronized(lock)
		{
			// 복사하고 원본은 비우기떄문에 얕은복사여도 된다.
			ret = (HashMap<Integer, ArrayList<Packet>>)recvPacketBySessionId.clone();
			for(Entry<Integer, ArrayList<Packet>> ety : recvPacketBySessionId.entrySet())
			{
				ArrayList<Packet> list = ety.getValue();
				if(list != null)
				{
					list.clear();
				}
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer,ArrayList<Packet>> flushSendPacket()
	{
		HashMap<Integer,ArrayList<Packet>> ret = null;
		synchronized(lock)
		{
			// 복사하고 원본(Value)은 비우기떄문에 얕은복사여도 된다.
			ret = (HashMap<Integer, ArrayList<Packet>>)sendPacketBySessionId.clone();
			for(Entry<Integer, ArrayList<Packet>> ety : sendPacketBySessionId.entrySet())
			{
				ArrayList<Packet> list = ety.getValue();
				if(list != null)
				{
					list.clear();
				}
			}
		}
		return ret;
	}
	
	@SuppressWarnings({ "deprecation", "static-access" })
	public void sendRun() throws InterruptedException
	{
		while(true)
		{
			if(threadStopVal.get())
				break;
			
			HashMap<Integer,ArrayList<Packet>> sendPacketList = flushSendPacket();
			for(Entry<Integer, ArrayList<Packet>> ety : sendPacketList.entrySet())
			{
				int sessionId = ety.getKey();
				var packetList = ety.getValue();
				
				Session session = netServer.getSession(sessionId);
				if(session != null)
				{
					for(Packet packet : packetList)
						session.send(packet);
				}
			}
			
			Thread.currentThread().sleep(10);
		}
		
		System.out.println("DispatchThread " + Thread.currentThread().getId() + "번 쓰레드 종료");
	}
}
