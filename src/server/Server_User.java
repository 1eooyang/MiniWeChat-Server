package server;

import org.apache.mina.core.session.IoSession;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

// ���������µ��ӷ��������������û�����¼�
public class Server_User {
	public static Server_User instance = new Server_User();
	
	private Server_User(){
		
	}
	
	// ��  �û��������ظ�  �Ĵ���
	public void KeepAlive(NetworkMessage networkMessage){
		System.out.println(" ��  �û��������ظ�  �Ĵ���");
//		ServerModel.instance.clientUserSet[networkMessage.ioSession.getRemoteAddress()]
	}
	
	// �������û�ע���¼�
	public void Register() {
		
	}
	
}
