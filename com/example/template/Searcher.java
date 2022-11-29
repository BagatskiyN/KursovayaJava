package example.template;

//TestTest12345$ - password
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.*;

import static java.rmi.server.LogStream.log;

public class Searcher {
    public static void main(String[] args) throws InterruptedException {
        List<Link> urls = new ArrayList<>();
        urls.add(new Link("https://www.kpi.kharkov.ua", false));
        long start = System.currentTimeMillis();
        long end = start + 9000;
        while (urls.stream().filter(x-> x.getIsUsed()==false).toList().size() > 0 && System.currentTimeMillis() < end) {
            Link url = urls.stream().filter(x -> x.getIsUsed() == false).findFirst().get();
            System.out.println(System.currentTimeMillis() < end);
            PageInfo pageInfo = processPage(url, args[0]);
            if (pageInfo == null) {
                // TODO якщо сторінка не була опрацьована,
                //      тоді необхідно опрацювати її пізніше
                System.out.println("URL failed : " + url);
                continue;
            }
            url.setIsUsed(true);
            System.out.println("URL   : " + pageInfo.getUrl());
            System.out.println("Title : " + pageInfo.getTitle());
            if(pageInfo.getWords() != null)
            {
                System.out.println("Words : " + pageInfo.getWords().size() + ", " + pageInfo.getWords().keySet());
            }
            if(pageInfo.getLinks() != null)
            {
                System.out.println("Links : " + pageInfo.getLinks().size());
            }
            // TODO зберегти url сторінки, ії заголовок та іншу
            //      необхідну інформацію в базу даних
            pageInfo.getWords().forEach((word, strings) -> {
                strings.forEach( x -> addNewWord(x, pageInfo.getTitle(), url.getUrl(), word, pageInfo.getText()));
            });

            // TODO необхідно вжити запобіжний захід, щоб уникнути
            //      повторне сканування однакових сторінок
            if(pageInfo.getLinks()!=null)
            {
                urls = addUniqueLink(urls, pageInfo.getLinks());
            }
            Thread.sleep(50);
        }
    }

    public  static List<Link> addUniqueLink(List<Link> links, List<Link> newLinks)
    {
        newLinks.forEach((x)->
        {
            if(!links.stream().map(p->p.getUrl()).toList().contains(x.getUrl()))
            {
                links.add(x);
            }
        });
        return  links;
    }

    public static void addNewWord(String content, String title, String url, String word, String text) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/kurs", "root", "TestTest12345$");
        //here sonoo is database name, root is username and password
            Statement stmt = con.createStatement();
            UUID Id = UUID.randomUUID();;
            int rows = stmt.executeUpdate("INSERT into articles (Id, Content, Title, Url, Word, Text) VALUES ('"+Id+"', '"+content+"', '"+title+"', '"+url+"', '"+word+"', '"+text+"')");
            System.out.printf("Added %d rows", rows);
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // TODO додати справжню реалізацію
    private static PageInfo processPage(Link url, String searchStr) {
        URLConnection connection = null;

        try {
            connection = new URL(url.getUrl()).openConnection();

            String title = null;
            Map<String, List<String>> words = new HashMap<>();
            List<Link> links = new ArrayList<>();
            Document doc = Jsoup.connect(url.getUrl()).get();
            links = doc.select("a")
                    .stream()
                    .map(x->x.attr("href"))
                    .filter(x-> x.contains("https://www.kpi.kharkov.ua/"))
                    .map(x -> new Link(x, false))
                    .toList();
            var title1 = doc.title();
            words = getWords(doc.text(),searchStr);
            PageInfo result = new PageInfo();
            result.setUrl(url.getUrl());
            result.setTitle(doc.title());
            result.setWords(words);
            result.setLinks(links);
            result.setText(doc.text());
            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        }
    }

    public static Map<String, List<String>> getWords(String str, String find) {

        List<String> searchList = new ArrayList<>();
        find = find.toLowerCase();
        str = str.toLowerCase();
        int countWords = 2;
        String[] sp = str.split(" +"); // "+" for multiple spaces
        for (int i = 0; i < sp.length; i++) {
            if (sp[i].equals(find)) {

                String before = "";
                for (int j = countWords; j > 0; j--) {
                    if(i-j >= 0) before += sp[i-j]+" ";
                }

                String after = "";
                for (int j = 1; j <= countWords; j++) {
                    if(i+j < sp.length) after += " " + sp[i+j];
                }
                String searhResult = before + find + after;
                searchList.add(searhResult);
            }
        }

        List<String> resList =  searchList.stream().toList();
        var res = new HashMap<String, List<String>>();
        res.put(find,resList);
        return res;
    }
}
