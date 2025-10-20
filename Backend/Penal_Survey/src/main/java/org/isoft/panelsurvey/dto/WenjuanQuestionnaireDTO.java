package org.isoft.panelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 问卷网问卷创建DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WenjuanQuestionnaireDTO {

    /**
     * 问卷标题
     */
    private String title;

    /**
     * 问卷描述
     */
    private String description;

    /**
     * 问卷类型: 1-普通问卷 2-考试问卷 3-报名问卷
     */
    private Integer surveyType;

    /**
     * 问卷设置
     */
    private SurveySetting setting;

    /**
     * 题目列表
     */
    private List<WenjuanQuestionDTO> questions;

    /**
     * 问卷设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveySetting {
        /**
         * 是否允许匿名
         */
        private Boolean allowAnonymous;

        /**
         * 是否显示进度条
         */
        private Boolean showProgress;

        /**
         * 提交后提示语
         */
        private String submitMessage;
    }

    /**
     * 问卷网题目DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WenjuanQuestionDTO {

        /**
         * 题目序号
         */
        private Integer order;

        /**
         * 题目类型
         * radio: 单选题
         * checkbox: 多选题
         * text: 填空题
         * textarea: 多行填空
         * score: 评分题
         * nps: NPS题
         */
        private String type;

        /**
         * 题目标题
         */
        private String title;

        /**
         * 题目描述
         */
        private String description;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 选项列表(单选、多选题使用)
         */
        private List<QuestionOption> options;

        /**
         * 评分最小值(评分题使用)
         */
        private Integer scoreMin;

        /**
         * 评分最大值(评分题使用)
         */
        private Integer scoreMax;

        /**
         * 填空框行数(文本题使用)
         */
        private Integer rows;
    }

    /**
     * 题目选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOption {
        /**
         * 选项ID
         */
        private String id;

        /**
         * 选项文本
         */
        private String text;
    }
}
