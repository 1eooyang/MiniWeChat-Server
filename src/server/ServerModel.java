package server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.buffer.IoBuffer;

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
	public static final int KeepAlivePacketTime = 5000;
	
	public static ServerModel instance = new ServerModel();
	// �������
	public LinkedBlockingQueue<NetworkMessage> requestQueue = new LinkedBlockingQueue<NetworkMessage>();
	// �û��б�
	public Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();
	
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
				System.out.println("ServerModel����������л�ȡ��һ��Client���������󣬿�ʼ�����������������");
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
				byte[] messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.KeepAliveSync.getNumber(), packetBytes);
				
				IoBuffer responseIoBuffer = IoBuffer.allocate(messageBytes.length);
				responseIoBuffer.put(messageBytes);
				responseIoBuffer.flip();
				
				while (true) {
					Thread.sleep(KeepAlivePacketTime);
					
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
						user.ioSession.write(messageBytes);
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
}
