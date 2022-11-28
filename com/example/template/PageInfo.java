package example.template;

import java.util.List;
import java.util.Map;

public class PageInfo {
    private String url;
    private String title;
    private Map<String, List<String>> words;
    private List<Link> links;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, List<String>> getWords() {
        return words;
    }

    public void setWords(Map<String, List<String>> words) {
        this.words = words;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
