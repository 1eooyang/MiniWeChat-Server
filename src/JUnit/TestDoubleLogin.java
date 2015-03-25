package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.LoginMsg;
import protocol.Msg.OffLineMsg;
import server.NetworkMessage;
import tools.DataTypeTranslater;

import client.SocketClientTest;

/**
 * ���Եڶ����˵�½���һ���˱������������
 * 
 * @author Feng
 * 
 */
public class TestDoubleLogin {
	public SocketClientTest client1, client2;

	@Before
	public void init() throws UnknownHostException, IOException {
		client1 = new SocketClientTest();
		client1.link();
		client2 = new SocketClientTest();
		client2.link();
	}

	@Test
	public void test() throws UnknownHostException, IOException {
		// 1�ſͻ��˵�½
		byte[] resultBytes = client1.testLogin_JUint("a", "aa");
		LoginMsg.LoginRsp responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS.toString());

		// 2�ſͻ��˵�½
		resultBytes = client2.testLogin_JUint("a", "aa");
		responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS.toString());

		// ���1�ſͻ��˵��յ��ġ������ߡ���Ϣ
		for (int i = 0; i < 2; i++) {
			resultBytes = client1.readFromServer();
			// ������Ϣ������
			if (ProtoHead.ENetworkMessage.OFFLINE_SYNC != NetworkMessage.getMessageType(resultBytes))
				continue;
			// System.err.println(NetworkMessage.getMessageType(resultBytes));

			// �ظ�������
			client1.writeToServer(NetworkMessage.packMessage(ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE,
					NetworkMessage.getMessageID(resultBytes), new byte[]{}));

			// ����֪ͨ
			assertTrue(true);
			return;
		}
		assertFalse(true);
	}
}
