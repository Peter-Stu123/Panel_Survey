package org.isoft.panelsurvey.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Favicon 控制器
 * 用于处理浏览器对 favicon.ico 的请求
 */
@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // 返回 204 No Content，避免 favicon.ico 找不到的警告
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
