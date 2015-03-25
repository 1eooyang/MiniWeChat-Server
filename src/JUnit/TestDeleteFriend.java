package JUnit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;

import protocol.Msg.AddFriendMsg;
import protocol.Msg.DeleteFriendMsg;
import server.NetworkMessage;

import client.SocketClientTest;

/**
 * ��ɾ�����ѵĲ���
 * @author wangfei
 *
 */
public class TestDeleteFriend {
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
	 * ����ɾ������
	 * @author wangfei
	 * @throws IOException
	 */
	public void testDeleteFriend() throws IOException{
		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		
		byte[] resultBytes = client.testDeleteFriend_JUnit(randomData);
		DeleteFriendMsg.DeleteFriendRsp responseObject = 
				DeleteFriendMsg.DeleteFriendRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), DeleteFriendMsg.DeleteFriendRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testDeleteFriend_JUnit(randomData);
		responseObject =DeleteFriendMsg.DeleteFriendRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), DeleteFriendMsg.DeleteFriendRsp.ResultCode.FAIL.toString());
	}

}
