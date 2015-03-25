package JUnit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;

import protocol.Msg.GetUserInfoMsg;
import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.RegisterMsg;
import server.NetworkMessage;

import client.SocketClientTest;

/**
 * �Ի�ȡ�û���Ϣ�Ĳ���
 * @author wangfei
 *
 */
public class TestGetUserInfo {
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
	 * ���ȡ�û���Ϣ
	 * @author wangfei
	 * @throws IOException
	 */
	public void testGetUserInfo() throws IOException{
		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		
		byte[] resultBytes = client.testGetUserInfo_JUnit(randomData);
		GetUserInfoMsg.GetUserInfoRsp responseObject = 
				GetUserInfoMsg.GetUserInfoRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testGetUserInfo_JUnit(randomData);
		responseObject =GetUserInfoMsg.GetUserInfoRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), GetUserInfoMsg.GetUserInfoRsp.ResultCode.FAIL.toString());
	}

}
