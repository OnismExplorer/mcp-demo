package cn.onism.mcp.service.search;

import cn.hutool.json.JSONUtil;
import cn.onism.mcp.entity.SearchResult;
import cn.onism.mcp.entity.SearxngResponse;
import groovy.json.StringEscapeUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SEARXNG 搜索服务
 *
 * @author Onism
 * @date 2025-04-08
 */
@Service
@Slf4j
public class SearxngSearchService {

    @Value("${spring.ai.websearch.searxng.url}")
    private String SEARXNG_URL;

    @Value("${spring.ai.websearch.searxng.nums:20}")
    private int NUMS;

    private final OkHttpClient httpClient;

    public SearxngSearchService() {
        this.httpClient = buildRequest();
    }

    private OkHttpClient buildRequest() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 联网实时搜索
     *
     * @param question 问题
     * @return {@link List }<{@link SearchResult }>
     */
    public List<SearchResult> search(String question) {
        HttpUrl url = HttpUrl.get(SEARXNG_URL).newBuilder()
                // 搜索问题
                .addQueryParameter("q", question)
                // 返回结果格式
                .addQueryParameter("format", "json")
                .build();
        log.info("搜索链接 => {}", url.url());

        Request request = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: HTTP " + response.code());
            }
            if (response.body() != null) {
                // 获取相应结果
                String responseBody = response.body().string();
                // 记录搜索结果的前 200 个字符
                log.info("搜索结果 <= {}", StringUtils.abbreviate(StringEscapeUtils.unescapeJava(responseBody),200));
                return parseResults(responseBody);
            }
            log.error("搜索失败：{}",response.message());
            return Collections.emptyList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析搜索相应结果
     *
     * @param resultJson 结果 JSON 字符串
     * @return {@link List }<{@link SearchResult }>
     */
    private List<SearchResult> parseResults(String resultJson) {
        if (StringUtils.isBlank(resultJson)) {
            return Collections.emptyList();
        }

        List<SearchResult> results = JSONUtil.toBean(resultJson, SearxngResponse.class).getResults();
        results = results.subList(0,Math.min(NUMS,results.size()))
                .parallelStream()
                // 按score降序排序
                .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                // 截取前num个元素
                .limit(NUMS).toList();
        return results;
    }

}
