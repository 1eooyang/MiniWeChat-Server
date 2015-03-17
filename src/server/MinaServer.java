package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class MinaServer {
	public static void main(String[] args) {
		// ��ʾIP��ַ
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			System.out.println("IP��ַ��" + addr.getHostAddress().toString());
			System.out.println("�������ƣ�" + addr.getHostName().toString());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		// ����ServerScoket
		SocketAcceptor acceptor = new NioSocketAcceptor();
		// ���ô��䷽ʽ���������óɶ�����ģʽ�����кܶ�����ͺ������彲��
		DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
		ProtocolCodecFilter filter = new ProtocolCodecFilter(new ObjectSerializationCodecFactory());
		chain.addLast("objectFilter", filter);
		// �����Ϣ����

		acceptor.setHandler(new MinaServerHanlder());
		// ����������
		int bindPort = 9988;
		try {
			acceptor.bind(new InetSocketAddress(bindPort));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
