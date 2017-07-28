package lizec.lizec.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Vector;

/**
 * 对通过网络查询书籍的类的包装
 * 注意：不可在主线程上执行网络操作
* */
public class SearchBook {
    public static int sleepTime = 3000;
    public static boolean isDEBUG = true;

    public static Vector<Book> search(String searchKeyword){
        Vector<Book> bookResult = new Vector<>();
        String url="http://zhannei.baidu.com/cse/search?q="+searchKeyword+"&s=8353527289636145615&nsid=0";
        Document doc;

        //注意网络操作不能在主线程上进行
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
        }catch (IOException e){
            e.printStackTrace();
            try{
                System.out.println("获取失败，进行第二次获取");
                Thread.sleep(sleepTime);
                doc = Jsoup.connect(url).get();
            }catch (Exception ee){
                System.out.println("第二次获取失败，返回空值");
                return bookResult;
            }
        }


        Elements resultList=doc.getElementsByClass("result-item result-game-item");
        for(Element e:resultList){
            Elements title=e.getElementsByClass("result-game-item-title-link");
            //titlelink
            String bookURL = title.first().attr("href");
            //title
            String bookName = title.first().attr("title");
            //desc
            Elements doc1 = e.getElementsByClass("result-game-item-desc");
            //根据严谨的实验可知，此字段需要处理异常
            String desc = null;
            try {
                desc = doc1.first().text();
            }
            catch (Exception exception){
                desc = "解析失败";
            }

            Elements doc2 = e.getElementsByClass("result-game-item-info");
            //author
            String author = doc2.first().getElementsByTag("span").get(1).text();
            //type
            String type = doc2.first().getElementsByTag("span").get(3).text();
            //time
            String updateTime = doc2.first().getElementsByTag("span").get(5).text();

            bookResult.add(new Book(bookURL,bookName,author,type,updateTime,desc));
        }

        if(isDEBUG){
            for(Book book:bookResult){
                System.out.println(book.getBookName());
            }
        }
        return bookResult;
    }
}
