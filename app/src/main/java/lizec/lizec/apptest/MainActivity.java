package lizec.lizec.apptest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import lizec.lizec.db.DbBookInfo;
import lizec.lizec.db.DbBookPage;
import lizec.lizec.tools.Book;
import lizec.lizec.tools.BookDownloadThread;

//TODO:确保下载进度只调用一次
//TODO:BUG没有网络的情况下，无法正确加载主界面，考虑下载线程中是否有未捕捉的异常(无法重现此BUG）
//考虑将setting跳转回来的方法改为结束setting对应的activity
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //书架书籍列表
    Book[] books;
    int width;

    private static final int BEGIN_DOWNLOAD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowManager wm1 = this.getWindowManager();
        width = wm1.getDefaultDisplay().getWidth();
        initActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //此线程只应该被执行一次

        new Thread(new BeginDownloadThread()).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookshelf();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intentSetting = new Intent(MainActivity.this,SettingsActivity.class);
                MainActivity.this.startActivity(intentSetting);
                return true;
            case R.id.action_search:
                Intent intentSearch = new Intent(MainActivity.this,SearchActivity.class);
                MainActivity.this.startActivity(intentSearch);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(this.getString(R.string.mainTitle));
        }
    }

    private void loadBookshelf(){
        Log.i("主界面加载","开始加载");
        LayoutInflater inflater = getLayoutInflater();
        DbBookInfo bookInfo = new DbBookInfo(this);
        books = bookInfo.select();
        GridLayout gridMainBook = (GridLayout) findViewById(R.id.gridMainBook);
        gridMainBook.removeAllViewsInLayout();

        for(int i=0;i<books.length;i++){
            //只能使用null，否则会导致崩溃
            ImageView imageView = (ImageView) inflater.inflate(R.layout.bookshelf_book,null);
            //通过强制指定大小，使书籍整体看起来为正方形
            gridMainBook.addView(imageView,width/3,width/3);
            Log.i("在添加书的过程中，宽度为",Integer.toString(gridMainBook.getWidth()));

            Book thisBook = books[i];
            Log.i("添加书过程中",thisBook.getBookName());
            Log.i("添加书过程中,i是" ,Integer.toString(i));
            imageView.setImageBitmap(thisBook.getBitmap());
            imageView.setOnClickListener(this);
            imageView.setTag(i);
        }
    }


    @Override
    public void onClick(View v) {
        Log.i("单击事件中标签是",Integer.toString((int)v.getTag()));
        Book thisBook = books[(int)v.getTag()];
        Intent intent = new Intent(MainActivity.this,BookActivity.class);
        intent.putExtra("URL",thisBook.getUrl());
        intent.putExtra("Type","");
        intent.putExtra("UpdateTime","");
        MainActivity.this.startActivity(intent);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this,"已经自动开始上次未完成的下载",Toast.LENGTH_LONG).show();
        }
    };

    private class BeginDownloadThread implements Runnable{
        @Override
        public void run() {
            boolean hasDownload = false;
            DbBookInfo infoDatabase = new DbBookInfo(MainActivity.this);
            Book[] books = infoDatabase.select();
            for(Book thisBook:books){
                if(infoDatabase.getDownloadPageIndexByURL(thisBook.getUrl())== 1){
                    hasDownload = true;
                    Book book = new Book(thisBook.getUrl());
                    Log.i("在主界面下载线程中","开始下载"+book.getBookName());
                    DbBookPage dbBookPage = new DbBookPage(MainActivity.this,book.getBookName());
                    File path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    int sleepSecond = 0;
                    if(setting.getBoolean("download_switch",true)){
                        sleepSecond = Integer.parseInt(setting.getString("sync_frequency","0"));
                    }
                    new Thread(new BookDownloadThread(book,dbBookPage,infoDatabase,path,sleepSecond)).start();
                }
            }

            if(hasDownload) {
                Message msg = new Message();
                msg.what = BEGIN_DOWNLOAD;
                handler.sendMessage(msg);
            }

        }
    }

}
