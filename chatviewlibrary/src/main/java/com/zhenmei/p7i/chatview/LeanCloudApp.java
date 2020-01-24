package com.zhenmei.p7i.chatview;

import android.content.Context;

import com.zhenmei.p7i.chatview.activity.ChatActivity;

import cn.leancloud.AVInstallation;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.push.PushService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by wli on 16/2/24.
 */
public class LeanCloudApp {

    private final String APP_ID = "DhL9381c2cbJy8KR9O5J4yO3-9Nh9j0Va";
    private final String APP_KEY = "QrM2CtTn8Ocq6vz1OxFEJ2OP";

    public static void init(Context context, String appId, String appKey, String serverUrl) {
        LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
        AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
        LCChatKit.getInstance().init(context, appId, appKey, serverUrl);

        PushService.setDefaultPushCallback(context, ChatActivity.class);
        PushService.setAutoWakeUp(true);
        PushService.setDefaultChannelId(context, "default");
        AVIMOptions.getGlobalOptions().setAutoOpen(true);
        AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(AVObject avObject) {
                String installationId = AVInstallation.getCurrentInstallation().getInstallationId();

            }

            @Override
            public void onError(Throwable e) {
                // 保存失败，输出错误信息
                System.out.println("failed to save installation.");
            }

            @Override
            public void onComplete() {

            }
        });
    }


}

