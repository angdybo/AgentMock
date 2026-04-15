package com.dxm.anymock.manager.web.controller;

import com.dxm.anymock.manager.web.WebConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ApiProxyController() {
        this.restTemplate = new RestTemplate();
        // 强制使用 UTF-8 读取响应，避免中文乱码
        restTemplate.getMessageConverters().stream()
                .filter(c -> c instanceof StringHttpMessageConverter)
                .forEach(c -> ((StringHttpMessageConverter) c).setDefaultCharset(StandardCharsets.UTF_8));
    }

    /**
     * 通用 HTTP 代理转发 + 自动保存历史记录
     * POST /anymockweb_api/v2/api_proxy/send
     */
    @PostMapping("/api_proxy/send")
    public ResponseEntity<String> proxyRequest(@RequestBody JsonNode requestNode) {
        try {
            String url = requestNode.get("url").asText();
            String method = requestNode.has("method") ? requestNode.get("method").asText() : "GET";
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            String requestBody = (requestNode.has("body") && !requestNode.get("body").isNull())
                    ? requestNode.get("body").toString() : "";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = requestBody.isEmpty()
                    ? new HttpEntity<>(headers)
                    : new HttpEntity<>(requestBody, headers);

            long startTime = System.currentTimeMillis();
            int statusCode;
            String responseBody;

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
                statusCode = response.getStatusCode().value();
                responseBody = response.getBody() != null ? response.getBody() : "";
            } catch (org.springframework.web.client.HttpClientErrorException | org.springframework.web.client.HttpServerErrorException ex) {
                statusCode = ex.getRawStatusCode();
                responseBody = ex.getResponseBodyAsString() != null ? ex.getResponseBodyAsString() : "";
            }
            long costTime = System.currentTimeMillis() - startTime;

            // 保存历史记录到数据库
            saveHistory(url, method, requestBody, statusCode, responseBody, costTime);

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
