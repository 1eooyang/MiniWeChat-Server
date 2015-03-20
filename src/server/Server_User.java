package server;

import model.HibernateSessionFactory;
import model.User;

import org.apache.mina.core.session.IoSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

/**
 *  ���������µ��ӷ��������������û�����¼�
 * @author Feng
 *
 */
public class Server_User {
	public static Server_User instance = new Server_User();
	
	private Server_User(){
		
	}
	
	/**
	 *  ��  �û��������ظ�  �Ĵ���
	 *  ��onlineֵ��ΪTrue
	 * @param networkMessage
	 * @author Feng
	 */
	public void KeepAlive(NetworkMessage networkMessage){
//		System.out.println((networkMessage == null) + "      " + (networkMessage.ioSession == null));
		System.out.println("Server_User: ��  �û�" + networkMessage.ioSession.getRemoteAddress() + "  �ظ���������  �Ĵ���");
//		System.out.println(ServerModel.instance.clientUserTable.keySet().size());
//		System.out.println("fuck   " + ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress().toString()));
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
	
	/**
	 *  �������û�ע���¼�
	 * @param networkMessage
	 * @author Feng
	 */
	public void Register(NetworkMessage networkMessage) {
		System.out.println("Server_User: ��  �û�" + networkMessage.ioSession.getRemoteAddress() + "  ��ע���¼�  �Ĵ���");
		
		// �����Ƿ����ͬ���û�
		   Session session = HibernateSessionFactory.getSession();
		   Criteria criteria = session.createCriteria(User.class);
		   criteria.add(Restrictions.eq("account", "account2"));
		   if (criteria.list().size() > 0) {
			   // �Ѵ�����ͬ�˺��û������߿ͻ���
			   
			   
			   
			   return;
		   }

    	User user = new User();
    	user.setAccount("account1");
    	user.setAccountName("name1");
    	user.setAccountPassword("121");
    	
    	session = HibernateSessionFactory.getSession();
    	session.save(user);
    	HibernateSessionFactory.commitSession(session);
	}
	
}
