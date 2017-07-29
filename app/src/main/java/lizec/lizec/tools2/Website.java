package lizec.lizec.tools2;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by ubuntu on 17-7-29.
 */

public class Website {
    private tools tool=new tools();
    private String m_webLink;
    private String m_searchLink;
    private String m_eleName;

    //从数据库拿到信息
    public boolean initFromDatabase(String m_webLink){
        System.out.println(m_webLink);
        if (m_webLink.equals("http://www.qu.la")){
            this.m_eleName="dd";
            this.m_webLink=m_webLink;
        }
        return false;
    }
    public boolean auto_analysis(){
        try{
            if(auto_analysis_searchLink()&&
                    auto_analysis_eleName())
                return true;
        }
        catch(Exception e){
            return false;
        }
        return false ;
    }
    //自动生成网站关键信息
    private boolean auto_analysis_eleName(){
        String link=getM_searchLink("斗破苍穹");
        Document doc=null;
        try {
            doc = tool.getDocument(link);
        } catch (Exception e) {
            // 网络不好
            e.printStackTrace();
        }
        Elements e=doc.getElementsContainingOwnText("斗破");
        System.out.println(e.size());
        Vector<Vector<String >> path=new Vector< Vector<String > >();
        Set<Element> s = new HashSet<Element>();
        Vector<Integer> flag = new Vector<Integer>();
        for(Element i:e){
            flag.add(0);
            path.add(new Vector<String>());
        }
        int sum=0;
        while (sum<(e.size()/2-1)){
            sum=0;
            int i=0;
            s.clear();
            while( i<e.size()){
                path.get(i).add(e.get(i).tagName());
                if(s.contains(e.get(i).parent())){
                    sum+=1;
                    System.out.println(i);
                    flag.set(i,flag.get(i)+1);
                }
                else{
                    s.add(e.get(i).parent());
                }
                e.set(i, e.get(i).parent());
                i=i+1;
            }
            try{
                if(sum>=(e.size()/2-1)){
                    sum=0;
                    int j=0;
                    s.clear();
                    while(j<e.size()){
                        if(s.contains(e.get(j).parent()))
                            sum++;
                        else
                            s.add(e.get(j).parent());
                        j++;
                    }
                }
            }
            catch(Exception e1){
                break;
            }
        }
        int ele=0;
        for(int i=0;i<flag.size();i++){
            if(flag.get(i)==1){
                ele=i;
                break;
            }
        }
        System.out.println(path.get(ele));
        System.out.println(path.get(ele).get(path.get(ele).size()-1));
        this.m_eleName=path.get(ele).get(path.get(ele).size()-1);
        return true;
    }


    //自动生成网站关键信息
    private boolean auto_analysis_searchLink(){
        Document doc=null;
        try {
            doc = tool.getDocument(m_webLink);
        } catch (Exception e) {
            // 网络不好
            e.printStackTrace();
        }
        if(doc==null){
            return false;
        }
        Elements e=doc.getElementsByTag("input");
        Element ele=e.get(0).parent();
        if (ele==null){
            return false;
        }
        else{
            while(!ele.tag().getName().equals("form")){
                ele=ele.parent();
            }
        }
        //构造访问的url
        String search_name="一念永恒";
        String s="";
        for (int i=0;i<ele.children().size();i++){
            if(!ele.child(i).tag().toString().isEmpty()){
                if (ele.child(i).attr("type").equals("hidden")){
                    s+=ele.child(i).tag().toString()+"="+ele.attr("value")+"&";
                }
                else{
                    s+=ele.child(i).attr("name")+"="+search_name+"&";
                }
            }
        }
        s=m_webLink+ele.attr("action")+"?"+s.substring(0,s.length()-1);
        System.out.println(s);
        this.setM_searchLink(s);
        return true;
    }

    public String getM_webLink() {
        return m_webLink;
    }
    public void setM_webLink(String m_webLink) {
        this.m_webLink = m_webLink;
    }

    public String getM_searchLink(String bookName) {
        return this.m_searchLink.replaceAll("search_name", bookName);
    }

    //请输入搜索一念永恒后的链接
    public void setM_searchLink(String m_searchLink) {
        this.m_searchLink = m_searchLink.replaceAll("一念永恒", "search_name");
    }

    public String getM_eleName() {
        return m_eleName;
    }
    public void setM_eleName(String m_eleName) {
        this.m_eleName = m_eleName;
    }

}