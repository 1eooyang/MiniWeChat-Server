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
//		System.out.println((networkMessage == null) + "      " + (networkMessage.ioSession == null));
		System.out.println("Server_User: ��  �û�" + networkMessage.ioSession.getRemoteAddress() + "  �ظ���������  �Ĵ���");
//		System.out.println(ServerModel.instance.clientUserTable.keySet().size());
		System.out.println("fuck   " + ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress().toString()));
		// ���ClientUser�Ѿ����߱�ɾ������ô�Ͳ�����
		try {
			if (!ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress().toString())){
				System.out.println("Server_User: �û���(ClientUserTalbe)���Ҳ��� �û�" + networkMessage.ioSession.getRemoteAddress() + "�������ظ���������!");
				return;
			}
			System.err.println("a");
			
			ServerModel.instance.clientUserTable.get(networkMessage.ioSession.getRemoteAddress().toString()).onLine = true;
		} catch (NullPointerException e) {
			System.out.println("Server_User: �쳣���û�" + networkMessage.ioSession.getRemoteAddress() + "�ѵ��ߣ������ظ���������!");
			e.printStackTrace();
		}
	}
	
	// �������û�ע���¼�
	public void Register() {
		
	}
	
}
