package server;

import java.util.Date;

import org.apache.mina.core.session.IoSession;

/**
 * ����һ�����ڡ�����Client�ظ������еĶ���
 * @author Feng
 *
 */
public class WaitClientResponse {
	long time;
	public byte[] messageHasSent;
	public IoSession ioSession;
	
	public WaitClientResponse(IoSession ioSession, byte[] messageHasSent) {
		this.ioSession = ioSession;
		this.messageHasSent = messageHasSent;
		
		time = new Date().getTime();
	}
}
