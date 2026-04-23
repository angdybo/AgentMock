package com.dxm.anymock.common.dal.entity;

import java.util.Date;

public class HttpInterfaceCallLogDO {
    private Long id;

    private Long httpInterfaceId;

    private String requestUri;

    private String requestMethod;

    private String requestHeaders;

    private String requestBody;

    private String responseBody;

    private String responseHeaders;

    private Integer responseStatus;

    private Long costTime;

    private String async;

    private Date gmtCreate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHttpInterfaceId() {
        return httpInterfaceId;
    }

    public void setHttpInterfaceId(Long httpInterfaceId) {
        this.httpInterfaceId = httpInterfaceId;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri == null ? null : requestUri.trim();
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod == null ? null : requestMethod.trim();
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders == null ? null : requestHeaders.trim();
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody == null ? null : requestBody.trim();
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody == null ? null : responseBody.trim();
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders == null ? null : responseHeaders.trim();
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }

    public String getAsync() {
        return async;
    }

    public void setAsync(String async) {
        this.async = async == null ? null : async.trim();
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
}
