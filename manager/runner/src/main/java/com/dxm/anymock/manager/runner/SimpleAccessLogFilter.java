package com.dxm.anymock.manager.runner;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class SimpleAccessLogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        long start = System.currentTimeMillis();
        String method = req.getMethod();
        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        if (qs != null) uri = uri + "?" + qs;

        // 打印请求
        System.out.println("[" + new Date() + "] >>> " + method + " " + uri);

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            // 打印响应（从 header 取 status，避免 body wrapper 问题）
            Integer status = (Integer) req.getAttribute("javax.servlet.error.status_code");
            if (status == null) status = resp.getStatus();
            System.out.println("[" + new Date() + "] <<< " + method + " " + uri + " " + status + " (" + duration + "ms)");
        }
    }

    @Override
    public void destroy() {
    }
}
