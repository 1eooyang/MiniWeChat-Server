package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;

public class TcpServer {
	
	public static void main(String[] args) throws IOException {
		// ��ʾIP��ַ
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			System.out.println("IP��ַ��" + addr.getHostAddress().toString());
			System.out.println("�������ƣ�" + addr.getHostName().toString());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		System.out.println("�˿ںţ�8080");

		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(new TcpServerHandle());
		acceptor.bind(new InetSocketAddress(8080));
	}

}

class TcpServerHandle extends IoHandlerAdapter {
	public static final int HEAD_INT_SIZE = 4;
	private int count = 0;

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

	// ���յ��µ�����
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// ���տͻ��˵�����
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());
		System.out.println("messageReceived:" + new String(byteArray, "UTF-8"));

		System.out.println("byteArray.length = " + byteArray.length);
		int offset = 0;
		// ��С
		int size = DataTypeTranslater.bytesToInt(byteArray, 0);
		System.out.println("ǰ�ĸ�byteΪ��");
		for (int i=0; i<4; i++)
			System.err.println(byteArray[i]);
		
		offset += HEAD_INT_SIZE;
		System.out.println("Size is :" + size);
		
		// ����
		int typeInt = DataTypeTranslater.bytesToInt(byteArray, 4);
//		System.out.println("Type Number is " + typeInt);

		ProtoHead.ENetworkMessage messageType = ProtoHead.ENetworkMessage.valueOf(typeInt);
		System.out.println("Type is :" + messageType.toString());
		

		offset += HEAD_INT_SIZE;
//		System.out.println("now offset : " + offset);
		
		// ����
		byte[] messageBytes = new byte[size - HEAD_INT_SIZE * 2];
		for (int i=0; i<messageBytes.length; i++)
			messageBytes[i] = byteArray[offset + i]; 

//		System.out.println("message Bytes is :" + messageBytes);

		// �����л�
		byte[] responseByteArray = new byte[0];
		try {
			KeepAliveMsg.KeepAliveSyncPacket packet = KeepAliveMsg.KeepAliveSyncPacket.parseFrom(messageBytes);
			System.out.println("�յ�����" + "   " + messageType.toString() + "  " + packet.getA() + "   " + packet.getB()
					+ "   " + packet.getC());

			// �ؽ�--���ظ�
			KeepAliveMsg.KeepAliveSyncPacket.Builder keepAliveSyncBuilder = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
			int i = messageType.LoginRsp.getNumber();
			keepAliveSyncBuilder.setA(packet.getA() + 1);
			keepAliveSyncBuilder.setB(!packet.getB());
			keepAliveSyncBuilder.setC("fuck " + packet.getC());
			responseByteArray = keepAliveSyncBuilder.build().toByteArray();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		// ���͵��ͻ���
		// byte[] responseByteArray =
		// "Hello MiniWeChat Client".getBytes("UTF-8");
		// byte[] responseByteArray = "Hello".getBytes("UTF-8");
		int returnPacketLength = HEAD_INT_SIZE * 2 + responseByteArray.length;
		IoBuffer responseIoBuffer = IoBuffer.allocate(returnPacketLength);
		System.out.println("�������ݵĳ��� = " + returnPacketLength);
		// 1.size
		responseIoBuffer.put(DataTypeTranslater.intToByte(returnPacketLength));
		// 2.ProtoHead
		System.out.println("�������ͺ� = " + messageType.KeepAliveSync.getNumber() + "   ���� = " + ProtoHead.ENetworkMessage.valueOf(messageType.KeepAliveSync.getNumber()).toString());
		responseIoBuffer.put(DataTypeTranslater.intToByte(messageType.KeepAliveSync.getNumber()));
		// 3.Message
		System.out.println("���ص�byte[] :" + responseByteArray);
		responseIoBuffer.put(responseByteArray);
		responseIoBuffer.flip();
		session.write(responseIoBuffer);
		System.out.println("������ϣ���");
	}

	// �ɵײ�����Ƿ񴴽�һ��session
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("sessionCreated");
	}

	// ������session ���ص�sessionOpened
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		System.out.println("\n�� " + count + " �� client ��½��address�� : " + session.getRemoteAddress());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed");

	}
}