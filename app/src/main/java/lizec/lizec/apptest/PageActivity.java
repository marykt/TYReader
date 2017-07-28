package lizec.lizec.apptest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import lizec.lizec.db.DbBookInfo;
import lizec.lizec.db.DbBookPage;
import lizec.lizec.tools.Page;


public class PageActivity extends AppCompatActivity implements View.OnTouchListener{
    //当前书籍的目录
    private String[] pageURLs;
    //新加载章节索引
    private int thisPageIndex;
    //此书的书名
    private String thisBookName;
    //此书的连接
    private String thisBookURL;

    //此Activity起始章节索引
    private int beginPageIndex;

    //浮标长度，用来记录当前位置的下一节的位置
    //初始为0，从而在第一次touch的时候，能够被更新为第一个文本框的长度
    private int floatLength = 0;
    //浮标索引，用来记录当前位置的下一节的 相对 索引（相对于起始章节作为第0节）
    //初始的时候为-1，从而在第一次touch的时候，能够被更新为0
    private int floatIndex = -1;


    //字体大小
    private int fontSize = 25;
    //提醒时间
    private int alertMinute = -1;

    //新的一页的内容
    private String newPageContent;
    //标记当前是否有爬取下一页的线程
    private boolean isGetting = false;

    //消息处理相关标记
    //表示完成爬取，通知主线程刷新界面
    private static final int FINISH_GET_PAGE = 1;
    private static final int ALERT = 2;

    private ScrollView scrollPage;
    private LinearLayout lineLayoutPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_page);
        scrollPage = (ScrollView)findViewById(R.id.scrolPage);
        lineLayoutPage = (LinearLayout)findViewById(R.id.lineLayoutPage);

        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
        fontSize = Integer.parseInt(setting.getString("font_size","25"));
        if(setting.getBoolean("alert_switch",false)){
            alertMinute = Integer.parseInt(setting.getString("alert_list","-1"));
        }

        pageURLs = getIntent().getStringArrayExtra("URLs");
        beginPageIndex = getIntent().getIntExtra("BeginIndex",0);
        thisPageIndex = beginPageIndex;
        thisBookName = getIntent().getStringExtra("BookName");
        thisBookURL = getIntent().getStringExtra("BookURL");

        new Thread(new GetPageThread()).start();
        new Thread(new AlertThread()).start();

        ScrollView scrollPage = (ScrollView)findViewById(R.id.scrolPage);
        scrollPage.setOnTouchListener(this);
    }

    //必须作为类成员，否则对成员变量访问复杂度增加
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            LinearLayout lineLayoutPage = (LinearLayout)findViewById(R.id.lineLayoutPage);
            if(msg.what == FINISH_GET_PAGE){
                TextView newPageTextView = new TextView(PageActivity.this);
                newPageTextView.setText(newPageContent);
                newPageTextView.setTextSize(fontSize);
                newPageTextView.setSingleLine(false);
                lineLayoutPage.addView(newPageTextView);


            }
            else if(msg.what == ALERT){
                //提醒操作只能在主线程上进行
                AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
                builder.setTitle("定时提醒" ) ;
                builder.setMessage("已经看了"+alertMinute+"分钟了，休息一下吧") ;
                builder.setPositiveButton("是", null);
                builder.setNegativeButton("否", null);
                builder.create().show();
            }
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //如果当前屏幕顶部在控件上的坐标文本底端坐标之差小于3倍的手机屏幕长度
        if(scrollPage.getChildAt(0).getHeight()-scrollPage.getScrollY() < 3*scrollPage.getHeight()){
            if(!isGetting){
                Log.i("在Page触摸事件处理中","开始新的获取页面线程");
                new Thread(new GetPageThread()).start();
                isGetting = true;
            }
        }


        //当前屏幕高度
        int nowY = scrollPage.getScrollY();


        if(nowY > floatLength){
            //文本累计高度
            int childHeight = floatLength;
            int i = floatIndex+1;
            for( ;i<lineLayoutPage.getChildCount();i++){
                childHeight += lineLayoutPage.getChildAt(i).getMeasuredHeight();
                if(nowY - childHeight < 0){
                    floatLength = childHeight;
                    floatIndex = i;
                    break;
                }
            }
            //TODO：检测对不存在的内容进行更新
            try{
                DbBookInfo info = new DbBookInfo(this);
                info.updateReadPageIndex(thisBookURL,beginPageIndex+i);
                Log.i("在Page触摸事件处理中","起始页面是"+beginPageIndex);
                Log.i("在Page触摸事件处理中","更新阅读索引"+beginPageIndex+i);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    private class GetPageThread implements Runnable{

        @Override
        public void run() {
            DbBookPage dbBookPage = new DbBookPage(PageActivity.this, thisBookName);
            //如果达到末尾，直接结束
            if(thisPageIndex >= pageURLs.length){
                Log.i("Page获取线程", "到达底部");
                return;
            }

            if (dbBookPage.isTableExist(thisBookName)) {
                newPageContent = dbBookPage.select(thisPageIndex);
                if(newPageContent != null){
                    Log.i("Page获取线程", "数据库命中");
                }
                else{
                    Log.i("Page获取线程", "从网络获取");
                    Page thisPage = new Page(pageURLs[thisPageIndex]);
                    newPageContent = "\n\n" + thisPage.getTitle() + "\n" + thisPage.getContext();
                    Log.i("Page获取线程", "插入数据库");
                    dbBookPage.insert(thisPageIndex, newPageContent);
                }
            }
            else{
                Log.i("Page获取线程", "非缓存书籍，从网络获取");
                Page thisPage = new Page(pageURLs[thisPageIndex]);
                newPageContent = thisPage.getTitle() + "\n" + thisPage.getContext()+ "\n\n" ;
            }

            //发送消息，更新UI
            Message msg = new Message();
            msg.what = FINISH_GET_PAGE;
            handler.sendMessage(msg);
            thisPageIndex++;
            isGetting = false;
        }
    }

    //定时提醒线程
    private class AlertThread implements Runnable{
        @Override
        public void run() {
            if(alertMinute != -1){
                try{
                    Thread.sleep(alertMinute*60*1000);
                    Message msg = new Message();
                    msg.what = ALERT;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}