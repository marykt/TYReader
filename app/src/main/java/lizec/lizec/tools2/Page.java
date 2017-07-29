package lizec.lizec.tools2;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

/**
 * Created by ubuntu on 17-7-29.
 */

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
    //new
    private Vector<Integer> get_text_n = new Vector<Integer>();

  //  @RequiresApi(api = Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Page(String url, String title) {//接口改变//预计可以和之前的融合
        //url="http://www.biquge.com/13_13453/7786250.html";
        Document doc = null;
        this.title=title;
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


        lenget_text(doc, 0);
        get_text_n.sort(null);
        System.out.println(get_text_n);
        int max_n=0;
        int t=0;
        for(int i=0;i<get_text_n.size()-1;i++){
            if(max_n<get_text_n.get(i+1)-get_text_n.get(i)){
                max_n=get_text_n.get(i+1)-get_text_n.get(i);
                t=i+1;
            }
        }
        System.out.println(get_text_n.get(t));
        lenget_text(doc, get_text_n.get(t));

//        Elements list=doc.getElementsByAttributeValue("class", "bookName");
//        title=list.first().getElementsByTag("h1").first().text();
//
//        context = doc.getElementById("content").html().replace("&nbsp;","");
//        context = context.replaceAll("\n<br>\n<br>","\n") + "\n\n\n";
//        context = Jsoup.parse(context).text();
    }


    //TODO:全局关键字过滤
    private void lenget_text(Element hh, int flag){
        try{
            for (Element i:hh.children()){
                try{
                    if (i.text().length()>20){
                        if (i.text().length()==flag){
                            this.context=i.text();
                            hasValue=true;
                        }
                        get_text_n.add(i.text().length());
                    }
                }
                catch (Exception e){
                    continue;
                }

            }
            for (Element i:hh.children()){
                lenget_text(i,flag);
            }
        }
        catch(Exception e1){
            return;
        }
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