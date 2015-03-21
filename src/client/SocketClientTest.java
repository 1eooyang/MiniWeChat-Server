package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import protocol.KeepAliveMsg;
import protocol.LoginMsg;
import protocol.PersonalSettingsMsg;
import protocol.ProtoHead;
import protocol.RegisterMsg;
import protocol.RegisterMsg.RegisterReq;
import server.NetworkMessage;
import tools.DataTypeTranslater;

public class SocketClientTest {

	public static final int HEAD_INT_SIZE = 4;
	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;

	String host = "192.168.45.34"; // Ҫ���ӵķ����IP��ַ
	// String host = "192.168.45.37"; // Ҫ���ӵķ����IP��ַ
	int port = 8080; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�

	public static void main(String args[]) throws IOException {
		new SocketClientTest();
	}

	public SocketClientTest() {
		// Ϊ�˼���������е��쳣��ֱ��������
		String host = "192.168.45.55"; // Ҫ���ӵķ����IP��ַ
		// String host = "192.168.45.37"; // Ҫ���ӵķ����IP��ַ
		int port = 8080; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�
		// �����˽�������
		try {
			// socket = new Socket(host, port);
			// �������Ӻ�Ϳ����������д������
			// Writer writer = new OutputStreamWriter(socket.getOutputStream());

			// ��
			KeepAliveMsg.KeepAliveSyncPacket.Builder keepAliveSyncBuilder = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
			keepAliveSyncBuilder.setA(1);
			keepAliveSyncBuilder.setB(true);
			keepAliveSyncBuilder.setC("wangxuanyi");
			byte[] responseByteArray = keepAliveSyncBuilder.build().toByteArray();

			byte[] messageBytes = new byte[HEAD_INT_SIZE * 2 + responseByteArray.length];
			System.out.println("length:" + messageBytes.length);

			int offset = 0;
			// 1. ����Size
			byte[] sizeBytes = DataTypeTranslater.intToByte(messageBytes.length);
			for (int i = 0; i < HEAD_INT_SIZE; i++)
				messageBytes[i + offset] = sizeBytes[i];
			offset += HEAD_INT_SIZE;

			// 2.�����ͷType
			byte[] typeBytes = DataTypeTranslater.intToByte(ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC.getNumber());
			for (int i = 0; i < HEAD_INT_SIZE; i++)
				messageBytes[i + offset] = typeBytes[i];
			offset += HEAD_INT_SIZE;

			// 3.����Protobuf����
			for (int i = 0; i < responseByteArray.length; i++)
				messageBytes[i + offset] = responseByteArray[i];

			System.out.println("Test:");
			// outputStream = socket.getOutputStream();

			byte[] second = new byte[messageBytes.length * 2];
			for (int i = 0; i < messageBytes.length * 2; i++)
				second[i] = messageBytes[i % messageBytes.length];

			// outputStream.write(second);
			// System.out.println("�������");

			// ����
			System.out.println("��ʼ����");
			// while (true) {
			// System.out.println("�յ�: " + readFromServer(socket));
			// }
			// InputStream in = socket.getInputStream();
			// byte[] byteArray = new byte[200];

			// for (int readTimes = 0; readTimes < 5; readTimes++) {
			// System.out.println(in.read(byteArray));
			// for (int i = 0; i < 4; i++)
			// System.out.println(in.read());
			// System.out.println(byteArray);
			// for (int i=0; i<byteArray.length; i++)
			// System.err.println(byteArray[i]);

			// int size = DataTypeTranslater.bytesToInt(byteArray, 0);
			// int reqOffset = 0;
			// do {
			//
			// ProtoHead.ENetworkMessage messageType =
			// ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(
			// byteArray, 4));
			// System.out.println("client���յ������ݳ��ȣ�" + size + " �ֽ�");
			// System.out.println("client���յ����������ͣ�" + messageType.toString());
			//
			// offset = HEAD_INT_SIZE * 2;
			// int contentLength = size - offset;
			// System.out.println("���ݵĳ���Ϊ��" + contentLength + " �ֽ�");
			// // byte[] contentbytes = new byte[contentLength];
			// // for (int i = 0; i < contentLength; i++)
			// // contentbytes[i] = byteArray[offset + i];
			//
			// KeepAliveMsg.KeepAliveSyncPacket packet =
			// KeepAliveMsg.KeepAliveSyncPacket.parseFrom(byteArray);
			// System.out.println("client�յ����������ǣ�" + "   " +
			// messageType.toString() + "  " + packet.getA() + "   "
			// + packet.getB() + "   " + packet.getC());
			//
			// System.out.println("�������");
			//
			// reqOffset += size;
			// size = DataTypeTranslater.bytesToInt(byteArray, reqOffset);
			// System.err.println(size);
			// } while (size > 0);
			//
			// // �ر���
			// // br.close();
			// }

			// in.close();
			// writer.close();
			// socket.close();
			// inputStream = socket.getInputStream();
//			socket = new Socket(host, port);
//			inputStream = socket.getInputStream();
//			outputStream = socket.getOutputStream();
			// ��ע��
			
			//testRegister();
			
			// ���½
			//testLogin();
			//���Ը�������
			testPersonalSettings();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		new Thread(new readThread()).start();
	}

	// ����������ظ�����

	public byte[] readFromServer(Socket socket) throws IOException {

//		inputStream = socket.getInputStream();
		byte[] byteArray = new byte[200];
		// System.out.println(in.read(byteArray));
		inputStream.read(byteArray);
		// System.out.println("client �յ�Server ������ �� " + byteArray);
		return byteArray;
	}
	
	public void writeToServer(byte[] arrayBytes) throws IOException {
//		outputStream = socket.getOutputStream();
		outputStream.write(arrayBytes);
//		outputStream.close();
	}

	/**
	 * ���ö��߳�
	 * 
	 * @author Feng
	 * 
	 */
	class readThread implements Runnable {
		@Override
		public void run() {
			try {
//				socket = new Socket(host, port);
				while (true) {
					Thread.sleep(1000);
					byte[] arrayBytes = readFromServer(socket);
					System.out.println("client �յ�Server ������ �� " + arrayBytes);

					System.out.println("size:" + DataTypeTranslater.bytesToInt(arrayBytes, 0));
					System.out.println("Type:"
							+ ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(arrayBytes, HEAD_INT_SIZE))
									.toString());
					
					//����ȥ
					writeToServer(arrayBytes);
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * ����ע�Ṧ��
	 * @author Feng
	 */
	public void testRegister(){
		RegisterMsg.RegisterReq.Builder builder = RegisterMsg.RegisterReq.newBuilder();
		builder.setUserId("a");
		builder.setUserPassword("aa");
		builder.setUserName("aaa");
		System.out.println("Start Test Register!");
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.REGISTER_REQ.getNumber(), builder.build().toByteArray());
//			outputStream = socket.getOutputStream();
			writeToServer(byteArray);
			
//			inputStream = socket.getInputStream();
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);
				
				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray, HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());
				
				if (type == ProtoHead.ENetworkMessage.REGISTER_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i=0; i<objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];
					
					RegisterMsg.RegisterRsp response = RegisterMsg.RegisterRsp.parseFrom(objBytes);
					
					System.out.println("Response : " + RegisterMsg.RegisterRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���Ե�½����
	 */
	public void testLogin(){

		LoginMsg.LoginReq.Builder builder = LoginMsg.LoginReq.newBuilder();
		builder.setUserId("aa");
		builder.setUserPassword("aa");
		System.out.println("Start Test Login!");
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.LOGIN_REQ.getNumber(), builder.build().toByteArray());
//			outputStream = socket.getOutputStream();
			writeToServer(byteArray);
			
//			inputStream = socket.getInputStream();
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);
				
				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray, HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());
				
				if (type == ProtoHead.ENetworkMessage.LOGIN_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i=0; i<objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];
					
					LoginMsg.LoginRsp response = LoginMsg.LoginRsp.parseFrom(objBytes);
					
					System.out.println("Response : " + LoginMsg.LoginRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void  testPersonalSettings(){
		PersonalSettingsMsg.PersonalSettingsReq.Builder builder = PersonalSettingsMsg.PersonalSettingsReq.newBuilder();
		builder.setUserId("Fuck");
		//builder.setUserName("ssss");
		//builder.setUserPassword("s123");
		builder.setHeadIndex(1);
		System.out.println("start personalSettings test!");
		try{
			Socket socket = new Socket(host,port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ.getNumber(), builder.build().toByteArray());
			writeToServer(byteArray);
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);
				
				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray, HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());
				
				if (type == ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i=0; i<objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];
					
					PersonalSettingsMsg.PersonalSettingsRsp response = PersonalSettingsMsg.PersonalSettingsRsp.parseFrom(objBytes);
					
					System.out.println("Response : " + PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
