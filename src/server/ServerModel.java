package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import model.HibernateSessionFactory;
import model.User;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.sun.org.apache.bcel.internal.generic.NEW;

import exception.NoIpException;

import protocol.ProtoHead;
import protocol.Msg.KeepAliveMsg;
import tools.DataTypeTranslater;
import tools.Debug;

/**
 * �����߼���
 * 
 * @author Feng
 * 
 */
public class ServerModel {
	// ���������(5��)
	public static final int KEEP_ALIVE_PACKET_TIME = 5000;
	// ��ѯ"�ȴ�client�ظ�"�б�waitClientRepTable���ļ��
	public static final int CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME = 1000;
	// ��ѯ"�ȴ�client�ظ�"�б�waitClientRepTable���ĳ�ʱʱ��
	public static final long WAIT_CLIENT_RESPONSE_TIMEOUT = 3000;

	public static ServerModel instance = new ServerModel();
	// Client�������
	private LinkedBlockingQueue<NetworkMessage> requestQueue = new LinkedBlockingQueue<NetworkMessage>();
	// �������û���Ϣ��(Key ΪIoSession.getRemoteAddress().toString)
	private Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();
	// �����ͻ��˻ظ��ı�
	private Hashtable<byte[], WaitClientResponse> waitClientRepTable = new Hashtable<byte[], WaitClientResponse>();

	private ServerModel() {
//		System.out.println("Fuycj");
//		Session session = HibernateSessionFactory.getSession();
//		Criteria criteria = session.createCriteria(User.class);
//		criteria.add(Restrictions.eq("userId", "a"));
//
//		System.out.println(1);
//		User user = (User) criteria.list().get(0);
//		System.out.println(2);
//		System.out.println(user.getFriends().get(0).getUserId());
//		System.out.println(3);
//		System.out.println(user.getFriends().get(1).getUserName());
	}

	/**
	 * ����һ�������MessageId
	 * 
	 * @author Feng
	 * @return
	 */
	public static byte[] createMessageId() {
		return DataTypeTranslater.floatToBytes((float) Math.random());
	}

	/**
	 * ��ʼ��
	 * 
	 * @author Feng
	 */
	public void init() {
		// ��ʼ���߳�
		new Thread(new DealClientRequest()).start();
		new Thread(new KeepAlivePacketSenser()).start();
		new Thread(new CheckWaitClientResponseThread()).start();
	}

	/**
	 * ���ͻ��������б��м���һ������
	 * 
	 * @param ioSession
	 * @param arrayBytes
	 * @author Feng
	 * @throws InterruptedException
	 */
	public void addClientRequestToQueue(IoSession ioSession, byte[] byteArray) throws InterruptedException {
		requestQueue.put(new NetworkMessage(ioSession, byteArray));
	}

	/**
	 * �����������û���Ϣ�������һ�����û�
	 * 
	 * @param key
	 * @param clientUser
	 * @author Feng
	 * @throws NoIpException 
	 */
	public void addClientUserToTable(IoSession ioSession, ClientUser clientUser) throws NoIpException {
		clientUser.onLine = true;
		clientUserTable.put(getIoSessionKey(ioSession), clientUser);
	}
	
	/**
	 * ��iosession����Key
	 * @param ioSession
	 * @return
	 * @throws NoIpException 
	 */
	public static String getIoSessionKey(IoSession ioSession) throws NoIpException {
//		System.err.println("1.2  " + (ioSession.getRemoteAddress() == null));
		if (ioSession.getRemoteAddress() == null)
			throw new NoIpException();
		return ((InetSocketAddress)ioSession.getRemoteAddress()).getAddress().toString() + ":" + ((InetSocketAddress)ioSession.getRemoteAddress()).getPort();
	}
	
	/**
	 * �ӡ��������û���Ϣ���л�ȡ�û�
	 * 
	 * @param key
	 * @return ClientUser
	 * @author Feng
	 */
	public ClientUser getClientUserFromTable(String key) {
		return clientUserTable.get(key);
	}
	
	public ClientUser getClientUserFromTable(IoSession ioSession) throws NoIpException {
		return getClientUserFromTable(getIoSessionKey(ioSession));
	}

	/**
	 * ����userId�ӡ��������û���Ϣ���л�ȡ�û�
	 * @param userId
	 * @return
	 */
	public ClientUser getClientUserByUserId(String userId) {
		Iterator iterator = clientUserTable.keySet().iterator();
		String key;
		ClientUser user;
		
		while (iterator.hasNext()) {

			key = iterator.next().toString();
			
			if (!clientUserTable.containsKey(key))
				continue;
			
			user = clientUserTable.get(key);
			
			if (user.userId == null)
				continue;
			
			if (user.userId.equals(userId))
				return user;
		}
		return null;
	}
	
	/**
	 * �������û���Ϣ��ɾ��һ���û�
	 * @param key
	 */
	synchronized
	public void removeClientUserFromTable(String key) {
		clientUserTable.remove(key);
	}
	
	/**
	 * ���һ���ȴ��ͻ��˻ظ��ļ�������������ͻ��˷�����Ϣ��Ҫ��ͻ��˻ظ���
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public void addClientResponseListener(IoSession ioSession, byte[] key, byte[] messageHasSent) {
		WaitClientResponse waitClientResponse = new WaitClientResponse(ioSession, messageHasSent);
		waitClientResponse.time = new Date().getTime();
		// ���뵽���ȴ��ظ����У���CheckWaitClientResponseThread �߳̽�����ѯ
		waitClientRepTable.put(key, waitClientResponse);
	}

	/**
	 * ɾ��һ���ȴ��ͻ��˻ظ��ļ�������������ͻ��˷�����Ϣ��Ҫ��ͻ��˻ظ���
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public void removeClientResponseListener(byte[] key) {
		waitClientRepTable.remove(key);
	}

	/**
	 * ����һ���ȴ��ͻ��˻ظ��ļ�������������ͻ��˷�����Ϣ��Ҫ��ͻ��˻ظ���
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public WaitClientResponse getClientResponseListener(byte[] key) {
		return waitClientRepTable.get(key);
	}

	/**
	 * ���ڴ����û�������߳�
	 * 
	 * @author Feng
	 * 
	 */
	private class DealClientRequest implements Runnable {
		@Override
		public void run() {
			NetworkMessage networkMessage = null;
			// ѭ����ȡ�µ���������ʽ
			while (true) {
				try {
					networkMessage = requestQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Debug.log("ServerModel", "ServerModel����������л�ȡ��һ��Client���������󣬿�ʼ�������������ClientRequest_Dispatcher����");
				if (networkMessage == null)
					continue;
				ClientRequest_Dispatcher.instance.dispatcher(networkMessage);

			}
		}
	}

	/**
	 * ���ڶ�ʱ����������
	 * 
	 * @author Feng
	 * 
	 */
	private class KeepAlivePacketSenser implements Runnable {
		@Override
		public void run() {
			KeepAliveMsg.KeepAliveSyncPacket.Builder packet = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
			byte[] packetBytes = packet.build().toByteArray();
			// ����������
			byte[] messageBytes;

			IoBuffer responseIoBuffer;
			ArrayList<String> keyIterators;

			while (true) {
				try {
					Thread.sleep(KEEP_ALIVE_PACKET_TIME);

					ClientUser user;
					Iterator iterator = clientUserTable.keySet().iterator();
					String key;
					
					Debug.log("ServerModel", "��ʼ�µ�һ�����������ͣ����� " + clientUserTable.size() + " ���û�!");
					while (iterator.hasNext()) {
//					for (String key : keyIterators) {
						Debug.log("ServerModel", "���뷢������ѭ��!");
						
						key = iterator.next().toString();
						
						if (!clientUserTable.containsKey(key))
							continue;
						user = clientUserTable.get(key);
						
						// ��������ɾ��    ;   ���ϴ�û�лظ��ĸɵ������û�����ɾ��
						if (user.die || user.onLine == false) {
							Debug.log("ServerModel", "Client �û���" + user.ioSession.getRemoteAddress() + "���ѵ��ߣ�����ɾ����");
							// user.ioSession.close(true);
							iterator.remove();
							continue;
						}
						
						messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC.getNumber(), packetBytes);
						responseIoBuffer = IoBuffer.allocate(messageBytes.length);
						responseIoBuffer.put(messageBytes);
						responseIoBuffer.flip();

						// ����������֮ǰ�Ƚ�online��ΪFalse��ʾ�����ߣ�����Client�ظ�����������ΪTrue
						// ����ʾ����
						Debug.log("ServerModel", "��Client " + user.ioSession.getRemoteAddress() + " ����������");
						user.onLine = false;
						user.ioSession.write(responseIoBuffer);
					}
				} catch (IOException e) {
					System.err.println("�����������߳��쳣!");
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.err.println("�����������߳��쳣! -----˯��ģ��");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * ��ѯ"�ȴ�client�ظ�"�б�waitClientRepTable��������Ƿ��г�ʱ����Ŀ ��ʱ�Ľ����ط�
	 * 
	 * @author Feng
	 * 
	 */
	private class CheckWaitClientResponseThread implements Runnable {
		@Override
		public void run() {
			long currentTime;
			WaitClientResponse waitObj;
			String key;
			while (true) {
				currentTime = new java.util.Date().getTime();
				// ÿ��CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIMEʱ����ѯһ��
				try {
					Thread.sleep(CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// ��ÿ���û����м��
				Iterator iterator = waitClientRepTable.keySet().iterator();
				while (iterator.hasNext()) {
					key = iterator.next().toString();
					waitObj = waitClientRepTable.get(key);
					if (waitObj == null)
						continue;
					
					if ((currentTime - waitObj.time) > WAIT_CLIENT_RESPONSE_TIMEOUT) {
						// ��ʱ���ط�
						Debug.log("ServerModel", "�ȴ��ͻ���" + waitObj.ioSession.getRemoteAddress() + " �ظ���ʱ��");
						System.out.println("ServerModel : �ȴ��ͻ���" + waitObj.ioSession.getRemoteAddress() + " �ظ���ʱ��");
						if (!clientUserTable.get(waitObj.ioSession.getRemoteAddress()).onLine) {
							// ������,ɾ��
							Debug.log("ServerModel", "�ͻ���" + waitObj.ioSession.getRemoteAddress() + " �Ѷ��ߣ����ӱ����Ƴ���");
							waitClientRepTable.remove(key);
							continue;
						}
						// �ط������õȴ�ʱ��
						Debug.log("ServerModel", "�ͻ���" + waitObj.ioSession.getRemoteAddress() + " ���ߣ���Ϣ���ط���");
						ServerNetwork.instance.sendMessageToClient(waitObj.ioSession, waitObj.messageHasSent);
						waitObj.time = currentTime;
					}
				}
			}
		}
	}
}
