package lizec.lizec.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbBookPage extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "ly_db";
    private final static int DATABASE_VERSION = 1;
    private final String TABLE_NAME;

    private static final String FILED_1 = "page_index";
    private static final String FILED_2 = "content";

    public DbBookPage(Context context,String tableName){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_NAME = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+TABLE_NAME+" ( "+FILED_1 +" INT primary key, "+ FILED_2 +" TEXT);";
        db.execSQL(sql);
        Log.i("在页数据库中创建表",TABLE_NAME);
    }

    @Override
    //完全更新数据库时调用的方法
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        System.out.println("onUpgrade删除表");
        this.onCreate(db);
    }

    public void initTable(){
        Log.i("在页数据库中","开始初始化");
        String sql = "CREATE TABLE "+TABLE_NAME+" ( "+FILED_1 +" INT primary key, "+ FILED_2 +" TEXT);";
        this.getWritableDatabase().execSQL(sql);
        this.getWritableDatabase().close();
        Log.i("在页数据库中","初始化结束");
    }

    /**
     *判断表名在当前数据库中是否存在
     * @param tableName 待查询表名
     * @return 是否存在此表
     */
    public boolean isTableExist(String tableName){
        Log.i("在页数据库中","判断表存在状态");
        boolean isExist=true;
        String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"+tableName+"';";
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor c=db.rawQuery(sql, null);
        //不可以直接使用Cursor，需要让其现移动到第一个位置之后，再进行查询
        while (c.moveToNext()){
            if (c.getInt(0)==0) {
                isExist=false;
            }
        }
        c.close();
        Log.i("在页数据库中,表存在状态为",Boolean.toString(isExist));
        return isExist;
    }

    /**
     *
     * @param index 书籍指定的索引
     * @return  指定索引的内容
     */
    public String select(int index) {
        String selection = "page_index = ?";
        String[] selectionArgs = {Integer.toString(index)};
        Cursor cursor = this.getReadableDatabase()
                .query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        String content = null;
        while (cursor.moveToNext()) {
            content = cursor.getString(cursor.getColumnIndex(FILED_2));
        }

        cursor.close();
        return content;
    }

    public boolean pageIsExist(int index){
        String selection = "page_index = ?";
        String[] selectionArgs = {Integer.toString(index)};
        String[] columns = {FILED_1};
        Cursor cursor = this.getReadableDatabase()
                .query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        String content = null;
        while (cursor.moveToNext()) {
            content = cursor.getString(cursor.getColumnIndex(FILED_1));
        }
        cursor.close();
        return (content != null);
    }

    /**
     * 插入一页
     * @param pageIndex 页序号
     * @param content   内容
     */
    public void insert(int pageIndex,String content){
        ContentValues cv = new ContentValues();
        cv.put(FILED_1, pageIndex);
        cv.put(FILED_2, content);

        this.getWritableDatabase().insert(TABLE_NAME, null, cv);
        this.getWritableDatabase().close();//关闭数据库对象
    }

    /**
     * 更新一页
     * @param pageIndex 页序号
     * @param content   内容
     */
    public void update(int pageIndex,String content){
        ContentValues cv = new ContentValues();
        cv.put(FILED_2,content);
        String where =FILED_1+" = ?";
        String[] whereValues= {Integer.toString(pageIndex)};
        this.getWritableDatabase().update(TABLE_NAME, cv, where, whereValues);
        this.getWritableDatabase().close();
    }


    public void dropTable (){
        this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        Log.i("删除表",TABLE_NAME);
        this.getWritableDatabase().close();
    }
}
