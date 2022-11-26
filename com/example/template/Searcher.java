package example.template;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

            pageInfo.getWords().forEach((word, testAroundWord) -> {
                // TODO зберегти кожне слово та текст навколо нього в базу даних
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

    // TODO додати справжню реалізацію
    private static PageInfo processPage(String url) {
        URLConnection connection = null;

        try {
            connection = new URL(url).openConnection();

            String title = null;
            Map<String, List<String>> words = new HashMap<>();
            List<String> links = new ArrayList<>();
            Document doc = Jsoup.connect("https://www.kpi.kharkov.ua/").get();
            System.out.println(doc.title());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {

                    // TODO тут необхідно реалізувати парсінг вмісту web-сторінки
                    //      порядковий або більш продвинутий
                    //      1. потрібно знайти заголовок сторінки
                    //      2. потрібно знайти слова та оточуючий текст
                    //      3. потрібно знайти всі посилання
                }

                // TODO цей блок коду наведений виключно для прикладу, його необхідно видалити
                if (true) {
                    Random random = new Random();

                    title = "Випадковий заголовок #" + doc.title();

                    String w1 = Integer.toHexString(random.nextInt());
                    String w2 = Integer.toHexString(random.nextInt());
                    String w3 = Integer.toHexString(random.nextInt());

                    for (int i = 0, n = 10 + random.nextInt(5); i < n; ++i) {
                        words.computeIfAbsent(w2, $ -> new ArrayList<>())
                                .add(w1 + " " + w2 + " " + w3);

                        w1 = w2;
                        w2 = w3;
                        w3 = Integer.toHexString(random.nextInt());
                    }

                    if (random.nextBoolean()) {
                        links.add("https://www.kpi.kharkov.ua/ukr/sajti/");
                    }

                    if (random.nextBoolean()) {
                        links.add("https://www.kpi.kharkov.ua/ukr/ntu-hpi/kontakti/");
                    }

                    if (random.nextBoolean()) {
                        links.add("https://www.kpi.kharkov.ua/ukr/osvita/fakulteti/");
                    }

                    if (random.nextBoolean()) {
                        links.add("https://www.kpi.kharkov.ua/ukr/category/novini/");
                    }

                    if (random.nextBoolean()) {
                        links.add("https://www.kpi.kharkov.ua/ukr/category/anonsi/");
                    }

                    Collections.shuffle(links, random);
                }
            }

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
}
