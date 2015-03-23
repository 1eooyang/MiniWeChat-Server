package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.KeepAliveMsg;
import protocol.LoginMsg;
import protocol.PersonalSettingsMsg;
import protocol.ProtoHead;
import protocol.RegisterMsg;
import protocol.RegisterMsg.RegisterReq;
import server.NetworkMessage;
import server.ServerModel;
import tools.DataTypeTranslater;
import tools.Debug;

public class SocketClientTest {

	public static final int HEAD_INT_SIZE = 4;
	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;

	// String host = "192.168.45.11"; // Ҫ���ӵķ����IP��ַ
	String host = "192.168.1.103"; // Ҫ���ӵķ����IP��ַ
	int port = 8080; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�

	public static void main(String args[]) throws IOException {
		new SocketClientTest();
	}

	public SocketClientTest() throws UnknownHostException, IOException {
		// Ϊ�˼���������е��쳣��ֱ��������
		String host = "192.168.45.55"; // Ҫ���ӵķ����IP��ַ
		// String host = "192.168.45.37"; // Ҫ���ӵķ����IP��ַ
		int port = 8080; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�
		// �����˽�������
		// ������
		// testKeepAlive();
//		socket = new Socket(host, port);
//		inputStream = socket.getInputStream();
//		outputStream = socket.getOutputStream();

		// ��ע��
		testRegister();
		
		// ���½
		// testLogin();
		// ���Ը�������
		// testPersonalSettings();

		// new Thread(new readThread()).start();
	}

	// ����������ظ�����

	public byte[] readFromServer(Socket socket) throws IOException {

		// inputStream = socket.getInputStream();
		byte[] byteArray = new byte[200];
		// System.out.println(in.read(byteArray));
		inputStream.read(byteArray);
		// System.out.println("client �յ�Server ������ �� " + byteArray);
//		inputStream.close();
		return byteArray;
	}

	public void writeToServer(byte[] arrayBytes) throws IOException {
		// outputStream = socket.getOutputStream();
		outputStream.write(arrayBytes);
		// outputStream.close();
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
				// socket = new Socket(host, port);
				while (true) {
					Thread.sleep(1000);
					byte[] arrayBytes = readFromServer(socket);
					System.out.println("client �յ�Server ������ �� " + arrayBytes);

					System.out.println("size:" + DataTypeTranslater.bytesToInt(arrayBytes, 0));
					System.out.println("Type:"
							+ ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(arrayBytes, HEAD_INT_SIZE))
									.toString());

					// ����ȥ
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
	 * ������������
	 */
	public void testKeepAlive() {
		System.out.println("Start Test KeepAliveSyc!");
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			byte[] byteArray1, byteArray2;
			// outputStream = socket.getOutputStream();
			// writeToServer(byteArray);

			// inputStream = socket.getInputStream();
			while (true) {
				byteArray1 = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray1, 0);
				System.out.println("size: " + size);

				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray1,
						HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());

				if (type == ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC) {
					byteArray2 = new byte[size];
					for (int i = 0; i < size; i++)
						byteArray2[i] = byteArray1[i];

					Debug.log("�ظ�������");
					writeToServer(byteArray2);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����ע�Ṧ��
	 * 
	 * @author Feng
	 */
	public void testRegister() {
		RegisterMsg.RegisterReq.Builder builder = RegisterMsg.RegisterReq.newBuilder();
		builder.setUserId("a");
		builder.setUserPassword("aa");
		builder.setUserName("aaa");
		System.out.println("Start Test Register!");
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.REGISTER_REQ.getNumber(), builder.build()
					.toByteArray());
			System.out.println("MessageID : " + NetworkMessage.getMessageID(byteArray));
			writeToServer(byteArray);
			
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);

				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray,
						HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());

				if (type == ProtoHead.ENetworkMessage.REGISTER_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i = 0; i < objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];

					RegisterMsg.RegisterRsp response = RegisterMsg.RegisterRsp.parseFrom(objBytes);

					System.out.println("Response : "
							+ RegisterMsg.RegisterRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
					System.out.println("MessageID : " + NetworkMessage.getMessageID(byteArray));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���Ե�½����
	 */
	public void testLogin() {

		LoginMsg.LoginReq.Builder builder = LoginMsg.LoginReq.newBuilder();
		builder.setUserId("aa");
		builder.setUserPassword("aa");
		System.out.println("Start Test Login!");
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.LOGIN_REQ.getNumber(), builder.build()
					.toByteArray());
			// outputStream = socket.getOutputStream();
			writeToServer(byteArray);

			// inputStream = socket.getInputStream();
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);

				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray,
						HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());

				if (type == ProtoHead.ENetworkMessage.LOGIN_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i = 0; i < objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];

					LoginMsg.LoginRsp response = LoginMsg.LoginRsp.parseFrom(objBytes);

					System.out
							.println("Response : " + LoginMsg.LoginRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���Ը������ù���
	 * @author WangFei
	 */
	public void testPersonalSettings() {
		PersonalSettingsMsg.PersonalSettingsReq.Builder builder = PersonalSettingsMsg.PersonalSettingsReq.newBuilder();
		//builder.setUserId("Fuck");
		builder.setUserName("ssss");
		// builder.setUserPassword("s123");
		builder.setHeadIndex(1);
		System.out.println("start personalSettings test!");
		try {
			Socket socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ.getNumber(), builder
					.build().toByteArray());
			writeToServer(byteArray);
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);

				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray,
						HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());

				if (type == ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i = 0; i < objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];

					PersonalSettingsMsg.PersonalSettingsRsp response = PersonalSettingsMsg.PersonalSettingsRsp
							.parseFrom(objBytes);

					System.out.println("Response : "
							+ PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
