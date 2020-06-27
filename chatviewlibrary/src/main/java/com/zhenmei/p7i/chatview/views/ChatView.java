package com.zhenmei.p7i.chatview.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.zhenmei.p7i.chatview.R;

import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.CustomMenuEventListener;
import cn.jiguang.imui.chatinput.listener.OnCameraCallbackListener;
import cn.jiguang.imui.chatinput.listener.OnClickEditTextListener;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.listener.RecordVoiceListener;
import cn.jiguang.imui.chatinput.menu.Menu;
import cn.jiguang.imui.chatinput.menu.MenuManager;
import cn.jiguang.imui.chatinput.menu.view.MenuFeature;
import cn.jiguang.imui.chatinput.menu.view.MenuItem;
import cn.jiguang.imui.chatinput.record.RecordVoiceButton;
import cn.jiguang.imui.messages.MessageList;
import cn.jiguang.imui.messages.MsgListAdapter;
import cn.jiguang.imui.messages.ptr.PtrDefaultHeader;
import cn.jiguang.imui.utils.DisplayUtil;


public class ChatView extends RelativeLayout {

    //    private TextView mTitle;
//    private LinearLayout mTitleContainer;
    private MessageList mMsgList;
    private ChatInputView mChatInput;
    private RecordVoiceButton mRecordVoiceBtn;
//    private PullToRefreshLayout mPtrLayout;

    private RefreshLayout refreshLayout;

    private ImageButton mSelectAlbumIb;

    private MaterialToolbar toolbar;


    private LinearLayout chat_view_container;

    public ChatView(Context context) {
        super(context);
    }

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initModule() {
        toolbar = findViewById(R.id.toolBar);
//        mTitleContainer = (LinearLayout) findViewById(R.id.title_container);
//        mTitle = (TextView) findViewById(R.id.title_tv);
        mMsgList = (MessageList) findViewById(R.id.msg_list);
        mChatInput = (ChatInputView) findViewById(R.id.chat_input);
        refreshLayout = (RefreshLayout) findViewById(R.id.pull_to_refresh_layout);
        refreshLayout.setEnableAutoLoadMore(false);
        refreshLayout.setEnableLoadMore(false);

        chat_view_container = (LinearLayout) findViewById(R.id.chat_view_container);
        /**
         * Should set menu container height once the ChatInputView has been initialized.
         * For perfect display, the height should be equals with soft input height.
         */
        mChatInput.setMenuContainerHeight(819);
        mRecordVoiceBtn = mChatInput.getRecordVoiceButton();
        mSelectAlbumIb = mChatInput.getSelectAlbumBtn();
        PtrDefaultHeader header = new PtrDefaultHeader(getContext());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new LayoutParams(-1, -2));
        header.setPadding(0, DisplayUtil.dp2px(getContext(), 15), 0,
                DisplayUtil.dp2px(getContext(), 10));
        // header.setPtrFrameLayout(mPtrLayout);
//        mMsgList.setDateBgColor(Color.parseColor("#FF4081"));
//        mMsgList.setDatePadding(5, 10, 10, 5);
//        mMsgList.setEventTextPadding(5);
//        mMsgList.setEventBgColor(Color.parseColor("#34A350"));
//        mMsgList.setDateBgCornerRadius(15);
        mMsgList.setHasFixedSize(true);
//        mPtrLayout.setLoadingMinTime(1000);
//        mPtrLayout.setDurationToCloseHeader(1500);
//        mPtrLayout.setHeaderView(header);
//        mPtrLayout.addPtrUIHandler(header);
        // 下拉刷新时，内容固定，只有 Header 变化
        //    mPtrLayout.setPinContent(true);
        // set show display name or not
//        mMsgList.setShowReceiverDisplayName(true);
//        mMsgList.setShowSenderDisplayName(false);


        // add Custom Menu View
        MenuManager menuManager = mChatInput.getMenuManager();
        // menuManager.addCustomMenu("MY_CUSTOM", R.layout.menu_text_item, R.layout.menu_text_feature);

        // Custom menu order
        menuManager.setMenu(Menu.newBuilder().
                customize(true).
                setRight(Menu.TAG_SEND).
                setBottom(Menu.TAG_VOICE, Menu.TAG_EMOJI, Menu.TAG_GALLERY, Menu.TAG_CAMERA, "MY_CUSTOM").
                build());
        menuManager.setCustomMenuClickListener(new CustomMenuEventListener() {
            @Override
            public boolean onMenuItemClick(String tag, MenuItem menuItem) {
                //Menu feature will not be show shown if return false；
                return true;
            }

            @Override
            public void onMenuFeatureVisibilityChanged(int visibility, String tag, MenuFeature menuFeature) {
                if (visibility == View.VISIBLE) {
                    // Menu feature is visible.
                } else {
                    // Menu feature is gone.
                }
            }
        });

    }

    public RefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    public void setTitle(String title) {
        //mTitle.setText(title);
        toolbar.setTitle(title);
    }

    public void setMenuClickListener(OnMenuClickListener listener) {
        mChatInput.setMenuClickListener(listener);
    }

    public void setAdapter(MsgListAdapter adapter) {
        mMsgList.setAdapter(adapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mMsgList.setLayoutManager(layoutManager);
    }

    public void setRecordVoiceFile(String path, String fileName) {
        mRecordVoiceBtn.setVoiceFilePath(path, fileName);
    }

    public void setCameraCaptureFile(String path, String fileName) {
        mChatInput.setCameraCaptureFile(path, fileName);
    }

    public void setRecordVoiceListener(RecordVoiceListener listener) {
        mChatInput.setRecordVoiceListener(listener);
    }

    public void setOnCameraCallbackListener(OnCameraCallbackListener listener) {
        mChatInput.setOnCameraCallbackListener(listener);
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mMsgList.setOnTouchListener(listener);
    }

    public void setOnTouchEditTextListener(OnClickEditTextListener listener) {
        mChatInput.setOnClickEditTextListener(listener);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public ChatInputView getChatInputView() {
        return mChatInput;
    }

    public MessageList getMessageListView() {
        return mMsgList;
    }

    public ImageButton getSelectAlbumBtn() {
        return this.mSelectAlbumIb;
    }

    /**
     * reset MessageList's height, so that switch to SoftInput or Menu
     * wouldn't cause MessageList scroll
     *
     * @param isTouchMsgList if touch MessageList, reset MessageList's height.
     */
    public void setMsgListHeight(boolean isTouchMsgList) {
        Logger.i("setMsgListHeight:" + isTouchMsgList);
        if (!isTouchMsgList) {
            ViewGroup.LayoutParams layoutParams = mMsgList.getLayoutParams();
            int height = mChatInput.getSoftKeyboardHeight();
            if (height > 0) {
                layoutParams.height = mChatInput.getSoftKeyboardHeight();
                mMsgList.setLayoutParams(layoutParams);
//                setMarginBottom();

            } else {
//                setMarginBottom();

            }
        } else {
            ViewGroup.LayoutParams layoutParams = mMsgList.getLayoutParams();
            layoutParams.height = mMsgList.getMaxHeight();
//            Log.d("ChatView", "set MessageList height, height = " + layoutParams.height);
            mMsgList.setLayoutParams(layoutParams);

//            setMarginBottom();
        }
    }


    public void setMarginBottom() {

        mChatInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                setMargins(chat_view_container, 0, 0, 0, mChatInput.getHeight());
            }
        }, 200);


    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (l > 0) {
                p.leftMargin = l;
            }
            if (r > 0) {
                p.rightMargin = r;

            }

            if (t >= 0) {
                p.topMargin = t;
            } else {
                p.topMargin = t;
            }

            if (b >= 0) {

                p.bottomMargin = b;
            }
            v.setLayoutParams(p);
//            p.setMargins(l, t, r, b);
            v.requestLayout();

        }
    }

}
