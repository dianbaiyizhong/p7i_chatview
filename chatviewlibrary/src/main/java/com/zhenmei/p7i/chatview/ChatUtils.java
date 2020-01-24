package com.zhenmei.p7i.chatview;

import android.content.Context;
import android.content.Intent;

import com.zhenmei.p7i.chatview.activity.ChatActivity;

import java.util.List;

import cn.leancloud.AVUser;
import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.cache.LCIMProfileCache;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.callback.AVIMClientCallback;

public class ChatUtils {
    public static void saveUser(String userId, String nickName, String avatar) {
        LCChatKitUser user = new LCChatKitUser(
                userId,
                nickName,
                avatar
        );
        LCIMProfileCache.getInstance().cacheUser(user);
    }

    public static void saveUser(Long userId, String nickName, String avatar) {
        LCChatKitUser user = new LCChatKitUser(
                String.valueOf(userId),
                nickName,
                avatar
        );
        LCIMProfileCache.getInstance().cacheUser(user);
    }


    public interface LoginListener {
        public void success(int unreadCount);

        public void fail();

    }

    public static void login(String userId, final LoginListener loginListener) {
        LCChatKit.getInstance().open(userId, new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {

                if (e != null) {
                    loginListener.fail();
                    return;
                }
                List<String> convIdList = LCIMConversationItemCache.getInstance().getSortedConversationList();
                int unreadCount = 0;
                for (String convId : convIdList) {
                }

                loginListener.success(unreadCount);

            }
        });
    }


    public static void startChat(Context context) {
        AVIMOptions.getGlobalOptions().setAutoOpen(true);
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(LCIMConstants.PEER_ID, "");
        context.startActivity(intent);
    }

    public static void startChat(Context context, String userId) {

        AVIMOptions.getGlobalOptions().setAutoOpen(true);
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(LCIMConstants.PEER_ID, userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startChat(Context context, Long userId) {
        if (userId == null || userId == 0l) {
            return;
        }
        AVIMOptions.getGlobalOptions().setAutoOpen(true);
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(LCIMConstants.PEER_ID, userId.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void logout() {
        AVUser.logOut();
    }
}
