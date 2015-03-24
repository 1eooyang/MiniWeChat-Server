package JUnit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.RegisterMsg;
import server.NetworkMessage;

import client.SocketClientTest;

/**
 * �Ը������ù��ܵĲ���
 * @author wangfei
 *
 */
public class TestPersonalSettings {
	String host = "192.168.45.17"; // Ҫ���ӵķ����IP��ַ
	int port = 8080; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�

	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;
	public SocketClientTest client;

	@Before
	public void init() throws UnknownHostException, IOException {
		client = new SocketClientTest();
		client.link();
	}

	private void link() throws IOException {
		socket = new Socket(host, port);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}
	
	
	/**
	 * ���Ը�������
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testPersonalSettings() throws IOException{
		
		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		
		byte[] resultBytes = client.testPersonalSettings_JUnit(randomData, randomData);
		PersonalSettingsMsg.PersonalSettingsRsp responseObject = 
				PersonalSettingsMsg.PersonalSettingsRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testPersonalSettings_JUnit(randomData, randomData);
		responseObject =PersonalSettingsMsg.PersonalSettingsRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL.toString());
	}
}
