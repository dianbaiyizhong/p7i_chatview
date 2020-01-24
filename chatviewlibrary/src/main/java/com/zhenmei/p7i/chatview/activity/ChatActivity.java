package com.zhenmei.p7i.chatview.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.BarUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.vondear.rxui.view.dialog.RxDialogScaleView;
import com.zhenmei.p7i.chatview.R;
import com.zhenmei.p7i.chatview.messages.BrowserImageActivity;
import com.zhenmei.p7i.chatview.models.DefaultUser;
import com.zhenmei.p7i.chatview.models.MyMessage;
import com.zhenmei.p7i.chatview.views.ChatView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.model.FileItem;
import cn.jiguang.imui.chatinput.model.VideoItem;
import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.messages.MsgListAdapter;
import cn.leancloud.AVException;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.cache.LCIMProfileCache;
import cn.leancloud.chatkit.event.LCIMIMTypeMessageEvent;
import cn.leancloud.chatkit.event.LCIMMessageUpdatedEvent;
import cn.leancloud.chatkit.event.LCIMOfflineMessageCountChangeEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.utils.LCIMConversationUtils;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.AVIMReservedMessageType;
import cn.leancloud.im.v2.AVIMTemporaryConversation;
import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.AVIMMessagesQueryCallback;
import cn.leancloud.im.v2.messages.AVIMImageMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;

public class ChatActivity extends AppCompatActivity implements View.OnTouchListener {
    private final int ITEM_LEFT = 100;
    private final int ITEM_LEFT_TEXT = 101;
    private final int ITEM_LEFT_IMAGE = 102;
    private final int ITEM_LEFT_AUDIO = 103;
    private final int ITEM_LEFT_LOCATION = 104;

    private final int ITEM_RIGHT = 200;
    private final int ITEM_RIGHT_TEXT = 201;
    private final int ITEM_RIGHT_IMAGE = 202;
    private final int ITEM_RIGHT_AUDIO = 203;
    private final int ITEM_RIGHT_LOCATION = 204;

    private final int ITEM_UNKNOWN = 300;


    protected AVIMConversation imConversation;
    private Window mWindow;

    private ChatView mChatView;

    private MsgListAdapter<MyMessage> rvAdapter;
    private ArrayList<String> mPathList = new ArrayList<>();
    private ArrayList<String> mMsgIdList = new ArrayList<>();
    private InputMethodManager mImm;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            //设置状态栏黑色字体与图标(只支持安卓5.0+，知乎也是这样)
            BarUtils.setStatusBarLightMode(this, true);
        }
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(getWindow());
        setContentView(R.layout.activity_chat_view);
        EventBus.getDefault().register(this);
        initView();
    }


    private void initView() {
        mChatView = findViewById(R.id.chat_view);
        mChatView.initModule();
        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mWindow = getWindow();
        if (null == LCChatKit.getInstance().getClient()) {
            finish();
            return;
        }

        /**
         * 获取参数
         */
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            if (extras.containsKey(LCIMConstants.PEER_ID)) {
                LCChatKit.getInstance().getClient().createConversation(
                        Arrays.asList(extras.getString(LCIMConstants.PEER_ID)), "", null, false, true, new AVIMConversationCreatedCallback() {
                            @Override
                            public void done(final AVIMConversation avimConversation, AVIMException e) {
                                if (null != e) {
                                } else {

                                    mChatView.getRefreshLayout().setOnRefreshListener(new OnRefreshListener() {
                                        @Override
                                        public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                                            updateConversation(avimConversation);

                                        }
                                    });

                                    mChatView.getRefreshLayout().autoRefresh();
                                }
                            }
                        });
            } else if (extras.containsKey(LCIMConstants.CONVERSATION_ID)) {
                String conversationId = extras.getString(LCIMConstants.CONVERSATION_ID);
                updateConversation(LCChatKit.getInstance().getClient().getConversation(conversationId));
            } else {
                finish();
            }
        }


//        mChatView.getChatInputView().getCameraBtn().setVisibility(View.GONE);
//        mChatView.getChatInputView().getVoiceBtn().setVisibility(View.GONE);
        // mChatView.getChatInputView().getPhotoBtn().setVisibility(View.GONE);
        initMsgAdapter();


        /**
         * 菜单的点击事件
         */
        mChatView.setMenuClickListener(new OnMenuClickListener() {
            @Override
            public boolean onSendTextMessage(CharSequence input) {
                if (input.length() == 0) {
                    return false;
                }
                sendText(input.toString());
                return true;
            }

            @Override
            public void onSendFiles(List<FileItem> list) {
                if (list == null || list.isEmpty()) {
                    return;
                }
                // 重新设置聊天列表的高度
                mChatView.setMsgListHeight(true);
                MyMessage message;
                for (FileItem item : list) {
                    if (item.getType() == FileItem.Type.Image) {
                        message = new MyMessage(null, IMessage.MessageType.SEND_IMAGE.ordinal());
                        mPathList.add(item.getFilePath());
                        mMsgIdList.add(message.getMsgId());

                        try {
                            sendMessage2Server(new AVIMImageMessage(item.getFilePath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (item.getType() == FileItem.Type.Video) {
                        message = new MyMessage(null, IMessage.MessageType.SEND_VIDEO.ordinal());
                        message.setDuration(((VideoItem) item).getDuration());

                    } else {
                        throw new RuntimeException("Invalid FileItem type. Must be Type.Image or Type.Video");
                    }


                }
            }

            @Override
            public boolean switchToMicrophoneMode() {
                scrollToBottom();
                String[] perms = new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

//                if (!EasyPermissions.hasPermissions(MessageListActivity.this, perms)) {
//                    EasyPermissions.requestPermissions(MessageListActivity.this,
//                            getResources().getString(R.string.rationale_record_voice),
//                            RC_RECORD_VOICE, perms);
//                }
                return true;
            }

            @Override
            public boolean switchToGalleryMode() {
                scrollToBottom();
                String[] perms = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };

//                if (!EasyPermissions.hasPermissions(MessageListActivity.this, perms)) {
//                    EasyPermissions.requestPermissions(MessageListActivity.this,
//                            getResources().getString(R.string.rationale_photo),
//                            RC_PHOTO, perms);
//                }
                // If you call updateData, select photo view will try to update data(Last update over 30 seconds.)
                mChatView.getChatInputView().getSelectPhotoView().updateData();
                return true;
            }

            @Override
            public boolean switchToCameraMode() {
                scrollToBottom();
                String[] perms = new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                };

//                if (!EasyPermissions.hasPermissions(MessageListActivity.this, perms)) {
//                    EasyPermissions.requestPermissions(MessageListActivity.this,
//                            getResources().getString(R.string.rationale_camera),
//                            RC_CAMERA, perms);
//                    return false;
//                } else {
//                    File rootDir = getFilesDir();
//                    String fileDir = rootDir.getAbsolutePath() + "/photo";
//                    mChatView.setCameraCaptureFile(fileDir, new SimpleDateFormat("yyyy-MM-dd-hhmmss",
//                            Locale.getDefault()).format(new Date()));
//                }
                return true;
            }

            @Override
            public boolean switchToEmojiMode() {
                scrollToBottom();
                return true;
            }
        });


        mChatView.getChatInputView().getInputView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrollToBottom();
                return false;
            }
        });


        mChatView.getSelectAlbumBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "点击选中一张图片",
                        Toast.LENGTH_SHORT).show();
            }
        });

        //scrollToBottom();

        mChatView.setMarginBottom();


    }


    private void scrollToBottom() {
//        mChatView.setMsgListHeight(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mChatView.getMessageListView().getLayoutManager();
                linearLayoutManager.setStackFromEnd(true);
                linearLayoutManager.scrollToPositionWithOffset(rvAdapter.getItemCount() - 1, Integer.MIN_VALUE);
                mChatView.getMessageListView().setLayoutManager(linearLayoutManager);
            }
        }, 200);
    }

    private void initMsgAdapter() {
        final float density = this.getResources().getDisplayMetrics().density;
        final float MIN_WIDTH = 60 * density;
        final float MAX_WIDTH = 200 * density;
        final float MIN_HEIGHT = 60 * density;
        final float MAX_HEIGHT = 200 * density;
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadAvatarImage(ImageView avatarImageView, String string) {
                // You can use other image load libraries.
                if (string.contains("R.drawable")) {
                    Integer resId = getResources().getIdentifier(string.replace("R.drawable.", ""),
                            "drawable", getPackageName());

                    avatarImageView.setImageResource(resId);
                } else {
                    Glide.with(ChatActivity.this)
                            .load(string)
                            .apply(new RequestOptions().placeholder(R.drawable.aurora_headicon_default))
                            .into(avatarImageView);
                }
            }


            @Override
            public void loadImage(final ImageView imageView, String path) {

                if (path.startsWith("http")) {
                    // You can use other image load libraries.
                    Glide.with(ChatActivity.this)
                            .asBitmap()
                            .load(path)
                            .apply(new RequestOptions().fitCenter().placeholder(R.drawable.aurora_picture_not_found))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    int imageWidth = resource.getWidth();
                                    int imageHeight = resource.getHeight();
                                    Logger.i("Image width " + imageWidth + " height: " + imageHeight);
                                    // 裁剪 bitmap
                                    float width, height;
                                    if (imageWidth > imageHeight) {
                                        if (imageWidth > MAX_WIDTH) {
                                            float temp = MAX_WIDTH / imageWidth * imageHeight;
                                            height = temp > MIN_HEIGHT ? temp : MIN_HEIGHT;
                                            width = MAX_WIDTH;
                                        } else if (imageWidth < MIN_WIDTH) {
                                            float temp = MIN_WIDTH / imageWidth * imageHeight;
                                            height = temp < MAX_HEIGHT ? temp : MAX_HEIGHT;
                                            width = MIN_WIDTH;
                                        } else {
                                            float ratio = imageWidth / imageHeight;
                                            if (ratio > 3) {
                                                ratio = 3;
                                            }
                                            height = imageHeight * ratio;
                                            width = imageWidth;
                                        }
                                    } else {
                                        if (imageHeight > MAX_HEIGHT) {
                                            float temp = MAX_HEIGHT / imageHeight * imageWidth;
                                            width = temp > MIN_WIDTH ? temp : MIN_WIDTH;
                                            height = MAX_HEIGHT;
                                        } else if (imageHeight < MIN_HEIGHT) {
                                            float temp = MIN_HEIGHT / imageHeight * imageWidth;
                                            width = temp < MAX_WIDTH ? temp : MAX_WIDTH;
                                            height = MIN_HEIGHT;
                                        } else {
                                            float ratio = imageHeight / imageWidth;
                                            if (ratio > 3) {
                                                ratio = 3;
                                            }
                                            width = imageWidth * ratio;
                                            height = imageHeight;
                                        }
                                    }
                                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                                    params.width = (int) width;
                                    params.height = (int) height;
                                    imageView.setLayoutParams(params);
                                    Matrix matrix = new Matrix();
                                    float scaleWidth = width / imageWidth;
                                    float scaleHeight = height / imageHeight;
                                    matrix.postScale(scaleWidth, scaleHeight);
                                    imageView.setImageBitmap(Bitmap.createBitmap(resource, 0, 0, imageWidth, imageHeight, matrix, true));
                                }
                            });
                } else {

                    File file = new File(path);

                    Glide.with(ChatActivity.this)
                            .asBitmap()
                            .load(file)
                            .apply(new RequestOptions().fitCenter().placeholder(R.drawable.aurora_picture_not_found))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    int imageWidth = resource.getWidth();
                                    int imageHeight = resource.getHeight();
                                    Logger.i("Image width " + imageWidth + " height: " + imageHeight);
                                    // 裁剪 bitmap
                                    float width, height;
                                    if (imageWidth > imageHeight) {
                                        if (imageWidth > MAX_WIDTH) {
                                            float temp = MAX_WIDTH / imageWidth * imageHeight;
                                            height = temp > MIN_HEIGHT ? temp : MIN_HEIGHT;
                                            width = MAX_WIDTH;
                                        } else if (imageWidth < MIN_WIDTH) {
                                            float temp = MIN_WIDTH / imageWidth * imageHeight;
                                            height = temp < MAX_HEIGHT ? temp : MAX_HEIGHT;
                                            width = MIN_WIDTH;
                                        } else {
                                            float ratio = imageWidth / imageHeight;
                                            if (ratio > 3) {
                                                ratio = 3;
                                            }
                                            height = imageHeight * ratio;
                                            width = imageWidth;
                                        }
                                    } else {
                                        if (imageHeight > MAX_HEIGHT) {
                                            float temp = MAX_HEIGHT / imageHeight * imageWidth;
                                            width = temp > MIN_WIDTH ? temp : MIN_WIDTH;
                                            height = MAX_HEIGHT;
                                        } else if (imageHeight < MIN_HEIGHT) {
                                            float temp = MIN_HEIGHT / imageHeight * imageWidth;
                                            width = temp < MAX_WIDTH ? temp : MAX_WIDTH;
                                            height = MIN_HEIGHT;
                                        } else {
                                            float ratio = imageHeight / imageWidth;
                                            if (ratio > 3) {
                                                ratio = 3;
                                            }
                                            width = imageWidth * ratio;
                                            height = imageHeight;
                                        }
                                    }
                                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                                    params.width = (int) width;
                                    params.height = (int) height;
                                    imageView.setLayoutParams(params);
                                    Matrix matrix = new Matrix();
                                    float scaleWidth = width / imageWidth;
                                    float scaleHeight = height / imageHeight;
                                    matrix.postScale(scaleWidth, scaleHeight);
                                    imageView.setImageBitmap(Bitmap.createBitmap(resource, 0, 0, imageWidth, imageHeight, matrix, true));
                                }
                            });
                }


            }

            /**
             * Load video message
             * @param imageCover Video message's image cover
             * @param uri Local path or url.
             */
            @Override
            public void loadVideo(ImageView imageCover, String uri) {
                long interval = 5000 * 1000;
                Glide.with(ChatActivity.this)
                        .asBitmap()
                        .load(uri)
                        // Resize image view by change override size.
                        .apply(new RequestOptions().frame(interval).override(200, 400))
                        .into(imageCover);
            }
        };

        // Use default layout
        MsgListAdapter.HoldersConfig holdersConfig = new MsgListAdapter.HoldersConfig();
        rvAdapter = new MsgListAdapter<>("0", holdersConfig, imageLoader);
        // If you want to customise your layout, try to create custom ViewHolder:
        // holdersConfig.setSenderTxtMsg(CustomViewHolder.class, layoutRes);
        // holdersConfig.setReceiverTxtMsg(CustomViewHolder.class, layoutRes);
        // CustomViewHolder must extends ViewHolders defined in MsgListAdapter.
        // Current ViewHolders are TxtViewHolder, VoiceViewHolder.

        rvAdapter.setOnMsgClickListener(new MsgListAdapter.OnMsgClickListener<MyMessage>() {
            @Override
            public void onMessageClick(MyMessage message) {
//                // do something
                if (message.getType() == IMessage.MessageType.RECEIVE_VIDEO.ordinal()
                        || message.getType() == IMessage.MessageType.SEND_VIDEO.ordinal()) {
//                    if (!TextUtils.isEmpty(message.getMediaFilePath())) {
//                        Intent intent = new Intent(MessageListActivity.this, VideoActivity.class);
//                        intent.putExtra(VideoActivity.VIDEO_PATH, message.getMediaFilePath());
//                        startActivity(intent);
//                    }
                } else if (message.getType() == IMessage.MessageType.RECEIVE_IMAGE.ordinal()
                        || message.getType() == IMessage.MessageType.SEND_IMAGE.ordinal()) {
                    Intent intent = new Intent(ChatActivity.this, BrowserImageActivity.class);

//                    intent.putExtra("msgId", message.getMsgId());
//                    intent.putStringArrayListExtra("pathList", mPathList);
//                    intent.putStringArrayListExtra("idList", mMsgIdList);
//                    startActivity(intent);

                    Glide.with(ChatActivity.this)
                            .asBitmap()
                            .load(message.getMediaFilePath())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    RxDialogScaleView rxDialogScaleView = new RxDialogScaleView(ChatActivity.this);
                                    rxDialogScaleView.setImage(resource);
                                    rxDialogScaleView.show();
                                }
                            });
                } else {
//                    Toast.makeText(getApplicationContext(),
//                            getApplicationContext().getString(R.string.message_click_hint),
//                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        rvAdapter.setMsgLongClickListener(new MsgListAdapter.OnMsgLongClickListener<MyMessage>() {
            @Override
            public void onMessageLongClick(View view, MyMessage message) {
//                Toast.makeText(getApplicationContext(),
//                        getApplicationContext().getString(R.string.message_long_click_hint),
//                        Toast.LENGTH_SHORT).show();
                // do something
            }
        });

        rvAdapter.setOnAvatarClickListener(new MsgListAdapter.OnAvatarClickListener<MyMessage>() {
            @Override
            public void onAvatarClick(MyMessage message) {
//                DefaultUser userInfo = (DefaultUser) message.getFromUser();
//                Toast.makeText(getApplicationContext(),
//                        getApplicationContext().getString(R.string.avatar_click_hint),
//                        Toast.LENGTH_SHORT).show();
                // do something
            }
        });

        rvAdapter.setMsgStatusViewClickListener(new MsgListAdapter.OnMsgStatusViewClickListener<MyMessage>() {
            @Override
            public void onStatusViewClick(MyMessage message) {
                // message status view click, resend or download here
            }
        });

        mChatView.setAdapter(rvAdapter);
        rvAdapter.getLayoutManager().scrollToPosition(0);

    }


    private void paddingNewMessage(AVIMConversation currentConversation) {
        if (null == currentConversation || currentConversation.getUnreadMessagesCount() < 1) {
            return;
        }
        final int queryLimit = currentConversation.getUnreadMessagesCount() > 100 ? 100 : currentConversation.getUnreadMessagesCount();
        currentConversation.queryMessages(queryLimit, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> list, AVIMException e) {
                if (null != e) {
                    return;
                }
                for (AVIMMessage m : list) {
                    if (getItemViewType(m) == ITEM_LEFT_TEXT) {
                        LCChatKitUser chatKitUser = LCIMProfileCache.getInstance().getUserMap().get(m.getFrom());
                        DefaultUser defaultUser = new DefaultUser(chatKitUser.getUserId(), chatKitUser.getName(), chatKitUser.getAvatarUrl());
                        AVIMTextMessage textMessage = (AVIMTextMessage) m;
                        MyMessage myMessage = new MyMessage(textMessage.getText(), IMessage.MessageType.RECEIVE_TEXT.ordinal());
                        myMessage.setUserInfo(defaultUser);
                        rvAdapter.addToEnd(myMessage, true);
                    } else if (getItemViewType(m) == ITEM_LEFT_IMAGE) {
                        LCChatKitUser chatKitUser = LCIMProfileCache.getInstance().getUserMap().get(m.getFrom());
                        DefaultUser defaultUser = new DefaultUser(chatKitUser.getUserId(), chatKitUser.getName(), chatKitUser.getAvatarUrl());
                        AVIMImageMessage message = (AVIMImageMessage) m;
                        MyMessage myMessage = new MyMessage(null, IMessage.MessageType.RECEIVE_IMAGE.ordinal());
                        myMessage.setMediaFilePath(message.getFileUrl());
                        myMessage.setUserInfo(defaultUser);
                        rvAdapter.addToEnd(myMessage, true);
                    }


                }
//                itemAdapter.notifyDataSetChanged();
                clearUnreadCount();
            }
        });
    }

    private void clearUnreadCount() {
        if (imConversation.getUnreadMessagesCount() > 0) {
            imConversation.read();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final LCIMMessageUpdatedEvent event) {
        Log.i("p7i", JSON.toJSONString(event));

    }


    /**
     * 主动刷新 UI
     *
     * @param conversation
     */
    protected void updateConversation(AVIMConversation conversation) {
        if (null != conversation) {
            this.imConversation = conversation;

            if (conversation instanceof AVIMTemporaryConversation) {
                System.out.println("Conversation expired flag: " + ((AVIMTemporaryConversation) conversation).isExpired());
            }
            //      conversationFragment.setConversation(conversation);
            fetchMessages(conversation);
            LCIMConversationItemCache.getInstance().insertConversation(conversation.getConversationId());
            LCIMConversationUtils.getConversationName(conversation, new AVCallback<String>() {
                @Override
                protected void internalDone0(String s, AVException e) {
                    mChatView.getRefreshLayout().finishRefresh();
                    if (null != e) {
                        LCIMLogUtils.logException(e);
                    } else {
//                        initActionBar(s);
//                        mAdapter.addToEndChronologically(mData);
                        //设置标题栏
                        mChatView.setTitle(s);
                    }
                }
            });
        }
    }


    private void fetchMessages(AVIMConversation imConversation) {

        imConversation.queryMessages(new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> messageList, AVIMException e) {
                if (filterException(e)) {


                    final List<MyMessage> list = new ArrayList<>();

                    for (final AVIMMessage message : messageList) {
                        Log.i("p7i", "getItemViewType:" + getItemViewType(message));

                        LCChatKitUser chatKitUser = LCIMProfileCache.getInstance().getUserMap().get(message.getFrom());
                        DefaultUser defaultUser = new DefaultUser(chatKitUser.getUserId(), chatKitUser.getName(), chatKitUser.getAvatarUrl());
                        if (getItemViewType(message) == ITEM_LEFT_TEXT) {
                            AVIMTextMessage textMessage = (AVIMTextMessage) message;
                            MyMessage myMessage = new MyMessage(textMessage.getText(), IMessage.MessageType.RECEIVE_TEXT.ordinal());
                            myMessage.setUserInfo(defaultUser);
                            list.add(myMessage);
                        } else if (getItemViewType(message) == ITEM_RIGHT_TEXT) {
                            AVIMTextMessage textMessage = (AVIMTextMessage) message;
                            MyMessage myMessage = new MyMessage(textMessage.getText(), IMessage.MessageType.SEND_TEXT.ordinal());
                            myMessage.setUserInfo(defaultUser);
                            myMessage.setMessageStatus(IMessage.MessageStatus.SEND_SUCCEED);
                            list.add(myMessage);
                        } else if (getItemViewType(message) == ITEM_RIGHT_IMAGE) {
                            AVIMImageMessage imageMessage = (AVIMImageMessage) message;
                            MyMessage myMessage = new MyMessage(null, IMessage.MessageType.SEND_IMAGE.ordinal());
                            myMessage.setTimeString(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                            myMessage.setMediaFilePath(imageMessage.getFileUrl());
                            myMessage.setMessageStatus(IMessage.MessageStatus.SEND_SUCCEED);
                            myMessage.setUserInfo(defaultUser);
                            mPathList.add(myMessage.getMediaFilePath());
                            mMsgIdList.add(myMessage.getMsgId());
                            list.add(myMessage);
                        } else if (getItemViewType(message) == ITEM_LEFT_IMAGE) {
                            AVIMImageMessage imageMessage = (AVIMImageMessage) message;
                            MyMessage myMessage = new MyMessage(null, IMessage.MessageType.RECEIVE_IMAGE.ordinal());
                            myMessage.setTimeString(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                            myMessage.setMediaFilePath(imageMessage.getFileUrl());
                            myMessage.setMessageStatus(IMessage.MessageStatus.SEND_SUCCEED);
                            myMessage.setUserInfo(defaultUser);
                            mPathList.add(myMessage.getMediaFilePath());
                            mMsgIdList.add(myMessage.getMsgId());
                            list.add(myMessage);
                        }
                    }

                    clearUnreadCount();
                    rvAdapter.addToEnd(list);
                    mChatView.getMessageListView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            mChatView.getMessageListView().smoothScrollToPosition(1);

                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mChatView.getMessageListView().getLayoutManager();
                            linearLayoutManager.setStackFromEnd(true);
                            linearLayoutManager.scrollToPositionWithOffset(rvAdapter.getItemCount() - 1, Integer.MIN_VALUE);
                            mChatView.getMessageListView().setLayoutManager(linearLayoutManager);

                            //rvAdapter.getLayoutManager().scrollToPosition(0);
                        }
                    }, 500);
                }
            }
        });
    }


    public int getItemViewType(AVIMMessage message) {
        if (null != message && message instanceof AVIMTypedMessage) {
            AVIMTypedMessage typedMessage = (AVIMTypedMessage) message;
            boolean isMe = fromMe(typedMessage);
            if (typedMessage.getMessageType() == AVIMReservedMessageType.TextMessageType.getType()) {
                return isMe ? ITEM_RIGHT_TEXT : ITEM_LEFT_TEXT;
            } else if (typedMessage.getMessageType() == AVIMReservedMessageType.AudioMessageType.getType()) {
                return isMe ? ITEM_RIGHT_AUDIO : ITEM_LEFT_AUDIO;
            } else if (typedMessage.getMessageType() == AVIMReservedMessageType.ImageMessageType.getType()) {
                return isMe ? ITEM_RIGHT_IMAGE : ITEM_LEFT_IMAGE;
            } else if (typedMessage.getMessageType() == AVIMReservedMessageType.LocationMessageType.getType()) {
                return isMe ? ITEM_RIGHT_LOCATION : ITEM_LEFT_LOCATION;
            } else {
                return isMe ? ITEM_RIGHT : ITEM_LEFT;
            }
        }
        return ITEM_UNKNOWN;
    }


    private boolean filterException(Exception e) {
        if (null != e) {
            LCIMLogUtils.logException(e);
            Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return (null == e);
    }


    /**
     * 是不是当前用户发送的数据
     *
     * @param msg
     * @return
     */
    protected boolean fromMe(AVIMTypedMessage msg) {
        String selfId = LCChatKit.getInstance().getCurrentUserId();
        return msg.getFrom() != null && msg.getFrom().equals(selfId);
    }


    private DefaultUser getBenRen() {
        String selfId = LCChatKit.getInstance().getCurrentUserId();
        LCChatKitUser chatKitUser = LCIMProfileCache.getInstance().getUserMap().get(selfId);

        return new DefaultUser(chatKitUser.getUserId(), chatKitUser.getName(), chatKitUser.getAvatarUrl());
    }

    public void sendText(String input) {
        AVIMTextMessage message = new AVIMTextMessage();
        message.setText(input);
        sendMessage2Server(message);
    }

    /**
     * 发送文本消息
     */
    private void sendMessage2Server(AVIMMessage message) {
//        if (addToList) {
//            itemAdapter.addMessage(message);
//        }
//

//        scrollToBottom();

        AVIMMessageOption option = new AVIMMessageOption();
        if (message instanceof AVIMTextMessage) {
            AVIMTextMessage textMessage = (AVIMTextMessage) message;
            if (textMessage.getText().startsWith("tr:")) {
                option.setTransient(true);
            } else {
                option.setReceipt(true);
            }

            final MyMessage myMessage = new MyMessage(textMessage.getText(), IMessage.MessageType.SEND_TEXT.ordinal());
            rvAdapter.addToEnd(myMessage, true);
            myMessage.setUserInfo(getBenRen());
            myMessage.setTimeString(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            myMessage.setMessageStatus(IMessage.MessageStatus.SEND_GOING);
            imConversation.sendMessage(message, option, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    if (null != e) {
                        LCIMLogUtils.logException(e);
                    }
                    myMessage.setMessageStatus(IMessage.MessageStatus.SEND_SUCCEED);
                    rvAdapter.updateMessage(myMessage.getMsgId(), myMessage);
                }
            });

        } else if (message instanceof AVIMImageMessage) {
            final MyMessage myMessage = new MyMessage(null, IMessage.MessageType.SEND_IMAGE.ordinal());
            myMessage.setTimeString(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            myMessage.setMediaFilePath(((AVIMImageMessage) message).getLocalFilePath());
            myMessage.setUserInfo(getBenRen());
            myMessage.setMessageStatus(IMessage.MessageStatus.SEND_GOING);
            rvAdapter.addToEnd(myMessage, true);
            imConversation.sendMessage(message, option, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    rvAdapter.notifyDataSetChanged();
                    if (null != e) {
                        LCIMLogUtils.logException(e);
                    } else {
                        myMessage.setMessageStatus(IMessage.MessageStatus.SEND_SUCCEED);
                        rvAdapter.updateMessage(myMessage.getMsgId(), myMessage);
                    }
                }
            });
        } else {
            option.setReceipt(true);
        }


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LCIMOfflineMessageCountChangeEvent event) {
        Log.i("p7i", JSON.toJSONString(event));
    }

    /**
     * 接收到新的消息
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LCIMIMTypeMessageEvent messageEvent) {

        if (null != imConversation && null != messageEvent &&
                imConversation.getConversationId().equals(messageEvent.conversation.getConversationId())) {
            Logger.i("currentConv unreadCount=" + imConversation.getUnreadMessagesCount());
            if (imConversation.getUnreadMessagesCount() > 0) {
                paddingNewMessage(imConversation);
            } else {
                Logger.i(messageEvent.message.getMessageType() + "");
                LCChatKitUser chatKitUser = LCIMProfileCache.getInstance().getUserMap().get(messageEvent.message.getFrom());
                DefaultUser defaultUser = new DefaultUser(chatKitUser.getUserId(), chatKitUser.getName(), chatKitUser.getAvatarUrl());
                AVIMTextMessage textMessage = (AVIMTextMessage) messageEvent.message;
                MyMessage myMessage = new MyMessage(textMessage.getText(), IMessage.MessageType.RECEIVE_TEXT.ordinal());
                myMessage.setUserInfo(defaultUser);
                rvAdapter.addToEnd(myMessage, true);
                scrollToBottom();
            }
        }


    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ChatInputView chatInputView = mChatView.getChatInputView();
                if (chatInputView.getMenuState() == View.VISIBLE) {
                    chatInputView.dismissMenuLayout();
                }
                mChatView.setMsgListHeight(true);
                try {
                    View v = getCurrentFocus();
                    if (mImm != null && v != null) {
                        mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        view.clearFocus();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
        }
        return false;
    }
}
