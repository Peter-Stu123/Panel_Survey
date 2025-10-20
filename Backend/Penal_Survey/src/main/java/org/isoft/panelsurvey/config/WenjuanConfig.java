package org.isoft.panelsurvey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 问卷网API配置
 * 问卷网官网: https://www.wenjuan.com
 * 开放平台: https://open.wenjuan.com
 * API文档: https://open.wenjuan.com/docs
 * 
 * 签名规则:
 * 1. 解析URL请求参数，按参数名字母顺序升序排列
 * 2. 按上步结果拼接参数的值，末尾加上app_secret的值
 * 3. 调用MD5算法，得到32位小写字符串
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wenjuan.api")
public class WenjuanConfig {

    /**
     * 问卷网API基础URL (V4版本)
     */
    private String baseUrl = "https://open.wenjuan.com/openapi/v4";

    /**
     * 应用Key (AppKey)
     * 从问卷网开放平台控制台获取
     */
    private String appKey;

    /**
     * 应用密钥 (AppSecret)
     * 从问卷网开放平台控制台获取，请妥善保管，防止泄露
     */
    private String appSecret;

    /**
     * 请求超时时间(毫秒)
     */
    private Integer timeout = 30000;
}
