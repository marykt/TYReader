package lizec.lizec.tools;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import lizec.lizec.db.DbBookInfo;
import lizec.lizec.db.DbBookPage;


/*
downloadIndex标记约定
1：正在下载
0，下载完毕
 */

//TODO：下载进度条
public class BookDownloadThread implements Runnable{
    private Book thisBook;
    private DbBookPage database;
    private DbBookInfo infoDatabase;
    private int sleepSeconds;
    private File path;

    private int successCount = 0;
    private int failCount = 0;

    public BookDownloadThread(Book thisBook,DbBookPage database,DbBookInfo infoDatabase,File path,int sleepSeconds){
        this.thisBook = thisBook;
        this.database = database;
        this.infoDatabase = infoDatabase;
        this.sleepSeconds = sleepSeconds;
        this.path = path;
        Log.i("在下载线程中","sleep="+sleepSeconds+"path"+path);
    }

    @Override
    public void run() {
        downloadToDatabase();
        databaseToTxt();
    }

    //下载一页，不检查是否已经插入
    private boolean downloadAPageToDatabase(int pageIndex){
        Page thisPage = new Page(thisBook.getCatalogUrls()[pageIndex]);
        if(thisPage.isHasValue()){
            String fullContext = thisPage.getTitle() + "\n" + thisPage.getContext()+ "\n\n" ;
            database.insert(pageIndex,fullContext);
        }
        return thisPage.isHasValue();
    }

    private void downloadToDatabase(){
        infoDatabase.updateDownloadPageIndex(thisBook.getUrl(),1);
        int length = thisBook.getCatalogUrls().length;
        for(int i=0;i<length;i++){
            if(!database.pageIsExist(i)){
                boolean success = downloadAPageToDatabase(i);
                while (!success){
                    Log.i("在下载线程中","下载失败，开始重试");
                    failCount++;
                    if(failCount > successCount && failCount > 20){
                        //Toast.makeText()
                        return;
                    }
                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    success = downloadAPageToDatabase(i);
                }
                successCount++;
                Log.i("在下载线程中","成功下载第"+i+"章");
                Log.i("在下载线程中","成功"+successCount+"次,失败"+failCount+"次");
                Log.i("在下载线程中","完成"+(double)i/length + "%");

                try {
                    Thread.sleep(sleepSeconds * 1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i("在下载线程中","数据库中已缓存，跳过第"+i+"章");
            }
        }
    }

    private void databaseToTxt(){
        try{
            Log.i("在下载线程中","开始生成文件");
            //每次都重新生成文件
            PrintStream outputStream = new PrintStream(
                    new FileOutputStream(
                            new File(path, thisBook.getBookName()+".txt")));

            for(int i=0;i<thisBook.getCatalogUrls().length;i++){
                String context = database.select(i);
                outputStream.print(context);
            }
            outputStream.close();
            Log.i("在下载线程中","文件生成结束");
            infoDatabase.updateDownloadPageIndex(thisBook.getUrl(),0);
        }catch (IOException e){
            e.printStackTrace();
            Log.i("在下载线程中","抛出异常，文件生成结束");
        }
    }
}
