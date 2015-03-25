package server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import model.HibernateSessionFactory;
import model.User;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.google.protobuf.InvalidProtocolBufferException;

import exception.NoIpException;

import protocol.ProtoHead;
import protocol.Msg.LoginMsg;
import protocol.Msg.LogoutMsg;
import protocol.Msg.OffLineMsg;
import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.RegisterMsg;
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
		// System.out.println(ServerModel.instance.clientUserTable.keySet().size());
		// System.out.println("fuck   " +
		// ServerModel.instance.clientUserTable.containsKey(ServerModel.getIoSessionKey(networkMessage.ioSession)));
		// ���ClientUser�Ѿ����߱�ɾ������ô�Ͳ�����
		try {
			Debug.log("Server_User", "��  �û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  �ظ���������  �Ĵ���");

			if (ServerModel.instance.getClientUserFromTable(networkMessage.ioSession) == null) {
				Debug.log("Server_User", "�û���(ClientUserTalbe)���Ҳ��� �û�" + ServerModel.getIoSessionKey(networkMessage.ioSession)
						+ "�������ظ���������!");
				return;
			}

			ServerModel.instance.getClientUserFromTable(networkMessage.ioSession).onLine = true;
		} catch (NullPointerException e) {
			System.out.println("Server_User: �쳣���û�" + networkMessage.ioSession + "�ѵ��ߣ������ظ���������!");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �������û�ע���¼�
	 * 
	 * @param networkMessage
	 * @author Feng
	 * @throws NoIpException
	 */
	public void register(NetworkMessage networkMessage) throws NoIpException {
		try {
			Debug.log("Server_User", "ע���¼��� ��  �û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  ��ע���¼�  �Ĵ���");

			RegisterMsg.RegisterReq registerObject = RegisterMsg.RegisterReq.parseFrom(networkMessage.getMessageObjectBytes());
			RegisterMsg.RegisterRsp.Builder responseBuilder = RegisterMsg.RegisterRsp.newBuilder();

			// �����Ƿ����ͬ���û�
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", registerObject.getUserId()));
			if (criteria.list().size() > 0) { // �Ѵ���
				// �Ѵ�����ͬ�˺��û������߿ͻ���
				// System.out.println("ʲô��");
				Debug.log("Server_User", "ע���¼����û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  ��ע���˺��ظ������ش���!");

				responseBuilder.setResultCode(RegisterMsg.RegisterRsp.ResultCode.USER_EXIST);
			} else { // û���⣬���Կ�ʼע��
				User user = new User();
				user.setUserId(registerObject.getUserId());
				user.setUserName(registerObject.getUserName());
				user.setUserPassword(registerObject.getUserPassword());

				session = HibernateSessionFactory.getSession();
				session.save(user);
				HibernateSessionFactory.commitSession(session);

				// �ɹ������ûذ���
				Debug.log("Server_User", "ע���¼����û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  ע��ɹ���������Ϣ!");
				responseBuilder.setResultCode(RegisterMsg.RegisterRsp.ResultCode.SUCCESS);
			}

			// �ظ��ͻ���
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.REGISTER_RSP.getNumber(), networkMessage.getMessageID(), responseBuilder.build()
							.toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : ע���¼��� ��Protobuf�����л� " + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ " �İ�ʱ�쳣��");
		} catch (IOException e) {
			System.err.println("Server_User : ע���¼��� " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " ���ذ�ʱ�쳣��");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����Client�ġ���½����
	 * 
	 * @param networkMessage
	 * @author Feng
	 * @throws NoIpException
	 */
	public void login(NetworkMessage networkMessage) throws NoIpException {
		try {
			Debug.log(new String[] { "Server_User", "login" }, " ��  �û�" + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ "  �ĵ�½�¼�  �Ĵ���");

			LoginMsg.LoginReq loginObject = LoginMsg.LoginReq.parseFrom(networkMessage.getMessageObjectBytes());
			LoginMsg.LoginRsp.Builder loginBuilder = LoginMsg.LoginRsp.newBuilder();

			// �����Ƿ����ͬ���û�
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", loginObject.getUserId()));
			if (criteria.list().size() > 0) { // �Ѵ���
				// �û����ڣ���ʼУ��
				User user = (User) criteria.list().get(0);
				if (user.getUserPassword().equals(loginObject.getUserPassword())) { // ������ȷ
					Debug.log(new String[] { "Server_User", "login" },
							"�û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  �ĵ�½У��ɹ�!");

					// ����Ƿ����ظ���½
					checkAnotherOnline(networkMessage, loginObject.getUserId());

					// ��¼������
					ClientUser clientUser = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
					if (clientUser != null)
						clientUser.userId = loginObject.getUserId();

					// ��¼�ظ�λ
					loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.SUCCESS);
				} else { // �������
					Debug.log(new String[] { "Server_User", "login" },
							"�û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  �ĵ�½�������!");
					loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
				}
			} else { // �û�������
				Debug.log(new String[] { "Server_User", "login" }, "�û�" + ServerModel.getIoSessionKey(networkMessage.ioSession)
						+ "  ���û�������!");
				loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
			}
			session.close();

			// �ظ��ͻ���
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.LOGIN_RSP.getNumber(), networkMessage.getMessageID(), loginBuilder.build()
							.toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : ע���¼��� ��Protobuf�����л� " + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ " �İ�ʱ�쳣��");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Server_User : ע���¼��� " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " ���ذ�ʱ�쳣��");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����Ƿ�����һ��ͬ�˺ŵ��û���½���еĻ�����ȥ
	 * 
	 * @param networkMessage
	 * @return
	 * @throws IOException
	 * @throws NoIpException
	 */
	private boolean checkAnotherOnline(NetworkMessage networkMessage, String userId) throws IOException {
		ClientUser user = ServerModel.instance.getClientUserByUserId(userId);
		if (user != null && !user.die) {
			// ���������˵�½��Ϣ
			OffLineMsg.OffLineReq.Builder offLineMessage = OffLineMsg.OffLineReq.newBuilder();
			offLineMessage.setCauseCode(OffLineMsg.OffLineReq.CauseCode.ANOTHER_LOGIN);
			byte[] objectBytes = offLineMessage.build().toByteArray();

			try {
				Debug.log(new String[] { "Server_User", "checkAnotherOnline" },
						"�û� " + user.userId + "�������豸��½��" + ServerModel.getIoSessionKey(user.ioSession) + "�������ߣ�");
			} catch (NoIpException e) {
				Debug.log(new String[] { "Server_User", "checkAnotherOnline" }, "�ҵ����û��Ѷ��ߣ���������");
				return false;
			}
			// ��ͻ��˷�����Ϣ
			byte[] messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.OFFLINE_REQ.getNumber(), objectBytes);
			ServerNetwork.instance.sendMessageToClient(user.ioSession, messageBytes);

			// ��ӵȴ��ظ�
			ServerModel.instance.addClientResponseListener(networkMessage.ioSession, NetworkMessage.getMessageID(messageBytes),
					messageBytes);

			return true;
		}
		return false;
	}

	/**
	 * 
	 * ��һ���˵�½�����û������µ�֪ͨ�Ļظ�
	 * 
	 * @param networkMessage
	 * @author Feng
	 * @throws NoIpException
	 */
	public void clientOfflineResponse(NetworkMessage networkMessage) throws NoIpException {
		ClientUser user = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
		Debug.log(new String[] { "Srever_User", "clientOfflineResponse" },
				"�ͻ��� " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " �ѽӵ������µ���Ϣ��������Ϊ������");
		user.userId = null;
		user.die = true;
	}

	/**
	 * ���������������
	 * 
	 * @param networkMessage
	 * @author wangfei
	 * @throws NoIpException
	 * @time 2015-03-21
	 */
	public void personalSettings(NetworkMessage networkMessage) throws NoIpException {
		try {
			Debug.log(new String[] { "Server_User", "personalSettings" },
					" ��  �û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  �ĸ��������¼�  �Ĵ���");

			PersonalSettingsMsg.PersonalSettingsReq personalSettingsObject = PersonalSettingsMsg.PersonalSettingsReq
					.parseFrom(networkMessage.getMessageObjectBytes());
			PersonalSettingsMsg.PersonalSettingsRsp.Builder personalSettingsBuilder = PersonalSettingsMsg.PersonalSettingsRsp
					.newBuilder();

			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);

			ClientUser clientUser = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);

			// ClientUser clientUser =
			// ServerModel.instance.getClientUserFromTable(networkMessage.ioSession.getRemoteAddress().toString());
			System.out.println("get userId that have been login:" + clientUser.userId);

			criteria.add(Restrictions.eq("userId", clientUser.userId));
			if (criteria.list().size() > 0) {
				User user = (User) criteria.list().get(0);
				// �޸��ǳ�
				if (personalSettingsObject.getUserName() != null && personalSettingsObject.getUserName() != "") {
					user.setUserName(personalSettingsObject.getUserName());
				}
				// �޸�����
				if (personalSettingsObject.getUserPassword() != null && personalSettingsObject.getUserPassword() != "") {
					user.setUserPassword(personalSettingsObject.getUserPassword());
				}

				// �޸�ͷ��
				if (personalSettingsObject.getHeadIndex() >= 1 && personalSettingsObject.getHeadIndex() <= 6) {
					BufferedImage image = null;
					user.setHeadIndex(personalSettingsObject.getHeadIndex());
					try {
						// ��Ĭ��ͷ���ļ��л�ȡͼƬ
						image = ImageIO.read(new File(ResourcePath.headDefaultPath + personalSettingsObject.getHeadIndex()
								+ ".png"));
						File file = new File(ResourcePath.headPath);
						// ��鱣��ͷ����ļ����Ƿ����
						if (!file.exists() && !file.isDirectory()) {
							// ��������� �򴴽��ļ���
							file.mkdir();
						}
						// �����ȡ��Ĭ��ͷ��ͷ���ļ���
						File saveFile = new File(ResourcePath.headPath + clientUser.userId + ".png");
						ImageIO.write(image, "png", saveFile);
						personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
					} catch (IOException e) {
						System.err.println("����ͷ��ͼƬʧ��");
						personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
						e.printStackTrace();
					}
				}

				// ���ݿ��޸��ǳƻ�����
				try {
					Transaction trans = session.beginTransaction();
					session.update(user);
					trans.commit();
					personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
				} catch (Exception e) {
					personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
					e.printStackTrace();
				}

			} else {
				// �û�������
				Debug.log(new String[] { "Server_User", "personalSettings" },
						"�û�" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  ���û�������!");
				personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			}
			session.close();

			// �ظ��ͻ���
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP.getNumber(),
							networkMessage.getMessageID(), personalSettingsBuilder.build().toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : ���������¼��� ��Protobuf�����л� " + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ " �İ�ʱ�쳣��");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Server_User : ���������¼��� " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " ���ذ�ʱ�쳣��");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �û��˳���¼
	 * 
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-25
	 */
	public void logout(NetworkMessage networkMessage) {
		try {
			ClientUser user = null;
			LogoutMsg.LogoutRsp.Builder logoutBuilder = null;
			try {
				user = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
				logoutBuilder = LogoutMsg.LogoutRsp.newBuilder();
				Debug.log(new String[] { "Srever_User", "logout" },
						"�ͻ��� " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " �˳���¼��������Ϊ������");
			} catch (NoIpException e) {
				
				e.printStackTrace();
			}

			user.userId = null;
			user.die = true;
			logoutBuilder.setResultCode(LogoutMsg.LogoutRsp.ResultCode.SUCCESS);

			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.LOGOUT_RSP.getNumber(), networkMessage.getMessageID(), logoutBuilder.build()
							.toByteArray()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
