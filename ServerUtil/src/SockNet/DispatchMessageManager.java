package SockNet;
import java.util.*;
import java.util.Map.Entry;

import PacketUtils.Packet;

public class DispatchMessageManager 
{
	private static DispatchMessageManager instance = null;
	private static Object lock = new Object();
	private NetServer netServer = null;
	
	private Thread dispatchSendThread = null; 
	
	private HashMap<Integer,ArrayList<Packet>> recvPacketBySessionId = new HashMap<Integer,ArrayList<Packet>>();
	private HashMap<Integer,ArrayList<Packet>> sendPacketBySessionId = new HashMap<Integer,ArrayList<Packet>>();
	
	private DispatchMessageManager() 
	{
		
	}
	
	public void init(NetServer netServer)
	{
		this.netServer = netServer;
		dispatchSendThread = new Thread()
		{
			@Override
			public void run()
			{
				try {
					DispatchMessageManager.getInstance().Test();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
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
				recvPacketBySessionId.get(sessionId).add(packet);
			}
		}
	}
	
	public void SendPacket(int sessionId, Packet packet)
	{
		Session session = netServer.getSession(sessionId);
		
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
	
	public void Test() throws InterruptedException
	{
		while(true)
		{
			
			
			Thread.currentThread().sleep(10);
		}
	}
}
