package example.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    // TODO додати справжню реалізацію
    private static List<ResultEntry> getSearchResults(String query) {
        ResultEntry e1 = new ResultEntry();
        e1.setUrl("https://www.kpi.kharkov.ua/ukr/department/informatsijni-systemy-ta-tehnologiyi");
        e1.setTitle("Інформаційні системи та технології");
        e1.setDescription("Кафедра інформаційних систем та технологій заснована у січні 2022 року на базі кафедри програмної інженерії та інформаційних систем управління з метою підготовки спеціалістів за спеціальністю 126 – «Інформаційні системи та технології».");

        ResultEntry e2 = new ResultEntry();
        e2.setUrl("https://web.kpi.kharkov.ua/ist/uk/2022/05/01/uk-active-interaction-of-ntu-khpi-with-bratislava-university-of-economics-and-management");
        e2.setTitle("Активна взаємодія НТУ “ХПІ” з Братиславським університетом економіки та менеджменту");
        e2.setDescription("Наступного тижня запланована онлайн-зустріч з бакалаврами 3 та 4 курсів українських університетів, які планують продовжувати навчання в магістратурі за освітньою програмою 126 «програмне забезпечення інформаційних систем», з метою обговорення вже більш конкретних кроків допомоги українським вишам та їх студентам.");

        switch ((int) (Math.random() * 5)) {
            case 1:
                return Collections.singletonList(e1);
            case 2:
                return Collections.singletonList(e2);
            case 3:
                return Arrays.asList(e1, e2);
            case 4:
                return Arrays.asList(e2, e1);
            default:
                return Collections.emptyList();
        }
    }
}
