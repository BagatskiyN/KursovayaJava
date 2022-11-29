package example.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Presenter {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(9091);
        Executor threadPool = Executors.newCachedThreadPool();

        while (true) {
            try {
                Socket client = server.accept();
                System.out.println("Accepted: " + client.getRemoteSocketAddress());

                threadPool.execute(() -> handleClient(client));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void handleClient(Socket client) {
        try (Socket c = client) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
                try (PrintWriter writer = new PrintWriter(c.getOutputStream(), true)) {
                    String line = reader.readLine();
                    System.out.println(line);

                    if (line == null) {
                        writer.println("HTTP/1.1 400 Bad Request");
                    } else if (line.startsWith("GET / ")) {
                        String page = "<!DOCTYPE html>\n" +
                                "<html><body>\n" +
                                "<form action=\"/search\">\n" +
                                "<label for=\"fname\">Введіть запит </label>\n" +
                                "<input type=\"text\" name=\"query\" />\n" +
                                "<input type=\"submit\" value=\"Пошук\" />\n" +
                                "</form>\n" +
                                "</body></html>";

                        writer.println("HTTP/1.1 200");
                        writer.println("Content-Type: text/html; charset=UTF-8");
                        writer.println("Content-Length: " + page.getBytes(StandardCharsets.UTF_8).length);
                        writer.println("");
                        writer.println(page);
                    } else if (line.startsWith("GET /search?query=")) {
                        String query = URLDecoder.decode(line.substring("GET /search?query=".length(), line.lastIndexOf(' ')), StandardCharsets.UTF_8.name());
                        String queryEscaped = query.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                        String page = getHtmlSearchResults(query, queryEscaped);
                        writer.println("HTTP/1.1 200");
                        writer.println("Content-Type: text/html; charset=UTF-8");
                        writer.println("Content-Length: " + page.getBytes(StandardCharsets.UTF_8).length);
                        writer.println("");
                        writer.println(page);
                    } else {
                        writer.println("HTTP/1.1 404 Not Found");
                        writer.println("Content-Length: 0");
                        writer.println("");
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getHtmlSearchResults(String query, String queryEscaped) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html><body>\n");
        sb.append("Пошуковий запит: ")
                .append(queryEscaped)
                .append("<br>\n");

        List<ResultEntry> results = getSearchResults(query);
        sb.append("Знайдено сторінок: ")
                .append(results.size())
                .append("<br>\n");

        results.forEach(entry -> {
            sb.append("<p>\n");
            sb.append("<a href=\"")
                    .append(entry.getUrl())
                    .append("\">")
                    .append(entry.getTitle())
                    .append("</a><br>\n");
            sb.append(entry.getDescription());
            sb.append("</p>\n");
        });

        sb.append("</body></html>");
        return sb.toString();
    }
    public static List<ResultEntry> getArticles(String search) {
        List<ResultEntry> result = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/kurs", "root", "TestTest12345$");
            //here sonoo is database name, root is username and password
            Statement stmt = con.createStatement();
            UUID Id = UUID.randomUUID();
            var rs = stmt.executeQuery("SELECT url, COUNT(*) AS Count  FROM articles \n" +
                    "where Word Like '"+ search +
                    "' GROUP BY url\n" + "order by Count desc;");
            System.out.printf("Found", rs);
            List<LinkSearchResult> linkSearchResults = new ArrayList<>();

            while (rs.next()) {
                linkSearchResults.add(new LinkSearchResult(rs.getString("url"),rs.getInt("Count")));
            }
            for(var i=0; i<linkSearchResults.size(); i++)
            {
                var resEnt = stmt.executeQuery("SELECT * FROM kurs.articles where Url like '" + linkSearchResults.get(i).getUrl()+"' Limit 1");
                while (resEnt.next()) {
                    ResultEntry resultEntry = new ResultEntry();
                    resultEntry.setUrl(resEnt.getString("Url"));
                    resultEntry.setTitle(resEnt.getString("Title"));
                    if (resEnt.getString("Text").length() <= 350) {
                        resultEntry.setDescription(resEnt.getString("Text"));

                    } else {
                        resultEntry.setDescription(resEnt.getString("Text").substring(0, 350) + "...");
                    }
                    result.add(resultEntry);
                };
            }


            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return  result;
    }

    // TODO додати справжню реалізацію
    private static List<ResultEntry> getSearchResults(String query) {

        var args = new String[1];
        args[0]= query;
        try {
            Searcher.main(args);
        }
        catch (InterruptedException ex)
        {
            System.out.println("Error");
        }

       return  getArticles(query);
    }
}
