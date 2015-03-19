package server;

import org.apache.mina.core.session.IoSession;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

// ���������µ��ӷ��������������û�����¼�
public class Server_User {
	public static Server_User instance = new Server_User();
	
	private Server_User(){
		
	}
	
	/**
	 *  ��  �û��������ظ�  �Ĵ���
	 *  ��onlineֵ��ΪTrue
	 * @param networkMessage
	 */
	public void KeepAlive(NetworkMessage networkMessage){
		System.out.println(" ��  �û�" + networkMessage.ioSession.getRemoteAddress() + "���������ظ�  �Ĵ���");
		ServerModel.instance.clientUserTable.get(networkMessage.ioSession.getRemoteAddress()).onLine = true;
	}
	
	// �������û�ע���¼�
	public void Register() {
		
	}
	
}
