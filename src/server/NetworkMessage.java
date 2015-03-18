package server;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;

import protocol.ProtoHead;

import sun.awt.datatransfer.DataTransferer;
import tools.DataTypeTranslater;

/**
 * �������ͨ��ÿһ������Ķ��󣨽�������У�
 * 
 * @author Administrator
 * 
 */
public class NetworkMessage {
	// ��Ϣ�е�int��ռ���ֽ���
	public static final int HEAD_INT_SIZE = 4;

	public IoSession ioSession;
	public byte[] arrayBytes;
	
	public NetworkMessage(IoSession ioSession, byte[] arrayBytes){
		this.ioSession = ioSession;
		this.arrayBytes = arrayBytes;
	}

	// ��ȡ����ĳ���
	public int getMessageLength() {
		return arrayBytes.length;
	}

	public static int getMessageLength(byte[] array) {
		return array.length;
	}

	// ��ȡ��������
	public ProtoHead.ENetworkMessage getMessageType() {
		return getMessageType(arrayBytes);
	}

	public static ProtoHead.ENetworkMessage getMessageType(byte[] array) {
		return ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(array, HEAD_INT_SIZE));
	}
	
	// ��ȡ��Ϣ�Ķ���byte����
	public byte[] getMessageObjectBytes(){
		byte[] response = new byte[getMessageLength() - HEAD_INT_SIZE * 2];
		for (int i=0; i<response.length; i++)
			response[i] = arrayBytes[HEAD_INT_SIZE * 2 + i];
		
		return response;
	}
	
	// ����ɿ��Է��͵�byte[]
	public static byte[] packMessage(int messageType, byte[] packetBytes) throws IOException {
		int size = NetworkMessage.HEAD_INT_SIZE * 2 + packetBytes.length;
		byte[] messageBytes = new byte[size];
		
		// 1.���size
		byte[] sizeBytes = DataTypeTranslater.intToByte(size);
		for (int i=0; i<sizeBytes.length; i++)
			messageBytes[i] = sizeBytes[i];
		
		// 2.��������
		int offset = sizeBytes.length;
		byte[] typeBytes = DataTypeTranslater.intToByte(messageType);
		for (int i=0; i<typeBytes.length; i++)
			messageBytes[offset + i] = typeBytes[i];
		offset += typeBytes.length;
		
		// 3.�������ݰ�
		for (int i=0; i<packetBytes.length; i++)
			messageBytes[offset + i] = packetBytes[i];
		
		return messageBytes;
	}
}
