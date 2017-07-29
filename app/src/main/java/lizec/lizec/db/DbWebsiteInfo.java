package lizec.lizec.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbWebsiteInfo extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "ly_db";
    private final static int DATABASE_VERSION = 1;
    private final String TABLE_NAME="WebsiteInfo";

    private static final String FILED_1 = "m_webLink";
    private static final String FILED_2 = "m_searchLink";
    private static final String FILED_3 = "m_eleName";
    public DbWebsiteInfo(Context context,String tableName){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+TABLE_NAME+" ( "+FILED_1 +" TEXT, "+ FILED_2 +" TEXT"+FILED_3+"TEXT"+");";
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
        Log.i("在网站数据库中","开始初始化");
        String sql = "CREATE TABLE "+TABLE_NAME+" ( "+FILED_1 +" TEXT,"+ FILED_2 +" TEXT"+FILED_3+"TEXT"+");";
        this.getWritableDatabase().execSQL(sql);
        this.getWritableDatabase().close();
        Log.i("在网站数据库中","初始化结束");
    }

    /**
     *判断表名在当前数据库中是否存在
     * @param tableName 待查询表名
     * @return 是否存在此表
     */
    public boolean isTableExist(String tableName){
        Log.i("在网站数据库中","判断表存在状态");
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
     * @param website 网址链接
     * @return  网站的两个数据１.搜索链接２．另一个关键数据
     */
    public String[] select(String website) {
        String selection = "m_webLink = ?";
        String[] selectionArgs = {website};
        Cursor cursor = this.getReadableDatabase()
                .query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        String[] content=new String[2];
        while (cursor.moveToNext()) {
            content[0] = cursor.getString(cursor.getColumnIndex(FILED_2));
            content[1]=cursor.getString(cursor.getColumnIndex(FILED_3));
        }

        cursor.close();
        return content;
    }

    public boolean websiteIsExist(String website){
        String selection = "m_webLink = ?";
        String[] selectionArgs = {website};
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
     * 插入一个网站
     * @param website 网站名字
     * @param data   数据
     */
    public void insert(String website,String[]data){
        ContentValues cv = new ContentValues();
        cv.put(FILED_1, website);
        cv.put(FILED_2, data[0]);
        cv.put(FILED_3, data[1]);

        this.getWritableDatabase().insert(TABLE_NAME, null, cv);
        this.getWritableDatabase().close();//关闭数据库对象
    }

    /**
     * 更新一个网站
     * @param website 网站名字
     * @param data   数据
     */
    public void update(String website,String[]data){
        ContentValues cv = new ContentValues();
        cv.put(FILED_2,data[0]);
        cv.put(FILED_3,data[1]);
        String where =FILED_1+" = ?";
        String[] whereValues= {website};
        this.getWritableDatabase().update(TABLE_NAME, cv, where, whereValues);
        this.getWritableDatabase().close();
    }


    public void dropTable (){
        this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        Log.i("删除表",TABLE_NAME);
        this.getWritableDatabase().close();
    }
}
