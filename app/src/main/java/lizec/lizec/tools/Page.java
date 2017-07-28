package lizec.lizec.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;

//表示文章其中的一章的具体内容的类
public class Page implements Serializable {

    public boolean isHasValue() {
        return hasValue;
    }

    public String getTitle() {
        return title;
    }

    public String getContext() {
        return context;
    }

    private boolean hasValue;
    private  String title;
    private String context;

    //TODO:全局关键字过滤
    public Page(String url) {
        //url="http://www.biquge.com/13_13453/7786250.html";
        Document doc = null;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (doc==null){
            hasValue =false;
            return;
        }

        hasValue =true;

        Elements list=doc.getElementsByAttributeValue("class", "bookName");
        title=list.first().getElementsByTag("h1").first().text();

        context = doc.getElementById("content").html().replace("&nbsp;","");
        context = context.replaceAll("\n<br>\n<br>","\n") + "\n\n\n";
        context = Jsoup.parse(context).text();
    }

    @Override
    public String toString() {
        return "Page{" +
                "hasValue=" + hasValue +
                ", title='" + title + '\'' +
                ", \ncontext='" + context + '\'' +
                '}';
    }
}
