package com.dxm.anymock.manager.web.controller;

import com.dxm.anymock.manager.web.WebConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * API 调试代理接口
 * 前端通过此接口转发 HTTP 请求，解决跨域问题
 * 同时提供请求历史记录的持久化存储
 */
@RestController
@RequestMapping(WebConstants.URL_PREFIX_API_V2)
public class ApiProxyController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient;

    public ApiProxyController() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(30000)
                .build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 通用 HTTP 代理转发 + 自动保存历史记录
     * POST /anymockweb_api/v2/api_proxy/send
     */
    @PostMapping("/api_proxy/send")
    public ResponseEntity<String> proxyRequest(@RequestBody JsonNode requestNode) {
        long startTime = System.currentTimeMillis();
        CloseableHttpResponse response = null;
        try {
            String urlStr = requestNode.get("url").asText();
            String method = requestNode.has("method") ? requestNode.get("method").asText().toUpperCase() : "GET";
            String requestBody = (requestNode.has("body") && !requestNode.get("body").isNull())
                    ? requestNode.get("body").asText() : "";

            HttpRequestBase httpRequest;
            if ("GET".equals(method)) {
                httpRequest = new HttpGet(urlStr);
            } else if ("POST".equals(method)) {
                HttpPost post = new HttpPost(urlStr);
                if (!requestBody.isEmpty()) {
                    post.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                }
                httpRequest = post;
            } else if ("PUT".equals(method)) {
                HttpPut put = new HttpPut(urlStr);
                if (!requestBody.isEmpty()) {
                    put.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                }
                httpRequest = put;
            } else if ("DELETE".equals(method)) {
                httpRequest = new HttpDelete(urlStr);
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }

            // 转发自定义请求头（如 Authorization、Content-Type）
            if (requestNode.has("headers") && requestNode.get("headers").isObject()) {
                requestNode.get("headers").fieldNames().forEachRemaining(key -> {
                    JsonNode val = requestNode.get("headers").get(key);
                    if (val != null && !val.isNull() && !val.asText().isEmpty()) {
                        httpRequest.setHeader(key, val.asText());
                    }
                });
            }
            // POST 请求默认加上 Content-Type（如果前端没传）
            if (!"GET".equals(method) && requestNode.has("headers") && requestNode.get("headers").has("Content-Type")
                    && (requestNode.get("headers").get("Content-Type").isNull() || requestNode.get("headers").get("Content-Type").asText().isEmpty())) {
                httpRequest.setHeader("Content-Type", "application/json");
            }

            response = httpClient.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseBody = (entity != null) ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";
            long costTime = System.currentTimeMillis() - startTime;

            // 保存历史记录
            saveHistory(urlStr, method, requestBody, statusCode, responseBody, costTime);

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("status", statusCode);
            result.put("body", responseBody);
            result.put("costTime", costTime);
            return ResponseEntity.ok(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            try {
                Map<String, String> errResult = new java.util.LinkedHashMap<>();
                errResult.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errResult));
            } catch (Exception ex2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"internal error\"}");
            }
        } finally {
            if (response != null) {
                try { response.close(); } catch (IOException ignored) {}
            }
        }
    }

    /** 查询历史记录列表（最新50条） */
    @GetMapping("/api_proxy/history/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        try {
            // 建表（如果不存在）
            ensureTableExists();

            int offset = (page - 1) * pageSize;
            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "SELECT id,url,method,request_body,response_status,cost_time,create_time FROM anymock_api_tester_history ORDER BY create_time DESC LIMIT ? OFFSET ?",
                    pageSize, offset);
            Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM anymock_api_tester_history", Integer.class);

            result.put("resultCode", "000000");
            result.put("resultMsg", "成功");
            result.put("data", list);
            result.put("total", total != null ? total : 0);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("resultCode", "999999");
            result.put("resultMsg", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /** 删除单条历史记录 */
    @DeleteMapping("/api_proxy/history/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteHistory(@PathVariable Long id) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        try {
            jdbcTemplate.update("DELETE FROM anymock_api_tester_history WHERE id = ?", id);
            result.put("resultCode", "000000");
            result.put("resultMsg", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("resultCode", "999999");
            result.put("resultMsg", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /** 清空所有历史记录 */
    @DeleteMapping("/api_proxy/history/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearHistory() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        try {
            jdbcTemplate.update("DELETE FROM anymock_api_tester_history WHERE 1=1");
            result.put("resultCode", "000000");
            result.put("resultMsg", "清空成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("resultCode", "999999");
            result.put("resultMsg", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /** 保存历史记录 */
    private void saveHistory(String url, String method, String requestBody, int statusCode, String responseBody, long costTime) {
        try {
            ensureTableExists();
            // 截断过长的字段避免报错
            String safeUrl = url.length() > 500 ? url.substring(0, 500) : url;
            String safeBody = (requestBody.length() > 10000 ? requestBody.substring(0, 10000) : requestBody).replace("'", "\\'");
            String safeResp = (responseBody.length() > 50000 ? responseBody.substring(0, 50000) : responseBody).replace("'", "\\'");

            jdbcTemplate.update(
                    "INSERT INTO anymock_api_tester_history(url,method,request_body,response_status,response_body,cost_time,create_time) VALUES(?,?,?,?,?,?,NOW())",
                    safeUrl, method, safeBody, statusCode, safeResp, costTime);
        } catch (Exception ignored) {
            // 保存失败不影响主流程
        }
    }

    /** 确保表存在 */
    private void ensureTableExists() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `anymock_api_tester_history` (\n" +
                    "`id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                    "`url` varchar(500) NOT NULL,\n" +
                    "`method` varchar(10) NOT NULL DEFAULT 'GET',\n" +
                    "`request_body` mediumtext,\n" +
                    "`response_status` int(11),\n" +
                    "`response_body` longtext,\n" +
                    "`cost_time` int(11),\n" +
                    "`create_time` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                    "PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (Exception ignored) {}
    }
}
