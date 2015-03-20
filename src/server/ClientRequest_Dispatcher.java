package server;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

/**
 *  ��switch��������ַ�
 * @author Feng
 *
 */
public class ClientRequest_Dispatcher {
	public static ClientRequest_Dispatcher instance = new ClientRequest_Dispatcher();

	private ClientRequest_Dispatcher() {

	}

	/**
	 *  ������������ͷ������ͬ�Ĵ�����
	 * @param networkMessage
	 * @author Feng
	 */
	public void dispatcher(NetworkMessage networkMessage) {
//		System.out.println("IP" + networkMessage.ioSession.getRemoteAddress());
		System.out.println("ClientRequest_Dispatcher: Client������������ " + networkMessage.getMessageType().toString());
		
		switch (networkMessage.getMessageType().getNumber()) {
		// Client�ظ�������
		case ProtoHead.ENetworkMessage.KeepAliveSync_VALUE:
			Server_User.instance.KeepAlive(networkMessage);
			break;
		case ProtoHead.ENetworkMessage.RegisterReq_VALUE:

			break;
		case ProtoHead.ENetworkMessage.LoginReq_VALUE:

			break;

		default:
			break;
		}
	}
}
