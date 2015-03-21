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
import tools.Debug;

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
	 * @author Feng
	 */
	public void init() throws IOException {
		// ��ʾIP��ַ
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			Debug.log("IP��ַ", addr.getHostAddress().toString());
			Debug.log("��������", addr.getHostName().toString());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		Debug.log("�˿ںţ�8080");

		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(8080));
	}

	private int count = 0;

	/**
	 *  ���յ��µ�����
	 * @author Feng
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// ���տͻ��˵�����
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());

		Debug.log("byteArray.length = " + byteArray.length);
		// ��С
		int size;
		// �ָ����ݽ��е�������Ĵ���
		byte[] oneReqBytes;
		int reqOffset = 0;
		do {
			Debug.log("\nServerNetwork: ��ʼ�ָ�һ���µ�����!");
			size = DataTypeTranslater.bytesToInt(byteArray, reqOffset);
			System.out.println("size:" + size);
			if (size == 0)
				break;
			oneReqBytes = new byte[size];
			for (int i = 0; i < size; i++)
				oneReqBytes[i] = byteArray[reqOffset + i];

			dealRequest(session, size, oneReqBytes);

			reqOffset += size;
		} while (reqOffset < byteArray.length);

//		new Thread(new check(session)).start();

	}
	
	/**
	 * ������
	 * @author Feng
	 *
	 */
//	class check implements Runnable {
//		IoSession mySession;
//		public check(IoSession session) {
//			mySession = session;
//		}
//		@Override
//		public void run() {
//			try {
//				Scanner s = new Scanner(System.in);
//				byte[] chechStr = new String("check online").getBytes();
//				while (true) {
//					System.err.println("shit");
//					String string = s.next();
////				if (s.equals("s")){
//					System.err.println("input " + string);
//					IoBuffer responseIoBuffer = IoBuffer.allocate(chechStr.length);
//					responseIoBuffer.put(chechStr);
//					responseIoBuffer.flip();
//					mySession.write(chechStr);
//					System.err.println("����У��");
////				}
//				}
//			} catch (Exception e) {
//				System.err.println("my Exception");
//				// TODO: handle exception
//			}
//		}
//	}

	/**
	 *  ���ڴ���һ������
	 * @param session
	 * @param size
	 * @param byteArray
	 * @author Feng
	 */
	private void dealRequest(IoSession ioSession, int size, byte[] byteArray) {
		try {
			ServerModel.instance.addClientRequestToQueue(ioSession, byteArray);
			Debug.log("ServerNetwork", "��Client���������������");
		} catch (InterruptedException e) {
			System.err.println("ServerNetwork : �������������������¼��쳣!");
			e.printStackTrace();
		}
	}

	/**
	 *  �ɵײ�����Ƿ񴴽�һ��session
	 * @author Feng
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		Debug.log("sessionCreated");
	}

	/**
	 *  ������session ���ص�sessionOpened
	 * @author Feng
	 */
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		Debug.log("\n�� " + count + " �� client ��½��address�� : " + session.getRemoteAddress());
		Debug.log("ServerNetwork", "��⵽һ��Client�����ӣ���ӽ�����");
		addClientUserToTable(session);
	}

	/**
	 *  ���ͳɹ����ص��ķ���
	 * @author Feng
	 */
	public void messageSent(IoSession session, Object message) {
		Debug.log("message send to client");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Debug.log("sessionClosed");

	}

	/**
	 *  session ���е�ʱ�����
	 * @author Feng
	 */
	public void sessionIdle(IoSession session, IdleStatus status) {
		Debug.log("connect idle");
	}

	/**
	 *  �쳣��׽
	 * @author Feng
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		Debug.log("throws exception");
		Debug.log("sesson.toString()", session.toString());
		Debug.log("cause.toString()", cause.toString());
		Debug.log("������ϣ���");
	}
	
	/**
	 * ���µ��û���ӵ����������û���Ϣ����
	 * @param ioSession
	 * @author Feng
	 */
	public void addClientUserToTable(IoSession ioSession){
		// ���оͲ��ӽ�����
		if (ServerModel.instance.getClientUserFromTable(ioSession.getRemoteAddress().toString()) != null){
			System.err.println("���ʱ�û��Ѵ���");
			return;
		}
		
		Debug.log("ServerNetwork", "�����µ��û�" + ioSession.getRemoteAddress() + "���ӣ������û���");
		ServerModel.instance.addClientUserToTable(ioSession.getRemoteAddress().toString(), new ClientUser(ioSession));
	}
	
	/**
	 * ���ͻ��˷���
	 * @param ioSession
	 * @param byteArray
	 * @author Feng
	 */
	public void sendMessageToClient(IoSession ioSession, byte[] byteArray) {
		IoBuffer responseIoBuffer = IoBuffer.allocate(byteArray.length);
		responseIoBuffer.put(byteArray);
		responseIoBuffer.flip();
		ioSession.write(responseIoBuffer);
	}
}
