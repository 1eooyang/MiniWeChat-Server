package server;

import java.io.IOException;
import java.sql.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.buffer.IoBuffer;

import com.sun.org.apache.bcel.internal.generic.NEW;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;

/**
 * �����߼���
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
	// �������
	public LinkedBlockingQueue<NetworkMessage> requestQueue = new LinkedBlockingQueue<NetworkMessage>();
	// �û��б�
	public Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();
	//�����ͻ��˻ظ��ı�
	public Hashtable<Byte[], WaitClientResponse> waitClientRepTable = new Hashtable<Byte[], WaitClientResponse>();
	
	
	private ServerModel() {
		
	}
	/**
	 * ����һ�������MessageId
	 * @return
	 */
	public static byte[] createMessageId(){
		return DataTypeTranslater.floatToBytes((float)Math.random());
	}
	
	/**
	 *  ��ʼ��
	 */
	public void init() {
		// ��ʼ���߳�
		new Thread(new DealClientRequest()).start();
		new Thread(new KeepAlivePacketSenser()).start();
	}
	
	// ���ڴ����û�������߳�
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
				System.out.println("ServerModel: ServerModel����������л�ȡ��һ��Client���������󣬿�ʼ�����������������");
				if (networkMessage == null)
					continue;
				ClientRequest_Dispatcher.instance.dispatcher(networkMessage);
				
			}
		}
	}
	
	/**
	 * ���ڶ�ʱ����������
	 * @author Administrator
	 *
	 */
	private class KeepAlivePacketSenser implements Runnable {
		@Override
		public void run() {
			byte[] packetBytes = KeepAliveMsg.KeepAliveSyncPacket.newBuilder().build().toByteArray();
			try {
				// ����������
				byte[] messageBytes;
				
				IoBuffer responseIoBuffer;
				
				while (true) {
					Thread.sleep(KEEP_ALIVE_PACKET_TIME);
					
					messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.KeepAliveSync.getNumber(), packetBytes);
					responseIoBuffer = IoBuffer.allocate(messageBytes.length);
					responseIoBuffer.put(messageBytes);
					responseIoBuffer.flip();
					
					ClientUser user;
					String key;
					for (Iterator it = clientUserTable.keySet().iterator(); it.hasNext();) {
						key = (String)it.next();
						user = clientUserTable.get(key);
						// ���ϴ�û�лظ��ĸɵ������û�����ɾ��
						if (user.onLine == false) {
							System.out.println("Client �û���" + user.ioSession.getRemoteAddress() + "���ѵ��ߣ�����ɾ����");
							user.ioSession.close(true);
							
							clientUserTable.remove(key);
							continue;
						}
						
						// ����������֮ǰ�Ƚ�online��ΪFalse��ʾ�����ߣ�����Client�ظ�����������ΪTrue ����ʾ����
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
	 * ��ѯ"�ȴ�client�ظ�"�б�waitClientRepTable��������Ƿ��г�ʱ����Ŀ
	 * ��ʱ�Ľ����ط�
	 * @author Feng
	 *
	 */
	class CheckWaitClientResponseThread implements Runnable {
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
				for (Iterator iterator = waitClientRepTable.keySet().iterator(); iterator.hasNext();){
					key = (String)iterator.next();
					waitObj = waitClientRepTable.get(key);
					if ((currentTime - waitObj.time) > WAIT_CLIENT_RESPONSE_TIMEOUT) {
						// ��ʱ���ط�
						if (!clientUserTable.get(waitObj.ioSession.getRemoteAddress()).onLine) {
							// ������,ɾ��
							waitClientRepTable.remove(key);
							continue;
						}
						// �ط������õȴ�ʱ��
						ServerNetwork.instance.sendMessageToClient(waitObj.ioSession, waitObj.messageHasSent);
						waitObj.time = currentTime;
					}
				}
			}
		}
		
	}
}
