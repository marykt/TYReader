package lizec.lizec.db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import lizec.lizec.tools.Book;

public class DbBookInfo extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "ly_db";
    private final static int DATABASE_VERSION = 1;
    private static final String TABLE_NAME ="book_data";

    private static final String FILED_1 = "book_name";
    private static final String FILED_2 = "pic_url";
    private static final String FILED_3="book_link";
    private static final String FILED_4="read_page";
    private static final String FILED_5="download_page";

    public DbBookInfo(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
        Log.i("在书籍数据库中","创建对象");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+TABLE_NAME+" ( "+FILED_1 +" TEXT, "+ FILED_2 +" TEXT, "+ FILED_3 +" TEXT , "+ FILED_4 +" INT, "+ FILED_5 +" INT);";
        db.execSQL(sql);
        Log.i("在书籍数据库中创建表",TABLE_NAME);
    }

    @Override
    //完全更新数据库时调用的方法
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        Log.w("在书籍数据库中","更新对象");
        this.onCreate(db);
    }

    /**
     * 查询表中所有的数据
     * @return 所有数据构成的Book数组
     */
    public Book[] select() {
        Cursor cursor = this.getReadableDatabase()
                .query(TABLE_NAME, null, null, null, null, null, null);
        Book[] data = new Book[cursor.getCount()];
        int count = 0;
        Log.i("在书籍数据库中","开始获取信息");
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(FILED_1));
            String pic_url = cursor.getString(cursor.getColumnIndex(FILED_2));
            String book_link = cursor.getString(cursor.getColumnIndex(FILED_3));
            int int1 = cursor.getInt(cursor.getColumnIndex(FILED_4));
            int int2 = cursor.getInt(cursor.getColumnIndex(FILED_5));
            data[count] = new Book(name, pic_url, book_link, int1, int2);
            count++;
        }
        cursor.close();
        Log.i("在书籍数据库中","获取信息结束");
        return data;
    }
    /**
     * 插入一条数据到表中
     * @param book_name 字段一的值
     * @param book_link 字段二的值
     */
    public void insert(String book_name ,String pic_url,String book_link,int readPage,int downloadPage){
        ContentValues cv = new ContentValues();
        cv.put(FILED_1, book_name);
        cv.put(FILED_2, pic_url);
        cv.put(FILED_3, book_link);
        cv.put(FILED_4, readPage);
        cv.put(FILED_5, downloadPage);
        Log.i("在书籍数据库中","开始插入");
        this.getWritableDatabase().insert(TABLE_NAME, null, cv);
        Log.i("在书籍数据库中","插入结束");
    }

    public  boolean checkBookByURL(String bookURL){
        Book[] books = select();
        for (Book book : books) {
            if (book.getUrl().equals(bookURL))
                return true;
        }
        return false;
    }

    /**
     * 查找指定URL对应的书的阅读进度
     * @param bookURL 书籍URL
     * @return 阅读进度，没有对应书籍则返回-1
     * 注意：此方法耗时较长
     */
    public int getReadPageIndexByURL(String bookURL){
        Book[] books=select();
        for (Book book : books) {
            if (book.getUrl().equals(bookURL))
                return book.getReadPageIndex();
        }
        return -1;
    }

    /**
     * 查找指定URL对应的书的下载进度
     * @param bookURL 书籍URL
     * @return 阅读进度，没有对应书籍则返回-1
     * 注意：此方法耗时较长
     */
    public int getDownloadPageIndexByURL(String bookURL){
        Book[] books=select();
        for (Book book : books) {
            if (book.getUrl().equals(bookURL))
                return book.getDownloadPageIndex();
        }
        return -1;
    }


    public void deleteBook(String bookURL){
        String where = FILED_3+" = ?";
        String[] names={bookURL};
        this.getWritableDatabase().delete(TABLE_NAME, where, names);
    }

    public void updateReadPageIndex(String bookURL ,int readPageIndex){
        ContentValues cv = new ContentValues();
        cv.put(FILED_4,readPageIndex);
        String where =FILED_3+" = ?";
        String[] whereValues= {bookURL};
        this.getWritableDatabase().update(TABLE_NAME, cv, where, whereValues);
    }

    public void updateDownloadPageIndex(String bookURL,int downloadPageIndex){
        ContentValues cv = new ContentValues();
        cv.put(FILED_5,downloadPageIndex);
        String where =FILED_3+" = ?";
        String[] whereValues= {bookURL};
        this.getWritableDatabase().update(TABLE_NAME, cv, where, whereValues);
    }

    public void dropTable (){
        this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        Log.i("在书籍数据库中","删除表");
        this.onCreate(this.getWritableDatabase());
    }
}