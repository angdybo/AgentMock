package com.dxm.anymock.manager.runner;

import org.apache.catalina.valves.AccessLogValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public FilterRegistrationBean<SimpleAccessLogFilter> simpleAccessLogFilter() {
        System.err.println("=== SimpleAccessLogFilter Bean created ===");
        FilterRegistrationBean<SimpleAccessLogFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new SimpleAccessLogFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Integer.MAX_VALUE);
        return reg;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogValve() {
        return factory -> {
            AccessLogValve valve = new AccessLogValve();
            valve.setDirectory("/app/logs");
            valve.setPrefix("access_log.");
            valve.setSuffix(".log");
            valve.setPattern("%h %l %u %t \"%r\" %s %b %D");
            valve.setRotatable(true);
            valve.setFileDateFormat("yyyy-MM-dd");
            valve.setBuffered(false);
            factory.addEngineValves(valve);
        };
    }
}
