package com.zhenmei.p7i.chatview;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private Button btn_single_talk;
    private final String APP_ID = "DhL9381c2cbJy8KR9O5J4yO3-9Nh9j0Va";
    private final String APP_KEY = "QrM2CtTn8Ocq6vz1OxFEJ2OP";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LeanCloudApp.init(this, APP_ID, APP_KEY, "https://leancloud.p7ik4n.com");

        btn_single_talk = findViewById(R.id.btn_single_talk);
        btn_single_talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatUtils.login("3923612775636566016", new ChatUtils.LoginListener() {
                    @Override
                    public void success(int unReadCount) {
                        ChatUtils.saveUser("3923612775636566016", "nntk", "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1563357563589&di=d1d6c8d2baa92b6195c26fae99630030&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201509%2F04%2F20150904090551_RAjfL.jpeg");
                        ChatUtils.saveUser("3923621296851681280", "荣耀", "https://upyun.p7ik4n.com/uploads/avatar/20200101/080843_1ectzs0620bod7oic62y4p7qo1glai7k.jpeg");
                        ChatUtils.startChat(getApplicationContext(), "3923621296851681280");
                    }

                    @Override
                    public void fail() {

                    }
                });
            }
        });
    }
}
