package lizec.lizec.apptest;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import lizec.lizec.db.DbBookInfo;
import lizec.lizec.db.DbBookPage;
import lizec.lizec.tools.Book;
import lizec.lizec.tools.BookDownloadThread;


public class BookActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{
    private Book thisBook;
    private String bookURL;
    private String bookType;
    private String bookUpdateTime;

    private static final int FINISH_LOAD_BOOK_FROM_NET = 1;
    private static final int UPDATE = 2;

    //新加载标题起始位置
    private int catalogLoadedIndex;
    //首次加载目录数量
    private static final int FIRST_LOAD_NUMBER = 10;
    //单次加载数量
    private static final int CATALOG_LOAD_NUM = 30;

    //标记当前页面是否完成加载（阻止某些事件提前响应）
    private boolean finishLoading = false;

    //TODO:根据数据库中是否已经缓存来实现同的加载方法
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        initActionBar();

        ProgressBar prbBook = (ProgressBar)findViewById(R.id.prbBook);
        prbBook.setProgress(0);

        bookURL = getIntent().getStringExtra("URL");
        bookType = getIntent().getStringExtra("Type");
        bookUpdateTime = getIntent().getStringExtra("UpdateTime");
        new Thread(new LoadBookThread()).start();
        new Thread(new RefreshProgressBarThread()).start();

        ScrollView scrollBook = (ScrollView)findViewById(R.id.scrollBook);
        scrollBook.setOnTouchListener(this);

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        ProgressBar prbBook = (ProgressBar)findViewById(R.id.prbBook);
        if(msg.what == FINISH_LOAD_BOOK_FROM_NET){
            prbBook.setProgress(100);
            initInfo();
            initCatalog();
        }
        else if(msg.what == UPDATE){
            prbBook.incrementProgressBy(msg.arg1);
        }
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ScrollView scrollBook = (ScrollView)findViewById(R.id.scrollBook);

        //如果当前屏幕顶部在控件上的坐标文本底端坐标之差小于1.2倍的手机屏幕长度
        if(finishLoading && scrollBook.getChildAt(0).getHeight()-scrollBook.getScrollY() < 1.2*scrollBook.getHeight()){
            addCatalog();
        }
        return false;
    }

    private class LoadBookThread implements Runnable{

        @Override
        public void run() {
            thisBook = new Book(bookURL);
            finishLoading = true;
            Message message = new Message();
            message.what = FINISH_LOAD_BOOK_FROM_NET;
            handler.sendMessage(message);
        }
    }

    private class RefreshProgressBarThread implements Runnable{
        @Override
        public void run() {
            Random random = new Random();
            int baseSleepTime = 160;
            for(int i=0;i<19;i++){
                try{
                    int time = -50 + random.nextInt(100);
                    Thread.sleep(baseSleepTime+time);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.arg1 = 5;
                msg.what = UPDATE;
                handler.sendMessage(msg);
            }
        }
    }

    private void initInfo(){
        TextView txtBookName = (TextView)findViewById(R.id.txtBookName);
        TextView txtBookAuthor = (TextView)findViewById(R.id.txtBookAuthor);
        TextView txtBookType = (TextView)findViewById(R.id.txtBookType);
        TextView txtBookUpdateTime = (TextView)findViewById(R.id.txtBookUpdateTime);
        TextView txtBookDesc = (TextView)findViewById(R.id.txtBookDesc);
        ImageView imgBook = (ImageView)findViewById(R.id.imgBook);
        if(thisBook.isHasValue()){
            txtBookName.setText(thisBook.getBookName());
            txtBookAuthor.setText(thisBook.getAuthor());
            txtBookDesc.setText(String.format("简介：\n%s",thisBook.getDesc()));
            txtBookType.setText(bookType);
            txtBookUpdateTime.setText(bookUpdateTime);
            imgBook.setImageBitmap(thisBook.getBitmap());
        }
        else{
            txtBookName.setText("加载失败");
            txtBookAuthor.setText("加载失败");
            txtBookDesc.setText("加载失败");
            txtBookType.setText("加载失败");
            txtBookUpdateTime.setText("加载失败");
        }
    }

    private void initCatalog(){
        LinearLayout layoutBook = (LinearLayout)findViewById(R.id.layoutBook);

        if(thisBook.isHasValue()){
            DbBookInfo info = new DbBookInfo(this);
            int beginIndex = info.getReadPageIndexByURL(thisBook.getUrl());
            int i = 0;
            for( ; (i< FIRST_LOAD_NUMBER || i <= beginIndex+5) && i < thisBook.getCatalogUrls().length; i++){
                String name;
                Log.i("在加载目录函数中","i等于"+i);
                if(i==beginIndex){
                    Log.i("在加载目录函数中","beginIndex="+beginIndex);
                    name = ">>>"+ thisBook.getCatalogNames()[i] + "<<<";
                }
                else{
                    name = thisBook.getCatalogNames()[i];
                }
                Button button = new Button(BookActivity.this);
                button.setText(name);
                button.setTag(i);
                button.setOnClickListener(BookActivity.this);
                button.setHeight(100);
                layoutBook.addView(button);
            }
            catalogLoadedIndex = i+1;
            //有缓存记录的书籍才滑动
            if(beginIndex != -1){
                scrollToBeginIndex(beginIndex,0);
            }

        }
    }

    private void scrollToBeginIndex(int index,int headBound){
        final ScrollView scrollBook = (ScrollView)findViewById(R.id.scrollBook);
        final int sum = headBound + 100 * (index+3);
        Log.i("在滚动函数中","index="+index+"bound="+headBound+"sum="+sum);
        scrollBook.post(new Runnable() {
            @Override
            public void run() {
                scrollBook.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }


    private void initDbPage(){
        DbBookPage bookPage = new DbBookPage(this,thisBook.getBookName());
        //在数据库中初始化相应的表，之后才能正常使用此表
        bookPage.initTable();
        Log.i("在页数据库初始化插入中","插入完毕");
    }

    private void dropDbPage(){
        DbBookPage bookPage = new DbBookPage(this,thisBook.getBookName());
        bookPage.dropTable();
        Log.i("在页数据库删除中","删除完毕");
    }

    private void addCatalog(){
        LinearLayout layoutBook = (LinearLayout)findViewById(R.id.layoutBook);
        if(thisBook.isHasValue()){
            for(int i=0;i<CATALOG_LOAD_NUM && catalogLoadedIndex+i < thisBook.getCatalogUrls().length;i++){
                String name = thisBook.getCatalogNames()[catalogLoadedIndex+i];
                Button button = new Button(BookActivity.this);
                button.setText(name);
                button.setTag(catalogLoadedIndex+i);
                button.setOnClickListener(BookActivity.this);
                layoutBook.addView(button);
            }
            catalogLoadedIndex += CATALOG_LOAD_NUM;
        }
    }

    private void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(this.getString(R.string.bookActionBarTitle));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,PageActivity.class);
        intent.putExtra("URLs",thisBook.getCatalogUrls());
        intent.putExtra("BeginIndex",(int)v.getTag());
        intent.putExtra("BookName",thisBook.getBookName());
        intent.putExtra("BookURL",thisBook.getUrl());
        this.startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //根据是否在数据库中，加载不同的布局
        DbBookInfo bookInfo = new DbBookInfo(this);
        if(bookInfo.checkBookByURL(bookURL)){
            getMenuInflater().inflate(R.menu.menu_book_delete, menu);
        }else {
            getMenuInflater().inflate(R.menu.menu_book_add, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(finishLoading){
            if(item.getTitle().equals(getString(R.string.action_delete))){
                removeFromBookshelf();
                item.setTitle(getString(R.string.action_add));
            }
            else if(item.getTitle().equals(getString(R.string.action_add))){
                addToBookshelf();
                item.setTitle(getString(R.string.action_delete));
            }
            else if(item.getItemId() == R.id.action_download) {
                //TODO:下载功能测试
                DbBookPage dbBookPage = new DbBookPage(this,thisBook.getBookName());

                if(dbBookPage.isTableExist(thisBook.getBookName())){
                    AlertDialog.Builder beginDownloadDialog = new AlertDialog.Builder(this);
                    beginDownloadDialog.setTitle("下载提醒" ) ;
                    beginDownloadDialog.setMessage("即将开始下载，预计"+(int)(thisBook.getCatalogUrls().length*0.0375)+"分钟后完成下载，确定开始？") ;
                    beginDownloadDialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadBook();
                        }
                    });
                    beginDownloadDialog.setNegativeButton("否", null);
                    beginDownloadDialog.create().show();
                    //Toast.makeText(this,"已经开始下载，预计"+(int)(thisBook.getCatalogUrls().length*0.0375)+"分钟后完成下载",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this,"请先将书籍添加到书架",Toast.LENGTH_LONG).show();
                }
            }
        }

        return true;
    }

    private void addToBookshelf(){
        try{
            String file = saveImgFile(thisBook.getBitmap(),thisBook.getBookName());
            DbBookInfo bookInfo = new DbBookInfo(this);
            bookInfo.insert(thisBook.getBookName(),file,thisBook.getUrl(),0,0);
            initDbPage();
        }catch (IOException e){
            Toast.makeText(this, "保存失败，请稍后重试", Toast.LENGTH_LONG).show();
        }
    }

    private void removeFromBookshelf(){
        try{
            DbBookInfo bookInfo = new DbBookInfo(this);
            bookInfo.deleteBook(bookURL);
            File file = new File(this.getFilesDir().getPath() + "/user_data/" + thisBook.getBookName());
            boolean f = file.delete();
            Log.i("删除结果：",Boolean.toString(f));
            dropDbPage();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public String saveImgFile(Bitmap bm, String fileName) throws IOException {
        File dirFile = new File(this.getFilesDir().getPath() + "/user_data");
        if(!dirFile.exists()){
            Boolean f = dirFile.mkdir();
            Log.i("文件夹创建结果",Boolean.toString(f));
        }
        String file = this.getFilesDir().getPath() + "/user_data/" + fileName;
        File myCaptureFile = new File(file);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        boolean f = bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        Log.i("保存图片结果为",Boolean.toString(f));
        bos.flush();
        bos.close();

        return file;
    }

    private void downloadBook(){

        DbBookPage dbBookPage = new DbBookPage(BookActivity.this,thisBook.getBookName());
        DbBookInfo dbBookInfo = new DbBookInfo(BookActivity.this);
        //从某个版本开始，不允许访问外部公共空间
        //只能访问外部私有空间
        File path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
        int sleepSecond = 0;
        if(setting.getBoolean("download_switch",true)){
            sleepSecond = Integer.parseInt(setting.getString("sync_frequency","0"));
        }

        new Thread(new BookDownloadThread(thisBook,dbBookPage,dbBookInfo,path,sleepSecond)).start();
    }

}
