package server;

import java.io.IOException;

import model.HibernateSessionFactory;
import model.User;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.protobuf.InvalidProtocolBufferException;


import protocol.ProtoHead;
import protocol.Data.UserData;
import protocol.Msg.GetUserInfoMsg;


/**
 * ���������µ��ӷ����� ����ͨѶ¼����¼�
 * 
 * @author wangfei
 *
 */
public class Server_Friend {
	public static Server_Friend instance = new Server_Friend();
	
	private Server_Friend(){
		
	}
	
	/**
	 * �����û�
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-23
	 */
	public void searchUser(NetworkMessage networkMessage){
		try {
			GetUserInfoMsg.GetUserInfoReq getUserInfoObject =GetUserInfoMsg.GetUserInfoReq.parseFrom(networkMessage.getMessageObjectBytes());
			GetUserInfoMsg.GetUserInfoRsp.Builder getUserInfoBuilder = GetUserInfoMsg.GetUserInfoRsp.newBuilder();
			//ClientUser clientUser = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession.getRemoteAddress().toString());
			
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", getUserInfoObject.getSearchUserId()));
			if(criteria.list().size()>0){
				User user = (User)criteria.list().get(0);
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS);
				
				UserData.UserItem.Builder userBuilder = UserData.UserItem.newBuilder();
				userBuilder.setUserId(user.getUserId());
				userBuilder.setUserName(user.getUserName());
				getUserInfoBuilder.setUserItem(0, userBuilder);
				
			}
			else{
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.FAIL);
			}
			//�ظ��ͻ���
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.GETUSERINFO_RSP.getNumber(),
							networkMessage.getMessageID(), getUserInfoBuilder.build().toByteArray()));
			
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}

	/**
	 * ��Ӻ���
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-24
	 */
	public void addFriend(NetworkMessage networkMessage){
		
	}
	
	/**
	 * ɾ������
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-24
	 */
	public void deleteFriend(NetworkMessage networkMessage){
		
	}

}
