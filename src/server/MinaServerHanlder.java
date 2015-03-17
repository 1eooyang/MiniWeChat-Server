package server;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaServerHanlder extends IoHandlerAdapter {
	private int count = 0;

	// �ɵײ�����Ƿ񴴽�һ��session
	public void sessionCreated(IoSession session) {
		System.out.println("�¿ͻ�����");
	}

	// ������session ���ص�sessionOpened
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		System.out.println("�� " + count + " �� client ��½��address�� : " + session.getRemoteAddress());
	}

	// ���յ��˿ͻ��˷��͵���Ϣ���ص��������
	public void messageReceived(IoSession session, Object message) throws Exception {
		System.out.println("�������յ��ͻ��˷���ָ�� ��" + (message.toString()).getBytes("UTF-8"));
		System.out.println(message.toString());
	}

	// ���ͳɹ����ص��ķ���
	public void messageSent(IoSession session, Object message) {
		System.out.println("message send to client");
	}

	// session �رյ���
	public void sessionClosed(IoSession session) {
		System.out.println("one client disconnect");
	}

	// session ���е�ʱ�����
	public void sessionIdle(IoSession session, IdleStatus status) {
		System.out.println("connect idle");
	}

	// �쳣��׽
	public void exceptionCaught(IoSession session, Throwable cause) {
		System.out.println("throws exception");
		System.err.println(session.toString());
		System.err.println(cause.toString());
	}
}
