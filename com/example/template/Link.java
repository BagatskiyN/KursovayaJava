package example.template;

import java.util.List;
import java.util.Map;

public class Link {
    public Link(String url, Boolean isUsed) {
        this.url = url;
        this.isUsed = isUsed;
    }

    public Link() {
    }

    private String url;
    private Boolean isUsed;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsUsed() {
        return  isUsed;
    }

    public void setIsUsed(Boolean val) {
        this. isUsed = val;
    }
}
