package com.example.merxu.sharesdktest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.mob.tools.utils.UIHandler;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

public class MainActivity extends Activity implements Handler.Callback,PlatformActionListener,View.OnClickListener {
    //app key  21113609ad2f0
    //app secret 85fb897a71de4fa131bae07935bb5751
    private Button share_btn , login_btn,weixinlogin_btn,weixinlogin2_btn,weixin_btn;
    String userName;
    Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        share_btn = (Button) findViewById(R.id.share_btn);
        login_btn = (Button) findViewById(R.id.login_btn);
        weixinlogin_btn = (Button) findViewById(R.id.weixinlogin_btn);
        weixinlogin2_btn = (Button) findViewById(R.id.weixinlogin_btn2);
        weixin_btn = (Button) findViewById(R.id.weixin_btn);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String userIcon = (String) msg.obj;//发送空消息也行，只起到提示的作用
                BitmapUtils b = new BitmapUtils(MainActivity.this);
                b.display(share_btn,userIcon);
                login_btn.setText(userName);
            }
        };


        share_btn.setOnClickListener(this);
        login_btn.setOnClickListener(this);

        weixinlogin_btn.setOnClickListener(this);
        weixinlogin2_btn.setOnClickListener(this);
        weixin_btn.setOnClickListener(this);
    }

    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        oks.setTitle("标题");
        // titleUrl是标题的网络链接，QQ和QQ空间等使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        //url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");
        // 启动分享GUI
        oks.show(this);
    }


    private void qqlogin(){
        Platform qq = ShareSDK.getPlatform(QQ.NAME);
        //回调信息，可以在这里获取基本的授权返回的信息，但是注意如果做提示和UI操作要传到主线程handler里去执行
        qq.setPlatformActionListener(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {
                // TODO Auto-generated method stub
                arg2.printStackTrace();
                Log.e("---","buhefa");
            }

            @Override
            public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
                // TODO Auto-generated method stub
                //输出所有授权信息
                arg0.getDb().exportData();
                String userIcon = arg0.getDb().getUserIcon();
                Log.e("----",""+arg0.getDb().getUserIcon());
                userName = arg0.getDb().getUserName();
                Log.e("----",""+arg0.getDb().getUserName());

                //发送
                Message message = mHandler.obtainMessage();
                message.obj = userIcon;
                mHandler.sendMessage(message);

                Log.e("----",""+arg0.getDb().exportData());
                Log.e("----",""+arg2);
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {
                // TODO Auto-generated method stub
                Log.e("---","quxiao");
            }
        });
        //authorize与showUser单独调用一个即可
//        qq.authorize();//单独授权,OnComplete返回的hashmap是空的
        qq.showUser(null);//授权并获取用户信息
        //移除授权
        //weibo.removeAccount(true);
    }

    // 授权登录
    private void authorize(Platform plat,Boolean isSSO) {
        // 判断指定平台是否已经完成授权
        if (plat.isAuthValid()) {
            // 已经完成授权，直接读取本地授权信息，执行相关逻辑操作（如登录操作）
            String userId = plat.getDb().getUserId();
            if (!TextUtils.isEmpty(userId)) {
                UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
                login(plat.getName(), userId, null);
                return;
            }
        }
        plat.setPlatformActionListener(this);
        // 是否使用SSO授权：true不使用，false使用
        plat.SSOSetting(isSSO);
        // 获取用户资料
        plat.showUser(null);
    }
    // 取消授权
    private void cancleAuth(){
        Platform wxPlatform = ShareSDK.getPlatform(Wechat.NAME);
        wxPlatform.removeAccount(true);
        Toast.makeText(this,"取消授权成功!", Toast.LENGTH_SHORT).show();
    }

    //回调：授权成功
    public void onComplete(Platform platform, int action,HashMap<String, Object> res) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
            // 业务逻辑处理：比如登录操作
            String userName = platform.getDb().getUserName(); // 用户昵称
            String userId	= platform.getDb().getUserId();	  // 用户Id
            String platName = platform.getName();			  // 平台名称

            login(platName, userName, res);
        }
    }
    // 回调：授权失败
    public void onError(Platform platform, int action, Throwable t) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
        }
        t.printStackTrace();
    }
    // 回调：授权取消
    public void onCancel(Platform platform, int action) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
    }
    // 业务逻辑：登录处理
    private void login(String plat, String userId,HashMap<String, Object> userInfo) {
        Toast.makeText(this, "用户ID:"+userId, Toast.LENGTH_SHORT).show();
        Message msg = new Message();
        msg.what    = MSG_LOGIN;
        msg.obj     = plat;
        UIHandler.sendMessage(msg, this);
    }

    // 统一消息处理
    private static final int MSG_USERID_FOUND 	= 1; // 用户信息已存在
    private static final int MSG_LOGIN 			= 2; // 登录操作
    private static final int MSG_AUTH_CANCEL 	= 3; // 授权取消
    private static final int MSG_AUTH_ERROR 	= 4; // 授权错误
    private static final int MSG_AUTH_COMPLETE 	= 5; // 授权完成

    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case MSG_USERID_FOUND:
                Toast.makeText(this, "用户信息已存在，正在跳转登录操作......", Toast.LENGTH_SHORT).show();
                break;
            case MSG_LOGIN:
                Toast.makeText(this, "使用微信帐号登录中...", 	Toast.LENGTH_SHORT).show();
                break;
            case MSG_AUTH_CANCEL:
                Toast.makeText(this, "授权操作已取消", 	Toast.LENGTH_SHORT).show();
                break;
            case MSG_AUTH_ERROR:
                Toast.makeText(this, "授权操作遇到错误，请阅读Logcat输出", Toast.LENGTH_SHORT).show();
                break;
            case MSG_AUTH_COMPLETE:
                Toast.makeText(this,"授权成功，正在跳转登录操作…", Toast.LENGTH_SHORT).show();
                // 执行相关业务逻辑操作，比如登录操作
                String userName = new Wechat().getDb().getUserName(); // 用户昵称
                String userId	= new Wechat().getDb().getUserId();   // 用户Id
                String platName = new Wechat().getName();			   // 平台名称

                login(platName, userId, null);
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.weixinlogin_btn:
                authorize(new Wechat(),true);
                Log.e("TAG","weixinlogin_btn");
                break;
            case R.id.weixinlogin_btn2://授权登录
                authorize(new Wechat(),false);
                Log.e("TAG","weixinlogin_btn2");
                break;
            case R.id.weixin_btn:
                cancleAuth();
                Log.e("TAG","weixin_btn");
                break;
            case R.id.login_btn:
                qqlogin();
                Log.e("TAG","login_btn");
                break;
            case R.id.share_btn:
                showShare();
                Log.e("TAG","share_btn");
                break;
        }
    }
}
