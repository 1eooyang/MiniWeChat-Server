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
		System.out.println("Server_User: ��  �û�" + networkMessage.ioSession.getRemoteAddress() + "���������ظ�  �Ĵ���");
//		System.out.println(ServerModel.instance.clientUserTable.keySet().size());
		System.out.println("fuck   " + networkMessage== null);
		// ���ClientUser�Ѿ����߱�ɾ������ô�Ͳ�����
		if (!ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress())){
			System.out.println("Server_User: �û�" + networkMessage.ioSession.getRemoteAddress() + "�ѵ��ߣ������ظ���������!");
			return;
		}
		
		ServerModel.instance.clientUserTable.get(networkMessage.ioSession.getRemoteAddress()).onLine = true;
	}
	
	// �������û�ע���¼�
	public void Register() {
		
	}
	
}
