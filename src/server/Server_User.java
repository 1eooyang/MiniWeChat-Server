package server;

import java.io.IOException;

import model.HibernateSessionFactory;
import model.User;

import org.apache.mina.core.session.IoSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import protocol.RegisterMsg;
import tools.DataTypeTranslater;
import tools.Debug;

/**
 * ���������µ��ӷ��������������û�����¼�
 * 
 * @author Feng
 * 
 */
public class Server_User {
	public static Server_User instance = new Server_User();

	private Server_User() {

	}

	/**
	 * �� �û��������ظ� �Ĵ��� ��onlineֵ��ΪTrue
	 * 
	 * @param networkMessage
	 * @author Feng
	 */
	public void keepAlive(NetworkMessage networkMessage) {
		// System.out.println((networkMessage == null) + "      " +
		// (networkMessage.ioSession == null));
		System.out.println("Server_User: ��  �û�" + networkMessage.ioSession.getRemoteAddress() + "  �ظ���������  �Ĵ���");
		// System.out.println(ServerModel.instance.clientUserTable.keySet().size());
		// System.out.println("fuck   " +
		// ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress().toString()));
		// ���ClientUser�Ѿ����߱�ɾ������ô�Ͳ�����
		try {
			if (!ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress().toString())) {
				System.out.println("Server_User: �û���(ClientUserTalbe)���Ҳ��� �û�" + networkMessage.ioSession.getRemoteAddress()
						+ "�������ظ���������!");
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
	 * �������û�ע���¼�
	 * 
	 * @param networkMessage
	 * @author Feng
	 */
	public void register(NetworkMessage networkMessage) {
		Debug.log("Server_User", "ע���¼��� ��  �û�" + networkMessage.ioSession.getRemoteAddress() + "  ��ע���¼�  �Ĵ���");

		try {
			RegisterMsg.RegisterReq registerObject = RegisterMsg.RegisterReq.parseFrom(networkMessage.getMessageObjectBytes());
			RegisterMsg.RegisterRsp.Builder responseBuilder = RegisterMsg.RegisterRsp.newBuilder();

			// �����Ƿ����ͬ���û�
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", registerObject.getUserId()));
			if (criteria.list().size() > 0) {	// �Ѵ���
				// �Ѵ�����ͬ�˺��û������߿ͻ���
				Debug.log("Server_User", "ע���¼����û�" + networkMessage.ioSession.getRemoteAddress() + "  ��ע���˺��ظ������ش���!");

				responseBuilder.setResultCode(RegisterMsg.ResultCode.USER_EXIST);
			} else {	// û���⣬���Կ�ʼע��
				User user = new User();
				user.setUserId(registerObject.getUserId());
				user.setUserName(registerObject.getUserName());
				user.setUserPassword(registerObject.getUserPassword());
				
				session = HibernateSessionFactory.getSession();
				session.save(user);
				HibernateSessionFactory.commitSession(session);
				
				// �ɹ������ûذ���
				Debug.log("Server_User", "ע���¼����û�" + networkMessage.ioSession.getRemoteAddress() + "  ע��ɹ���������Ϣ!");
				responseBuilder.setResultCode(RegisterMsg.ResultCode.SUCCESS);
			}

			// �ظ��ͻ���
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.RegisterRsp.getNumber(), responseBuilder.build().toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : ע���¼��� ��Protobuf�����л� " + networkMessage.ioSession.getRemoteAddress() + " �İ�ʱ�쳣��");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Server_User : ע���¼��� " + networkMessage.ioSession.getRemoteAddress() + " ���ذ�ʱ�쳣��");
			e.printStackTrace();
		}

	}

}
