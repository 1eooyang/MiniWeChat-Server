package server;

import org.apache.mina.core.session.IoSession;

/**
 * �������˶Կͻ����û���״̬��¼
 * @author Feng
 *
 */
public class ClientUser {
	public IoSession ioSession;
	public boolean onLine = true;
	
	public ClientUser(IoSession ioSession){
		this.ioSession = ioSession;
	}
}
