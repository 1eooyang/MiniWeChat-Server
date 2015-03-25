package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.http.HttpServerCodec;
import org.apache.mina.http.api.DefaultHttpResponse;
import org.apache.mina.http.api.HttpRequest;
import org.apache.mina.http.api.HttpResponse;
import org.apache.mina.http.api.HttpStatus;
import org.apache.mina.http.api.HttpVersion;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class HttpServer {

	public static void main(String[] args) throws IOException {
		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.getFilterChain().addLast("codec", new HttpServerCodec());
		acceptor.setHandler(new HttpServerHandle());
		acceptor.bind(new InetSocketAddress(8080));
	}
}

class HttpServerHandle extends IoHandlerAdapter {

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		Debug.log("HttpServer", "exceptionCaught");
		cause.printStackTrace();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		Debug.log("HttpServer", "messageReceived");

		if (message instanceof HttpRequest) {

			// ���󣬽�����������ת����HttpRequest����
			HttpRequest request = (HttpRequest) message;

			// ��ȡ�������
			String name = request.getParameter("name");
			name = URLDecoder.decode(name, "UTF-8");

			// ��ӦHTML
			// byte[] responseBytes = getHelloResponse(name);
			byte[] responseBytes = getImageResonse(name);

			int contentLength = responseBytes.length;

			// ����HttpResponse����HttpResponseֻ������Ӧ��status line��header����
//			headers.put("Content-Type", "text/html; charset=utf-8");
			HttpResponse response = getImageHttpResponse(contentLength);

			// ��ӦBODY
			IoBuffer responseIoBuffer = IoBuffer.allocate(contentLength);
			responseIoBuffer.put(responseBytes);
			responseIoBuffer.flip();

			session.write(response); // ��Ӧ��status line��header����
			session.write(responseIoBuffer); // ��Ӧbody����
		}
	}

	public byte[] getHelloResponse(String name) throws UnsupportedEncodingException {
		// ��ӦHTML
		String responseHtml = "<html><body>Hello, " + name + "</body></html>";
		return responseHtml.getBytes("UTF-8");
	}

	public byte[] getImageResonse(String name) throws IOException {
//		String filePath = "D:/2.jpg";
		String filePath = "D:/" + name;
		System.err.println("FilePath : " + filePath);
		
		File file = new File(filePath);
		long fileSize = file.length();

		FileInputStream fi = new FileInputStream(file);
		byte[] buffer = new byte[(int) fileSize];
		
		int offset = 0;
		int numRead = 0;
		while (offset < buffer.length && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
			offset += numRead;
		}
		// ȷ���������ݾ�����ȡ
		if (offset != buffer.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		fi.close();
		return buffer;
	}
	
	public HttpResponse getImageHttpResponse(int length){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "image/gif; charset=utf-8");
		headers.put("Content-Length", Integer.toString(length));
		return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SUCCESS_OK, headers);
		
	}
}
