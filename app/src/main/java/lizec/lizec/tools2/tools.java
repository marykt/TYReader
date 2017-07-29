package lizec.lizec.tools2;

/**
 * Created by ubuntu on 17-7-29.
 */

        import org.jsoup.Jsoup;
        import org.jsoup.nodes.Document;

public class tools {
    public String addpath(String url,String weburl){
        if(url.indexOf("http:")>0){
            return url;
        }
        else{
            return weburl+url;
        }
    }

    public Document getDocument(String URL) throws Exception{
        Document doc;
        try {
            doc = Jsoup.connect(URL).get();
        } catch (Exception e) {
            try {
                Thread.sleep(3);
                doc = Jsoup.connect(URL).get();
            } catch (Exception e1) {
                throw new Exception();
            }
        }
        return doc;
    }
}
