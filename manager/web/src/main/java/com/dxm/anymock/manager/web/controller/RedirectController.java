package com.dxm.anymock.manager.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController {

    @RequestMapping("/")
    public String redirect() {
        return "forward:/fe/index.html";
    }

    /** SPA 路由 fallback：所有非API、非静态资源路径都返回 index.html（支持 Vue Router history 模式） */
    @RequestMapping("/{path:[^\\.]*}")
    public String spaFallback() {
        return "forward:/fe/index.html";
    }
}
