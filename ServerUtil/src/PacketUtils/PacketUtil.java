package PacketUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class PacketUtil
{
	private PacketUtil() {}
	
	public static byte[] getPacketBuffer(short protocol, Object object) throws Exception
	{
		if(protocol < 0 || object == null)
			throw new Exception("protocol < 0 || object == null");
		
		byte[] protocolArray = ByteBuffer.allocate(Short.BYTES).putShort(protocol).array();
		byte[] objectArray = Serialize(object);
		if(objectArray == null)
			throw new Exception("Object Array null");
		final short PACKET_SIZE = (short)(Packet.PACKET_MIN_LEN + objectArray.length);
		
		ByteBuffer buffer = ByteBuffer.allocate(2);
		byte[] packetLenBuffer = buffer.putShort(PACKET_SIZE).array();

		byte[] ret = new byte[PACKET_SIZE];
		System.arraycopy(protocolArray, 0, ret, 0, protocolArray.length);
		System.arraycopy(packetLenBuffer, 0, ret, Packet.PACKET_CHECK, packetLenBuffer.length);
		System.arraycopy(objectArray, 0, ret, Packet.PACKET_MIN_LEN, objectArray.length);
		return ret;
	}
	
	public static byte[] convertBytesFromPacket(Packet packet) throws Exception
	{
		if(packet == null || !packet.isValidPacket())
			return null;
		
		return getPacketBuffer(packet.getPacketInfo().getValue(),packet);
	}
	
	
	public static Packet convertPacketFromBytes(byte[] bytes) throws ClassNotFoundException, IOException
	{
		short packetSize = isValidPacket(bytes);
		if(packetSize < 0)
			return null;
		
		return GeneratePacket(bytes, packetSize);
	}
	

	private static byte[] Serialize(Object object) throws IOException {
		if(object == null)
			return null;
		
		if(!(object instanceof Packet))
			return null;
		
	    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutputStream out = new ObjectOutputStream(bos)) {
	        out.writeObject(object);
	        return bos.toByteArray();
	    } 
	}
	
	private static Object Deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		if(bytes == null)
			return null;
		
	    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	        ObjectInputStream in = new ObjectInputStream(bis)) {
	        return in.readObject();
	    } 
	}
	
	private static Packet GeneratePacket(byte[] bytes, short packetSize) throws ClassNotFoundException, IOException
	{
		byte[] protocolBuffer = new byte[Packet.PACKET_CHECK];
		System.arraycopy(bytes, 0, protocolBuffer, 0, Packet.PACKET_CHECK);
		short protocol = ByteBuffer.wrap(protocolBuffer).getShort();
		
		int objectLen = packetSize - Packet.PACKET_MIN_LEN;
		byte[] objectBuffer = new byte[objectLen];
		System.arraycopy(bytes, Packet.PACKET_MIN_LEN, objectBuffer, 0, objectLen); 
		
		Packet packet = (Packet)Deserialize(objectBuffer);
		packet.setPacketInfo(packetSize,protocol);
		return packet;
	}
	
	// 데이터 수신 후 정상적인 패킷인지 체크 후 정상적이라면 패킷 크기를 반환
	private static short isValidPacket(byte[] bytes)
	{
		if(bytes == null)
			return -1;
		
		if(bytes.length < Packet.PACKET_MIN_LEN)
			return -1;
		
		byte[] packetLenBuffer = new byte[Packet.PACKET_CHECK];
		System.arraycopy(bytes, 2, packetLenBuffer, 0, Packet.PACKET_CHECK);
		short packetLen = ByteBuffer.wrap(packetLenBuffer).getShort();
		
		//버퍼에서 읽어온 패킷 크기값이 실제 버퍼크기보다크다면 false. 
		if(packetLen > bytes.length)
			return -1;
		
		return packetLen;
	}
}