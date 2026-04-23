package com.dxm.anymock.core.biz.service.impl;

import com.dxm.anymock.common.base.enums.ResultCode;
import com.dxm.anymock.common.base.exception.BizException;
import com.dxm.anymock.common.base.interceptor.MdcManager;
import com.dxm.anymock.common.dal.HttpInterfaceCacheManager;
import com.dxm.anymock.common.dal.dao.HttpInterfaceCallLogDao;
import com.dxm.anymock.common.dal.dao.HttpInterfaceDao;
import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDO;
import com.dxm.anymock.common.dal.model.HttpInterfaceBO;
import com.dxm.anymock.common.dal.model.HttpInterfaceKeyBO;
import com.dxm.anymock.core.biz.HttpMockContext;
import com.dxm.anymock.core.biz.service.HttpAsyncMockService;
import com.dxm.anymock.core.biz.service.HttpMockService;
import com.dxm.anymock.core.biz.service.HttpSyncMockService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@Service
public class HttpMockServiceImpl implements HttpMockService {

    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final Logger logger = LoggerFactory.getLogger(HttpMockServiceImpl.class);

    private static final String BODY = "body";

    private String buildRequestHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            sb.append(key).append(":").append(request.getHeader(key)).append("\n");
        }
        return sb.toString();
    }

    private void recordCallLog(HttpServletRequest request, HttpServletResponse response, boolean async) {
        try {
            HttpInterfaceCallLogDO logDO = new HttpInterfaceCallLogDO();
            logDO.setHttpInterfaceId(request.getAttribute("_httpInterfaceId") != null
                    ? (Long) request.getAttribute("_httpInterfaceId") : null);
            logDO.setRequestUri(request.getRequestURI());
            logDO.setRequestMethod(request.getMethod());
            logDO.setRequestHeaders(request.getAttribute("_requestHeaders") != null
                    ? (String) request.getAttribute("_requestHeaders") : buildRequestHeaders(request));
            logDO.setRequestBody((String) request.getAttribute(BODY));
            logDO.setResponseBody((String) request.getAttribute("_responseBody"));
            logDO.setResponseStatus(response.getStatus());
            logDO.setAsync(String.valueOf(async));
            
            // 计算耗时
            Long startTime = (Long) request.getAttribute("_startTime");
            if (startTime != null) {
                long costTime = System.currentTimeMillis() - startTime;
                logDO.setCostTime(costTime);
            }
            
            logDO.setGmtCreate(new Date());
            httpInterfaceCallLogDao.insert(logDO);
        } catch (Exception e) {
            logger.warn("Failed to record call log", e);
        }
    }

    @Autowired
    private HttpInterfaceDao httpInterfaceDao;

    @Autowired
    private HttpInterfaceCallLogDao httpInterfaceCallLogDao;

    @Autowired
    private HttpInterfaceCacheManager httpInterfaceCacheManager;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private HttpSyncMockService httpSyncMockService;

    @Autowired
    private HttpAsyncMockService httpAsyncMockService;

    private String buildHttpBody(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        String method = request.getMethod();
        String requestEncoding =  request.getCharacterEncoding();

        // 判断是否为x-www-form-urlencoded模式
        boolean isFormPost = (contentType != null && contentType.contains(FORM_CONTENT_TYPE) &&
                HttpMethod.POST.matches(method));
        if (isFormPost) {
            // 通过读取getParameterMap反构造出body
            StringBuilder content = new StringBuilder();
            Map<String, String[]> form = request.getParameterMap();
            for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext(); ) {
                String name = nameIterator.next();
                List<String> values = Arrays.asList(form.get(name));
                for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
                    String value = valueIterator.next();
                    content.append(URLEncoder.encode(name, requestEncoding));
                    if (value != null) {
                        content.append('=');
                        content.append(URLEncoder.encode(value, requestEncoding));
                        if (valueIterator.hasNext()) {
                            content.append('&');
                        }
                    }
                }
                if (nameIterator.hasNext()) {
                    content.append('&');
                }
            }
            return content.toString();
        } else {
            // 直接读取输入流获取body
            return IOUtils.toString(request.getInputStream(), requestEncoding);
        }
    }

    private HttpInterfaceBO loadHttpInterfaceBO(HttpInterfaceKeyBO httpInterfaceKeyBO) {
        logger.info("Loading HTTP interface from redis...");
        HttpInterfaceBO httpInterfaceBO = httpInterfaceCacheManager.get(httpInterfaceKeyBO);
        if (httpInterfaceBO == null) {
            logger.info("Loading HTTP interface from DB...");
            httpInterfaceBO = httpInterfaceDao.queryByKey(httpInterfaceKeyBO);
            if (httpInterfaceBO == null) {
                throw new BizException(ResultCode.NOT_FOUND_HTTP_INTERFACE);
            }
            httpInterfaceCacheManager.set(httpInterfaceBO);
        }

        if (BooleanUtils.isNotTrue(httpInterfaceBO.getStart())) {
            throw new BizException(ResultCode.HTTP_INTERFACE_NOT_OPEN);
        }
        return httpInterfaceBO;
    }

    private String buildRawHttpMsg(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(request.getMethod());
        stringBuilder.append(" ");
        stringBuilder.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            stringBuilder.append("?");
            stringBuilder.append(request.getQueryString());
        }
        stringBuilder.append("\n");

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String)headerNames.nextElement();
            stringBuilder.append(key);
            stringBuilder.append(":");
            stringBuilder.append(request.getHeader(key));
            stringBuilder.append("\n");
        }
        stringBuilder.append("\n");
        stringBuilder.append(request.getAttribute(BODY));
        return stringBuilder.toString();
    }

    private MockHttpServletRequest buildMockRequest(HttpServletRequest request) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameters(request.getParameterMap());

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String)headerNames.nextElement();
            mockRequest.addHeader(key, request.getHeader(key));
        }

        mockRequest.setMethod(request.getMethod());
        mockRequest.setRequestURI(request.getRequestURI());
        mockRequest.setRemoteAddr(request.getRemoteAddr());
        mockRequest.setRemoteHost(request.getRemoteHost());
        mockRequest.setRemotePort(request.getRemotePort());
        mockRequest.setRemoteUser(request.getRemoteUser());
        mockRequest.setAttribute(BODY, request.getAttribute(BODY));
        return mockRequest;
    }

    @Override
    public void mock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute("_startTime", startTime);

        HttpMockContext context = new HttpMockContext();

        // 加载HTTP接口数据
        HttpInterfaceKeyBO httpInterfaceKeyBO = new HttpInterfaceKeyBO();
        httpInterfaceKeyBO.setRequestMethod(request.getMethod());
        httpInterfaceKeyBO.setRequestUri(request.getRequestURI());
        HttpInterfaceBO httpInterfaceBO = loadHttpInterfaceBO(httpInterfaceKeyBO);
        context.setHttpInterfaceBO(httpInterfaceBO);

        // 由于输入流能且仅能读取一次，而后续可能多次调用，因此需要临时存储
        request.setAttribute(BODY, buildHttpBody(request));

        // 存储接口ID和请求头用于日志记录
        request.setAttribute("_httpInterfaceId", httpInterfaceBO.getId());
        request.setAttribute("_requestHeaders", buildRequestHeaders(request));

        if (logger.isInfoEnabled()) {
            logger.info("\n################### HTTP REQUEST ###################\n"
                         + buildRawHttpMsg(request)
                      + "\n####################################################");
        }

        // 同步
        httpSyncMockService.mock(context, request, response);

        // 同步日志记录
        recordCallLog(request, response, false);

        // 异步
        if (BooleanUtils.isTrue(httpInterfaceBO.getNeedAsyncCallback())) {
            String mdcTraceId = MDC.get(MdcManager.MDC_TRACE_ID_KEY);
            MockHttpServletRequest mockRequest = buildMockRequest(request);
            mockRequest.setAttribute("_httpInterfaceId", httpInterfaceBO.getId());
            mockRequest.setAttribute("_requestHeaders", buildRequestHeaders(request));
            mockRequest.setAttribute(BODY, request.getAttribute(BODY));
            threadPoolTaskExecutor.execute(() -> {
                try {
                    MDC.put(MdcManager.MDC_TRACE_ID_KEY, mdcTraceId);
                    httpAsyncMockService.mock(context, mockRequest);
                    // 异步日志记录
                    recordCallLog(mockRequest, response, true);
                    MDC.clear();
                } catch (Exception e) {
                    logger.warn("", e);
                }
            });
        }
    }
}
