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
        Deque<Link> urls = new LinkedList<>();
        urls.add(new Link("https://www.kpi.kharkov.ua", false));
        while (urls.size() > 0) {
            Link url = urls.stream().filter(x -> x.getIsUsed() == false).findFirst().get();

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
            System.out.println("Words : " + pageInfo.getWords().size() + ", " + pageInfo.getWords().keySet());
            System.out.println("Links : " + pageInfo.getLinks().size());

            // TODO зберегти url сторінки, ії заголовок та іншу
            //      необхідну інформацію в базу даних
            pageInfo.getWords().forEach((word, strings) -> {
                strings.forEach( x -> addNewWord(word, x, url.getUrl()));
            });

            // TODO необхідно вжити запобіжний захід, щоб уникнути
            //      повторне сканування однакових сторінок
            urls.addAll(pageInfo.getLinks());

            // TODO затримка для запобігання блокування зі сторони сервера
            //      використання Thread.sleep() не бажано використовувати,
            //      замість цього метода необхідно використовувати інші засоби
            Thread.sleep(1000);
        }
    }
    public static void addNewWord(String content, String title, String url) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/kurs", "root", "TestTest12345$");
        //here sonoo is database name, root is username and password
            Statement stmt = con.createStatement();
            UUID Id = UUID.randomUUID();;
            int rows = stmt.executeUpdate("INSERT into articles (Id, Content, Title, Url) VALUES ('"+Id+"', '"+content+"', '"+title+"', '"+url+"')");
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
            var data = doc.text();
            words = getWords(doc.text(),searchStr);
            PageInfo result = new PageInfo();
            result.setUrl(url.getUrl());
            result.setTitle("title");
            result.setWords(words);
            result.setLinks(links);
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
