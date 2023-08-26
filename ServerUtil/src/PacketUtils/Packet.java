package PacketUtils;

import java.util.AbstractMap;

public class Packet {
	public static final int PACKET_MIN_LEN = 4; // 최소 식별가능한 packetSize
	public static final int PACKET_CHECK = 2;
	
	private short packetSize = 0;
	private short protocol = 0;
	
	public void setPacketInfo( short packetSize, short protocol )
	{
		this.packetSize = packetSize;
		this.protocol = protocol;
	}
	
	public boolean isValidPacket()
	{
		boolean ret = false;
		if(packetSize >= PACKET_MIN_LEN)
			ret = true;
		
		return ret;
	}
	

	public AbstractMap.SimpleEntry<Short, Short> getPacketInfo()
	{
		return new AbstractMap.SimpleEntry<Short, Short>(packetSize,protocol);
	}
}
