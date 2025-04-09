package cn.onism.mcp.entity;

import lombok.Data;

/**
 * 联网搜索结果实体类
 *
 * @author Onism
 * @date 2025-04-08
 */
@Data
public class SearchResult {
    private String title;
    private String url;
    private String content;
    private double score;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
