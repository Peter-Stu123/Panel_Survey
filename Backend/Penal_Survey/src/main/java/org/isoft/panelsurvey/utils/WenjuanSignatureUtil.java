package org.isoft.panelsurvey.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 问卷网API签名工具类
 * 
 * 签名规则（官方文档）:
 * 1. 解析URL请求参数，按参数名字母顺序升序排列
 * 2. 按上步结果拼接参数的值，末尾加上app_secret的值，得到待签名字符串
 * 3. 调用MD5算法，得到32位小写字符串
 * 
 * 注意事项:
 * - signature本身不参与签名
 * - 参数值使用转义前的字符串
 * - MD5算法使用32位小写字符串
 */
@Slf4j
public class WenjuanSignatureUtil {

    /**
     * 生成问卷网API签名
     * 
     * @param params    请求参数（不包含signature）
     * @param appSecret 应用密钥
     * @return MD5签名（32位小写）
     */
    public static String generateSignature(Map<String, String> params, String appSecret) {
        try {
            // 1. 按参数名字母顺序升序排列（TreeMap自动排序）
            TreeMap<String, String> sortedParams = new TreeMap<>(params);

            // 2. 拼接参数值
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                sb.append(entry.getValue());
                log.info("[签名调试] 参数: {} = {}", entry.getKey(), entry.getValue());
            }

            // 3. 末尾加上app_secret
            sb.append(appSecret);

            // 4. MD5加密
            String signString = sb.toString();
            String signature = md5(signString);

            log.info("[签名调试] 待签名字符串: {}", signString);
            log.info("[签名调试] 签名结果: {}", signature);

            return signature;

        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * MD5加密（32位小写）
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // 转换为32位小写十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toLowerCase();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * URL参数编码（用于GET请求）
     * 注意：空格转义为%20，加号+转义为%2B
     */
    public static String encodeURIComponent(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20") // 空格应该编码为%20而不是+
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    /**
     * 构建带签名的GET请求URL
     * 
     * @param baseUrl   API基础URL
     * @param params    请求参数（不包含signature）
     * @param appSecret 应用密钥
     * @return 完整的请求URL（包含签名）
     */
    public static String buildSignedUrl(String baseUrl, Map<String, String> params, String appSecret) {
        // 生成签名
        String signature = generateSignature(params, appSecret);

        // 构建URL
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?");

        // 添加参数（需要URL编码）
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            url.append(entry.getKey()).append("=")
                    .append(encodeURIComponent(entry.getValue()))
                    .append("&");
        }

        // 添加签名
        url.append("signature=").append(signature);

        return url.toString();
    }
}
