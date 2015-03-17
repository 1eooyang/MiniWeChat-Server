package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;

public class SocketClientTest {

	public static final int HEAD_INT_SIZE = 4;

	public static void main(String args[]) throws IOException {
		// byte[] bs = DateTypeTranslater.intToByte(10);
		// for (byte b : bs)
		// System.out.println(b);
		// System.err.println(DateTypeTranslater.bytesToInt(bs, 0));

		// Ϊ�˼���������е��쳣��ֱ��������
		String host = "192.168.45.55"; // Ҫ���ӵķ����IP��ַ
		int port = 8080; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�
		// �����˽�������
		try {
			Socket socket = new Socket(host, port);
			// �������Ӻ�Ϳ����������д������
			Writer writer = new OutputStreamWriter(socket.getOutputStream());

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
			byte[] typeBytes = DataTypeTranslater.intToByte(ProtoHead.ENetworkMessage.LoginReq.getNumber());
			for (int i = 0; i < HEAD_INT_SIZE; i++)
				messageBytes[i + offset] = typeBytes[i];
			offset += HEAD_INT_SIZE;

			// 3.����Protobuf����
			for (int i = 0; i < responseByteArray.length; i++)
				messageBytes[i + offset] = responseByteArray[i];

			System.out.println("Test:");
//			for (byte b : messageBytes)
//				System.out.print(b + "  ");
			// 4.byte[] ת char[]
			// Charset cs = Charset.forName("UTF-8");
			// ByteBuffer bb = ByteBuffer.allocate(messageBytes.length);
			// bb.put(messageBytes);
			// bb.flip();
			// CharBuffer cb = cs.decode(bb);
			// char[] messageChars = cb.array();
			//
			// // writer.write("Hello Server.");
			// writer.write(messageChars);
			// writer.
			// writer.flush();// д���Ҫ�ǵ�flush
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(messageBytes);
			System.out.println("�������");

			// ����
			System.out.println("��ʼ����");
			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(socket.getInputStream(), "UTF-8"));
			// System.out.println("�ӷ������յ� :" + br.readLine());

			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(socket.getInputStream()));
			InputStream in = socket.getInputStream();
			byte[] byteArray = new byte[200];

			System.out.println(in.read(byteArray));
			// for (int i = 0; i < 4; i++)
			// System.out.println(in.read());
//			System.out.println(byteArray);
			in.close();

			int size = DataTypeTranslater.bytesToInt(byteArray, 0);
			ProtoHead.ENetworkMessage messageType = ProtoHead.ENetworkMessage
					.valueOf(DataTypeTranslater.bytesToInt(byteArray, 4));
			System.out.println("client���յ������ݳ��ȣ�" + size + " �ֽ�");
			System.out.println("client���յ����������ͣ�" + messageType.toString());

			offset = HEAD_INT_SIZE * 2;
			int contentLength = size - offset;
			System.out.println("���ݵĳ���Ϊ��" + contentLength + " �ֽ�");
			byte[] contentbytes = new byte[contentLength];
			for (int i=0; i<contentLength; i++)
				contentbytes[i] = byteArray[offset + i];
			
			KeepAliveMsg.KeepAliveSyncPacket packet = KeepAliveMsg.KeepAliveSyncPacket.parseFrom(contentbytes);
			System.out.println("client�յ����������ǣ�" + "   " + messageType.toString() + "  " + packet.getA() + "   " + packet.getB()
					+ "   " + packet.getC());

			System.out.println("�������");

			// �ر���
			// br.close();

			writer.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
