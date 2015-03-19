package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;

/**
 * ������������㣬�������罻��
 * 
 * @author Feng
 * 
 */
public class ServerNetwork extends IoHandlerAdapter {
	public static ServerNetwork instance = new ServerNetwork();

	private ServerNetwork() {

	}

	/**
	 *  ��ʼ��
	 * @throws IOException
	 */
	public void init() throws IOException {
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
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(8080));
	}

	private int count = 0;

	/**
	 *  ���յ��µ�����
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// ���տͻ��˵�����
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());
		// System.out.println("messageReceived:" + new String(byteArray,
		// "UTF-8"));

		System.out.println("byteArray.length = " + byteArray.length);
		// ��С
		int size;
		// System.out.println("ǰ�ĸ�byteΪ��");
		// for (int i=0; i<4; i++)
		// System.err.println(byteArray[i]);

		// �ָ����ݽ��е�������Ĵ���
		byte[] oneReqBytes;
		int reqOffset = 0;
		do {
			System.out.println();
			System.out.println("��ʼ����һ���µ�����!");
			size = DataTypeTranslater.bytesToInt(byteArray, 0);
			oneReqBytes = new byte[size];
			for (int i = 0; i < size; i++)
				oneReqBytes[i] = byteArray[reqOffset + i];

			dealRequest(session, size, oneReqBytes);

			reqOffset += size;
		} while (reqOffset < byteArray.length);

//		new Thread(new check(session)).start();

	}
	class check implements Runnable {
		IoSession mySession;
		public check(IoSession session) {
			mySession = session;
		}
		@Override
		public void run() {
			try {
				Scanner s = new Scanner(System.in);
				byte[] chechStr = new String("check online").getBytes();
				while (true) {
					System.err.println("shit");
					String string = s.next();
//				if (s.equals("s")){
					System.err.println("input " + string);
					IoBuffer responseIoBuffer = IoBuffer.allocate(chechStr.length);
					responseIoBuffer.put(chechStr);
					responseIoBuffer.flip();
					mySession.write(chechStr);
					System.err.println("����У��");
//				}
				}
			} catch (Exception e) {
				System.err.println("my Exception");
				// TODO: handle exception
			}
		}
	}

	/**
	 *  ���ڴ���һ������
	 * @param session
	 * @param size
	 * @param byteArray
	 */
	private void dealRequest(IoSession session, int size, byte[] byteArray) {
		try {
			ServerModel.instance.requestQueue.put(new NetworkMessage(session, byteArray));
		} catch (InterruptedException e) {
			System.err.println("�������������������¼��쳣!");
			e.printStackTrace();
		}
		
//		System.out.println("Size is :" + size);
//		int offset = NetworkMessage.HEAD_INT_SIZE;
//
//		// ����
//		int typeInt = DataTypeTranslater.bytesToInt(byteArray, 4);
//		// System.out.println("Type Number is " + typeInt);
//
//		ProtoHead.ENetworkMessage messageType = ProtoHead.ENetworkMessage.valueOf(typeInt);
//		System.out.println("Type is :" + messageType.toString());
//		offset += NetworkMessage.HEAD_INT_SIZE;
//
//		// ����
//		byte[] messageBytes = new byte[size - NetworkMessage.HEAD_INT_SIZE * 2];
//		for (int i = 0; i < messageBytes.length; i++)
//			messageBytes[i] = byteArray[offset + i];
//
//		byte[] responseByteArray = new byte[0];
//		// �����л�
//		try {
//			KeepAliveMsg.KeepAliveSyncPacket packet = KeepAliveMsg.KeepAliveSyncPacket.parseFrom(messageBytes);
//			System.out.println("�յ�����" + "   " + messageType.toString() + "  " + packet.getA() + "   " + packet.getB() + "   "
//					+ packet.getC());
//
//			// �ؽ�--���ظ�
//			KeepAliveMsg.KeepAliveSyncPacket.Builder keepAliveSyncBuilder = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
//			int i = messageType.LoginRsp.getNumber();
//			keepAliveSyncBuilder.setA(12345678);
//			keepAliveSyncBuilder.setB(!packet.getB());
//			keepAliveSyncBuilder.setC("Holy Shit������");
//			responseByteArray = keepAliveSyncBuilder.build().toByteArray();
//
//			// ���͵��ͻ���
//			int returnPacketLength = NetworkMessage.HEAD_INT_SIZE * 2 + responseByteArray.length;
//			IoBuffer responseIoBuffer = IoBuffer.allocate(returnPacketLength);
//			System.out.println("�������ݵĳ��� = " + returnPacketLength);
//			// 1.size
//			responseIoBuffer.put(DataTypeTranslater.intToByte(returnPacketLength));
//			// 2.ProtoHead
//			System.out.println("�������ͺ� = " + messageType.KeepAliveSync.getNumber() + "   ���� = "
//					+ ProtoHead.ENetworkMessage.valueOf(messageType.KeepAliveSync.getNumber()).toString());
//			responseIoBuffer.put(DataTypeTranslater.intToByte(messageType.KeepAliveSync.getNumber()));
//			// 3.Message
//			System.out.println("���ص�byte[] :" + responseByteArray);
//			responseIoBuffer.put(responseByteArray);
//
//			// for (int i=0; i<9; i++){
//			// responseIoBuffer.put(DataTypeTranslater.intToByte(returnPacketLength));
//			// responseIoBuffer.put(DataTypeTranslater.intToByte(messageType.KeepAliveSync.getNumber()));
//			// responseIoBuffer.put(responseByteArray);
//			// }
//			responseIoBuffer.flip();
//			session.write(responseIoBuffer);
//			session.write(responseIoBuffer);
//
//			System.out.println("������ϣ���");
//			System.out.println("���������");
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//		}
	}

	/**
	 *  �ɵײ�����Ƿ񴴽�һ��session
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("sessionCreated");
	}

	// ������session ���ص�sessionOpened
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		System.out.println("\n�� " + count + " �� client ��½��address�� : " + session.getRemoteAddress());
	}

	// ���ͳɹ����ص��ķ���
	public void messageSent(IoSession session, Object message) {
		System.out.println("message send to client");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed");

	}

	// session ���е�ʱ�����
	public void sessionIdle(IoSession session, IdleStatus status) {
		System.out.println("connect idle");
	}

	// �쳣��׽
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		System.out.println("throws exception");
		System.err.println("sesson.toString() :" + session.toString());
		System.err.println("cause.toString() :" + cause.toString());
		System.err.println("������ϣ���");
	}
}
