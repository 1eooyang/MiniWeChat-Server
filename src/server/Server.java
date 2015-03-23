package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import protocol.ProtoHead;
import tools.DataTypeTranslater;

/**
 * ������������
 * @author Feng
 *
 */
public class Server {
	
	public static void main(String[] args) throws IOException {
		// ���������
		ServerNetwork.instance.init();
		
		// �����߼���
		ServerModel.instance.init();
	}

}
