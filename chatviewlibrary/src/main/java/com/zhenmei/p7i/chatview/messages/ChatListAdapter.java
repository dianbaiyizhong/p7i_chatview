package com.zhenmei.p7i.chatview.messages;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zhenmei.p7i.chatview.models.MyMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatListAdapter extends BaseQuickAdapter<MyMessage, BaseViewHolder> {
    public ChatListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @Nullable MyMessage myMessage) {

    }
}
