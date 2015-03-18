package server;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

// ��switch��������ַ�
public class ClientRequest_Dispatcher {
	public static ClientRequest_Dispatcher instance = new ClientRequest_Dispatcher();

	private ClientRequest_Dispatcher() {

	}

	// ������������ͷ������ͬ�Ĵ�����
	public void dispatcher(NetworkMessage networkMessage) {
		switch (networkMessage.getMessageType().getNumber()) {
		case ProtoHead.ENetworkMessage.KeepAliveSync_VALUE:
			try {
				KeepAliveMsg.KeepAliveSyncPacket packet = KeepAliveMsg.KeepAliveSyncPacket.parseFrom(networkMessage
						.getMessageObjectBytes());

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
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
