package com.dxm.anymock.core.biz.service;

/**
 * Mock 数据生成器服务
 * 解析响应体中的模板占位符并生成真实数据
 */
public interface MockDataGeneratorService {

    /**
     * 解析并替换响应体中的数据生成器占位符
     *
     * @param responseBody 原始响应体
     * @return 替换后的响应体
     */
    String parse(String responseBody);
}
