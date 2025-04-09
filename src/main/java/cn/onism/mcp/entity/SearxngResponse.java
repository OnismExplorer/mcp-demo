package cn.onism.mcp.entity;

import lombok.Data;

import java.util.List;

/**
 * searxng 响应体
 *
 * @author Onism
 * @date 2025-04-08
 */
@Data
public class SearxngResponse {
    private String query;
    private List<SearchResult> results;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }
}
