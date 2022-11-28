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
        Deque<String> urls = new LinkedList<>();
        urls.add("https://www.kpi.kharkov.ua");
        while (urls.size() > 0) {
            String url = urls.removeFirst();

            PageInfo pageInfo = processPage(url);
            if (pageInfo == null) {
                // TODO якщо сторінка не була опрацьована,
                //      тоді необхідно опрацювати її пізніше
                System.out.println("URL failed : " + url);
                continue;
            }

            System.out.println("URL   : " + pageInfo.getUrl());
            System.out.println("Title : " + pageInfo.getTitle());
            System.out.println("Words : " + pageInfo.getWords().size() + ", " + pageInfo.getWords().keySet());
            System.out.println("Links : " + pageInfo.getLinks().size());

            // TODO зберегти url сторінки, ії заголовок та іншу
            //      необхідну інформацію в базу даних
                addNewWord("cont", "title", "url");
//            pageInfo.getWords().forEach((word, strings) -> {
//                strings.forEach( x -> addNewWord(word, x, url));
//            });

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
    private static PageInfo processPage(String url) {
        URLConnection connection = null;

        try {
            connection = new URL(url).openConnection();

            String title = null;
            Map<String, List<String>> words = new HashMap<>();
            List<String> links = new ArrayList<>();
            Document doc = Jsoup.connect("https://www.kpi.kharkov.ua/").get();
            var alinks = doc.select("a").stream().map(x->x.attr("href")).filter(x-> x.contains("https://www.kpi.kharkov.ua/")).toArray();
            var title1 = doc.title();

            PageInfo result = new PageInfo();
            result.setUrl(url);
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

    public Map<String, List<String>> getWords(String str, String find) {

        String[] sp = str.split(" +"); // "+" for multiple spaces
        for (int i = 2; i < sp.length; i++) {
            if (sp[i].equals(find)) {
                // have to check for ArrayIndexOutOfBoundsException
                String surr = (i-2 > 0 ? sp[i-2]+" " : "") +
                        (i-1 > 0 ? sp[i-1]+" " : "") +
                        sp[i] +
                        (i+1 < sp.length ? " "+sp[i+1] : "") +
                        (i+2 < sp.length ? " "+sp[i+2] : "");
                System.out.println(surr);
            }
        }
        List<String> resList =  Arrays.stream(sp).toList();
        var res = new HashMap<String, List<String>>();
        res.put(find,resList);
        return res;
    }
}
