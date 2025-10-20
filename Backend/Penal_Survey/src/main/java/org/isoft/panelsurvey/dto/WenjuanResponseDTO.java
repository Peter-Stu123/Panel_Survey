package org.isoft.panelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问卷网API响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WenjuanResponseDTO<T> {

    /**
     * 响应码: 0-成功 其他-失败
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 问卷网创建成功响应数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WenjuanCreateResult {

        /**
         * 问卷ID
         */
        private String surveyId;

        /**
         * 问卷访问链接 (公网链接)
         */
        private String surveyUrl;

        /**
         * 问卷短链接
         */
        private String shortUrl;

        /**
         * 问卷二维码链接
         */
        private String qrcodeUrl;

        /**
         * 问卷管理后台链接
         */
        private String manageUrl;

        /**
         * 创建时间
         */
        private String createTime;
    }
}
