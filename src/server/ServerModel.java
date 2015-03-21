package server;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.sun.org.apache.bcel.internal.generic.NEW;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
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
	// �������û���Ϣ��
	private Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();
	// �����ͻ��˻ظ��ı�
	private Hashtable<byte[], WaitClientResponse> waitClientRepTable = new Hashtable<byte[], WaitClientResponse>();

	private ServerModel() {

	}

	/**
	 * ����һ�������MessageId
	 * @author Feng
	 * @return
	 */
	public static byte[] createMessageId() {
		return DataTypeTranslater.floatToBytes((float) Math.random());
	}

	/**
	 * ��ʼ��
	 * @author Feng
	 */
	public void init() {
		// ��ʼ���߳�
		new Thread(new DealClientRequest()).start();
		new Thread(new KeepAlivePacketSenser()).start();
		new Thread(new CheckWaitClientResponseThread()).start();
	}

	/**
	 *  ���ͻ��������б��м���һ������
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
	 * @param key
	 * @param clientUser
	 * @author Feng
	 */
	public void addClientUserToTable(String key, ClientUser clientUser) {
		clientUserTable.put(key, clientUser);
	}
	
	/**
	 * �ӡ��������û���Ϣ���л�ȡ�û�
	 * @param key
	 * @return ClientUser
	 * @author Feng
	 */
	public ClientUser getClientUserFromTable(String key) {
		return clientUserTable.get(key);
	}
	
	/**
	 * ���һ���ȴ��ͻ��˻ظ��ļ�������������ͻ��˷�����Ϣ��Ҫ��ͻ��˻ظ���
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
//			byte[] packetBytes = KeepAliveMsg.KeepAliveSyncPacket.newBuilder().build().toByteArray();
			KeepAliveMsg.KeepAliveSyncPacket.Builder packet = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
//			packet.setA(123);
//			packet.setB(true);
			packet.setC("Fuck");
			byte[] packetBytes = packet.build().toByteArray();
			try {
				// ����������
				byte[] messageBytes;

				IoBuffer responseIoBuffer;

				while (true) {
					Thread.sleep(KEEP_ALIVE_PACKET_TIME);

					messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC.getNumber(), packetBytes);
					responseIoBuffer = IoBuffer.allocate(messageBytes.length);
					responseIoBuffer.put(messageBytes);
					responseIoBuffer.flip();

					ClientUser user;
					String key;
					Debug.log("ServerModel", "��ʼ�µ�һ�����������ͣ����� " + clientUserTable.size() + " ���û�!");
					for (Iterator it = clientUserTable.keySet().iterator(); it.hasNext();) {
						if (it == null)
							continue;
						Debug.log("ServerModel", "���뷢������ѭ��!");
						key = (String) it.next();
						user = clientUserTable.get(key);

						// ���ϴ�û�лظ��ĸɵ������û�����ɾ��
						if (user.onLine == false) {
							Debug.log("ServerModel", "Client �û���" + user.ioSession.getRemoteAddress() + "���ѵ��ߣ�����ɾ����");
//							user.ioSession.close(true);

//							user.ioSession.close(true);
							clientUserTable.remove(key);
							continue;
						}

						// ����������֮ǰ�Ƚ�online��ΪFalse��ʾ�����ߣ�����Client�ظ�����������ΪTrue
						// ����ʾ����
						Debug.log("ServerModel", "��Client " + user.ioSession.getRemoteAddress() + " ����������");
						user.onLine = false;
						user.ioSession.write(responseIoBuffer);
					}
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
				for (Iterator iterator = waitClientRepTable.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
					waitObj = waitClientRepTable.get(key);
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
