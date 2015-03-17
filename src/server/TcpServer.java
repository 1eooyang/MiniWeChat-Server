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

		// ���͵��ͻ���
//		byte[] responseByteArray = "Hello MiniWeChat Client".getBytes("UTF-8");
		byte[] responseByteArray = "Hello".getBytes("UTF-8");
//		byte[] responseByteArray = new byte[].getBytes("UTF-8");
//		byte[] responseByteArray = "Fuck ��ѡ��".getBytes("UTF-8");
		IoBuffer responseIoBuffer = IoBuffer.allocate(responseByteArray.length);
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
		System.out.println("�� " + count + " �� client ��½��address�� : " + session.getRemoteAddress());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed");
		
	}
}