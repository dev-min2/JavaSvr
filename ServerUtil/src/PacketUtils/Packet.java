package PacketUtils;

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
}
