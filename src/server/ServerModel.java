package server;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.buffer.IoBuffer;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

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
	public HashSet<ClientUser> clientUserSet = new HashSet<ClientUser>();
	
	private ServerModel() {
		
	}
	
	// ��ʼ��
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
	
	// ���ڶ�ʱ����������
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
					
					for (ClientUser user : clientUserSet) {
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
