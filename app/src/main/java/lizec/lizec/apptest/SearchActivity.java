package lizec.lizec.apptest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.Random;
import java.util.Vector;

import lizec.lizec.tools.Book;
import lizec.lizec.tools.SearchBook;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    Vector<Book> bookResult;
    private static final int COMPLEMENT = 1;
    private static final int UPDATE = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initActionBar();

        Button btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressBar prbSearchBook = (ProgressBar)findViewById(R.id.prbSearchBook);
                prbSearchBook.setProgress(0);
                LinearLayout layoutSearchResult = (LinearLayout)findViewById(R.id.layoutSearchResult);
                layoutSearchResult.removeAllViews();
                new Thread(new SearchBookThread()).start();
                new Thread(new RefreshProgressBarThread()).start();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ProgressBar prbSearchBook = (ProgressBar)findViewById(R.id.prbSearchBook);
            if (msg.what == COMPLEMENT) {
                System.out.println("收到结束指令");
                prbSearchBook.setProgress(100);
                LinearLayout layoutSearchResult = (LinearLayout)findViewById(R.id.layoutSearchResult);
                int count = 0;
                for(Book book:bookResult){
                    Button button = new Button(SearchActivity.this);
                    button.setText(book.getBookName());
                    button.setTag(count);
                    button.setOnClickListener(SearchActivity.this);
                    layoutSearchResult.addView(button);
                    count++;
                }
            }
            else if(msg.what == UPDATE){
                System.out.println("收到刷新指令");
                prbSearchBook.incrementProgressBy(msg.arg1);
            }
            System.out.println("收到指令,内容为"+msg.what);
        }
    };

    private class SearchBookThread implements Runnable{
        @Override
        public void run() {
            EditText txtSearch = (EditText)findViewById(R.id.txtSearch);
            String keyword = txtSearch.getText().toString();
            System.out.println(keyword);
            bookResult = SearchBook.search(keyword);
            Message message = new Message();
            message.what = COMPLEMENT;
            handler.sendMessage(message);
        }
    }

    private class RefreshProgressBarThread implements Runnable{
        @Override
        public void run() {
            Random random = new Random();
            int baseSleepTime = 50;
            for(int i=0;i<12;i++){
                try{
                    int time = -20 + random.nextInt(40);
                    Thread.sleep(baseSleepTime+time);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.arg1 = 8;
                msg.what = UPDATE;
                System.out.print("发送更新量="+msg.arg1);
                handler.sendMessage(msg);
            }
        }
    }

    private void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(this.getString(R.string.searchTitle));
        }
    }

    @Override
    public void onClick(View v) {
        Book thisBook = bookResult.elementAt((int)v.getTag());
        Intent intent = new Intent(SearchActivity.this,BookActivity.class);
        intent.putExtra("URL",thisBook.getUrl());
        intent.putExtra("Type",thisBook.getType());
        intent.putExtra("UpdateTime",thisBook.getUpdateTime());
        SearchActivity.this.startActivity(intent);
    }
}
