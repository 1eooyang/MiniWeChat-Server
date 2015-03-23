package server;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;

import protocol.ProtoHead;

import sun.awt.datatransfer.DataTransferer;
import tools.DataTypeTranslater;

/**
 * �������ͨ��ÿһ������Ķ��󣨽�������У�
 * @author Feng
 */
public class NetworkMessage {
	// ��Ϣ�е�int��ռ���ֽ���
	public static final int HEAD_INT_SIZE = 4;
	// ��Ϣ�е�floats��ռ���ֽ���
	public static final int HEAD_FLOAT_SIZE = 4;

	public IoSession ioSession;
	public byte[] arrayBytes;
	
	public NetworkMessage(IoSession ioSession, byte[] arrayBytes){
		this.ioSession = ioSession;
		this.arrayBytes = arrayBytes;
	}

	/**
	 *  ��ȡ����ĳ���
	 * @return int
	 * @author Feng
	 */
	public int getMessageLength() {
		return arrayBytes.length;
	}

	public static int getMessageLength(byte[] array) {
		return array.length;
	}

	/**
	 *  ��ȡ��������
	 * @return ProtoHead.ENetworkMessage
	 * @author Feng
	 */
	public ProtoHead.ENetworkMessage getMessageType() {
		return getMessageType(arrayBytes);
	}

	public static ProtoHead.ENetworkMessage getMessageType(byte[] array) {
		return ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(array, HEAD_INT_SIZE));
	}
	
	/**
	 * ��ȡMessageId
	 * @return byte[]
	 * @author Feng
	 */
	public byte[] getMessageID(){
		return getMessageID(arrayBytes);
	}
	
	public static byte[] getMessageID(byte[] array){
		int offset = HEAD_FLOAT_SIZE * 2;
		byte[] messageIdBytes = new byte[HEAD_FLOAT_SIZE];
		for (int i=0; i<messageIdBytes.length; i++)
			messageIdBytes[i] = array[offset + i];
		
		return messageIdBytes;
	}
	
	/**
	 *  ��ȡ��Ϣ�Ķ���byte����
	 * @return byte[]
	 * @author Feng
	 */
	public byte[] getMessageObjectBytes(){
		return getMessageObjectBytes(arrayBytes);
	}
	
	public static byte[] getMessageObjectBytes(byte[] array){
		byte[] response = new byte[getMessageLength(array) - getMessageObjectStartIndex()];
		for (int i=0; i<response.length; i++)
			response[i] = array[getMessageObjectStartIndex() + i];
		
		return response;
	}
	
	/**
	 *  ����ɿ��Է��͵�byte[]
	 * @param messageType
	 * @param packetBytes
	 * @return byte[]
	 * @throws IOException
	 * @author Feng
	 */
	public static byte[] packMessage(int messageType, byte[] packetBytes) throws IOException {
		return packMessage(messageType, ServerModel.createMessageId(), packetBytes);
	}

	public static byte[] packMessage(int messageType, byte[] messageIdBytes, byte[] packetBytes) throws IOException {
		int size = getMessageObjectStartIndex() + packetBytes.length;
		byte[] messageBytes = new byte[size];
		
		// 1.���size
		byte[] sizeBytes = DataTypeTranslater.intToByte(size);
		for (int i=0; i<sizeBytes.length; i++)
			messageBytes[i] = sizeBytes[i];
		
		// 2.��������
		byte[] typeBytes = DataTypeTranslater.intToByte(messageType);
		for (int i=0; i<typeBytes.length; i++)
			messageBytes[getTypeStartIndex() + i] = typeBytes[i];
		
		// 3.���MessageId
		for (int i=0; i<messageIdBytes.length; i++)
			messageBytes[getMessageIdStartIndex() + i] = messageIdBytes[i];
		
		// 4.�������ݰ�
		for (int i=0; i<packetBytes.length; i++)
			messageBytes[getMessageObjectStartIndex() + i] = packetBytes[i];
		
		return messageBytes;
	}
	
	// �������ݵĿ�ʼλ
	public static int getSizeStartIndex(){
		return 0;
	}
	public static int getTypeStartIndex(){
		return HEAD_INT_SIZE;
	}
	public static int getMessageIdStartIndex(){
		return getTypeStartIndex() + HEAD_INT_SIZE;
	}
	
	public static int getMessageObjectStartIndex(){
		return getMessageIdStartIndex() + HEAD_FLOAT_SIZE;
	}
}
