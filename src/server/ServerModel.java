package server;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

/**
 * �����߼���
 * @author Feng
 *
 */
public class ServerModel {
	public static ServerModel instance;
	// �������
	public BlockingQueue<NetworkMessage> requestQueue = new NetworkMessageQueue();
	// �û��б�
	public HashSet<ClientUser> clientUserSet = new HashSet<ClientUser>();
	
	private ServerModel() {
		
	}
	
	public void init() {
		
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
				ClientRequest_Dispatcher.instance.dispatcher(networkMessage);
				
			}
		}
	}
	
	// ���ڶ�ʱ����������
	private class KeepAlivePacketSenser implements Runnable {

		@Override
		public void run() {
			for (ClientUser user : clientUserSet) {
				
			}
			// TODO Auto-generated method stub
			
		}
		
	}
}
