package server;

import org.apache.mina.core.session.IoSession;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

// ���������µ��Է��������������û�����¼�
public class TcpServer_User {
	public static TcpServer_User instance = new TcpServer_User();
	
	private TcpServer_User(){
		
	}
	
	// �û�������
	public void KeepAlive(IoSession session, int size, KeepAliveMsg.KeepAliveSyncPacket packet){
		
	}
	
	// �������û�ע���¼�
	public void Register() {
		
	}
	
}
