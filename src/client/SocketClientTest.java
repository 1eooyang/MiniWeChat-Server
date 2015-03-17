package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SocketClientTest {
	public static void main(String args[]) {
		// Ϊ�˼���������е��쳣��ֱ��������
		String host = "192.168.45.55"; // Ҫ���ӵķ����IP��ַ
		int port = 8080; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�
		// �����˽�������
		try {
			Socket socket = new Socket(host, port);
			// �������Ӻ�Ϳ����������д������
			Writer writer = new OutputStreamWriter(socket.getOutputStream());
			writer.write("Hello Server.");
			writer.flush();// д���Ҫ�ǵ�flush

			// ����
			System.out.println("��ʼ����");
			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(socket.getInputStream(), "UTF-8"));
			// System.out.println("�ӷ������յ� :" + br.readLine());

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			char[] aaa = new char[100];
//			System.out.println(in.readLine());
			System.out.println(in.read(aaa));
			// for (int i = 0; i < 4; i++)
			// System.out.println(in.read());
			System.out.println(aaa);
			in.close();

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
