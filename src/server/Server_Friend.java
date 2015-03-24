package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.HibernateSessionFactory;
import model.User;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.google.protobuf.InvalidProtocolBufferException;


import protocol.ProtoHead;
import protocol.Data.UserData;
import protocol.Msg.AddFriendMsg;
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
	public void getUserInfo(NetworkMessage networkMessage){
		try {
			GetUserInfoMsg.GetUserInfoReq getUserInfoObject =GetUserInfoMsg.GetUserInfoReq.parseFrom(networkMessage.getMessageObjectBytes());
			GetUserInfoMsg.GetUserInfoRsp.Builder getUserInfoBuilder = GetUserInfoMsg.GetUserInfoRsp.newBuilder();
			
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", getUserInfoObject.getTargetUserId()));
			if(criteria.list().size()>0){
				//��֧��ģ������ ���������������� ֻ������һ�����
				User user = (User)criteria.list().get(0);
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS);
				
				UserData.UserItem.Builder userBuilder = UserData.UserItem.newBuilder();
				userBuilder.setUserId(user.getUserId());
				userBuilder.setUserName(user.getUserName());
				getUserInfoBuilder.setUserItem(userBuilder);
				
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
		try {
			AddFriendMsg.AddFriendReq addFriendObject = AddFriendMsg.AddFriendReq.
					parseFrom(networkMessage.getMessageObjectBytes());
			AddFriendMsg.AddFriendRsp.Builder addFriendBuilder = AddFriendMsg.AddFriendRsp.newBuilder();
			
			ClientUser clientUser = ServerModel.instance.getClientUserFromTable(
					networkMessage.ioSession.getRemoteAddress().toString());
			try{
				Session session = HibernateSessionFactory.getSession();
				Criteria criteria = session.createCriteria(User.class);
				Criteria criteria2 = session.createCriteria(User.class);
				criteria.add(Restrictions.eq("userId", clientUser.userId));
				User u = (User) criteria.list().get(0);
				criteria2.add(Restrictions.eq("userId", addFriendObject.getFriendUserId()));
				User friend = (User) criteria2.list().get(0);
				u.getFriends().add(friend);
				friend.getFriends().add(u);
			
				Transaction trans = session.beginTransaction();
			    session.update(u);
			    session.update(friend);
			    trans.commit();
			    
			    addFriendBuilder.setResultCode(AddFriendMsg.AddFriendRsp.ResultCode.SUCCESS);
			}catch(Exception e){
				addFriendBuilder.setResultCode(AddFriendMsg.AddFriendRsp.ResultCode.FAIL);
				e.printStackTrace();
			}
			

			//�ظ��ͻ���
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.ADDFRIEND_RSP.getNumber(),
							networkMessage.getMessageID(), addFriendBuilder.build().toByteArray()));
			
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
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
