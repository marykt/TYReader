package lizec.lizec.tools2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by ubuntu on 17-7-29.
 */

public class Book {
    //新增加的变量
    private Website site;
    //共用变量

    //private static String g_url="http://www.biquge.com.tw/";

    private Bitmap img;//图片实例
    private boolean hasValue =false;
    private String img_url;
    private String url; //书籍URL
    private String bookName;
    private String author;
    private String type; //书籍类型
    private String updateTime; //更新时间
    private String desc;//书籍简介
    private String[] catalogUrls; //详细目录（URL）
    private String[] catalogNames; //详细目录（书名）
    private int readPageIndex = 0;
    private int downloadPageIndex = 0;
    private tools tool=new tools();

    //通过搜索结果构造Book
    public Book(String url, String bookName, String author, String type, String updateTime,String desc){
        this.author=author;
        this.url=url;
        this.bookName = bookName;
        this.type=type;
        this.updateTime = updateTime;
        this.desc = desc;
    }

    //通过URL，从网络构造Book
    public Book(String url,String name) {//比上一版多加了一个名字。。
        //url="http://www.biquge.com/13_13453/";
        //System.out.println("在Book类URL构造函数中，URL为："+url);
        //对应为test2
        this.url=url;
        this.bookName=name;
        URL u=null;
        try {
            u=new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Website site=new Website();
        site.initFromDatabase("http://"+u.getHost());
        this.site=site;
        Document doc = getDocument(url);
        if(doc==null){
            hasValue =false;
            return;
        }

        for (Element i :doc.getElementsByTag("img")){
            for (Attribute j :i.attributes()){
                if(j.getValue().equals(name)){
                    img_url=tool.addpath(i.attr("src"), site.getM_webLink());
                    System.out.println(img_url);
                }
            }
        }
        initCatalog(doc);
//        Elements ListDiv =doc.getElementsByAttributeValue("id","info");
//        Elements docList=doc.getElementsByAttributeValue("id","intro");
//        desc     = docList.first().text();
//        bookName = ListDiv.first().getElementsByTag("h1").text();
//        author   = ListDiv.first().getElementsByTag("p").first().text().split("：")[1];
//
//        //初始化两个目录相关的变量
//
//
//        //获得图片URL
//        img_url=g_url+doc.getElementById("fmimg").getElementsByTag("img").first().attr("src");
//        img = getImgFromInterNet(img_url);

        hasValue =true;
    }

    //从数据库构造Book
    public Book(String bookName,String imgURL,String bookURL,int readPageIndex,int downloadPageIndex){
        this.bookName = bookName;
        this.img = getImgFromFile(imgURL);
        this.url = bookURL;
        this.readPageIndex = readPageIndex;
        this.downloadPageIndex = downloadPageIndex;
        this.hasValue = true;
    }

    //在目录没有构造或者需要刷新的时候调用此方法
    public void loadCatalog(){
        Document doc = getDocument(this.url);
        initCatalog(doc);
    }

    public Page getPage(int index){
        System.out.println("在Book类getPage函数中，URL："+catalogUrls[index]+catalogNames[index]);
        return new Page(catalogUrls[index],catalogNames[index]);
    }


    private void sort(Elements catalog){
        Elements list = catalog;
        Element a=list.first();
        int i=1;
        while(a.text().equals(list.get(list.size()-i).text())){
            list.remove(list.first());
            a=list.first();
            i++;
        }
    }


    //从网络中获取图片，以流的形式返回
    public static InputStream getImageViewInputStream(String s_url) throws IOException {
        InputStream inputStream = null;
        URL url = new URL(s_url);                    //服务器地址

        if (url != null) {
            //打开连接
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(1000);//设置网络连接超时的时间为1秒
            httpURLConnection.setRequestMethod("GET");        //设置请求方法为GET
            httpURLConnection.setDoInput(true);                //打开输入流
            int responseCode = httpURLConnection.getResponseCode();    // 获取服务器响应值
            if (responseCode == HttpURLConnection.HTTP_OK) {        //正常连接
                inputStream = httpURLConnection.getInputStream();        //获取输入流
            }
        }
        return inputStream;
    }

    private String[] getCatalogURLStrings(Elements catalog){
        String[] URLStrings = new String[catalog.size()];
        for(int i=0;i<catalog.size();i++){
            Element e = catalog.get(i);
            URLStrings[i]= tool.addpath(e.getElementsByTag("a").attr("href"), site.getM_webLink());
        }
        return URLStrings;
    }

    private String[] getCatalogNameStrings(Elements catalog){
        String[] nameStrings = new String[catalog.size()];
        for(int i=0;i<catalog.size();i++){
            Element e = catalog.get(i);
            nameStrings[i] = e.text();
        }
        return nameStrings;
    }

    private Document getDocument(String URL){
        Document doc;
        try {
            doc = Jsoup.connect(URL).get();
        } catch (Exception e) {
            try {
                Thread.sleep(3);
                doc = Jsoup.connect(URL).get();
            } catch (Exception e1) {
                doc = null;
            }
        }
        return doc;
    }

    //对此类中的两个关于目录的字段进行初始化
    private void initCatalog(Document doc){
        //获得目录相关信息
        Elements catalog=doc.getElementsByTag(this.site.getM_eleName());
        //查询a标签获得所有连接
        //Elements catalog =list.first().getElementsByTag("a");
        //自定义函数，对标签进行排序
        //  print(i.get_text())
        //print(i.find("a")["href"])
        //sort(catalog);
        catalogUrls = getCatalogURLStrings(catalog);
        catalogNames = getCatalogNameStrings(catalog);
    }

    private Bitmap getImgFromInterNet(String imgURL){
        try {
            InputStream inputStream = getImageViewInputStream(img_url);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    private Bitmap getImgFromFile(String imgFile){
        Log.i("从文本获得图片，图片路径为",imgFile);
        try {
            InputStream inputStream = new FileInputStream(imgFile);
            Log.i("从文本获得图片","正常结束");
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.i("从文本获得图片","抛出异常");
            e.printStackTrace();
            return null;
        }
    }

    public boolean isHasValue() {
        return hasValue;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAuthor() {
        return author;
    }

    public String getType() {
        return type;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getDesc() {
        return desc;
    }

    public String[] getCatalogUrls() {
        return catalogUrls;
    }

    public String[] getCatalogNames(){
        return catalogNames;
    }

    public String getUrl(){
        return url;
    }

    public Bitmap getBitmap(){
        return img;
    }

    @Override
    public String toString() {
        return "Book{" +
                "hasValue=" + hasValue +
                ", url='" + url + '\'' +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", type='" + type + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", \ndesc='" + desc + '\'' +
                ", \ncatalogUrls=" + Arrays.toString(catalogUrls) +
                '}';
    }

    public int getReadPageIndex() {
        return readPageIndex;
    }

    public void setReadPageIndex(int readPageIndex) {
        this.readPageIndex = readPageIndex;
    }

    public int getDownloadPageIndex() {
        return downloadPageIndex;
    }

    public void setDownloadPageIndex(int downloadPageIndex) {
        this.downloadPageIndex = downloadPageIndex;
    }
}