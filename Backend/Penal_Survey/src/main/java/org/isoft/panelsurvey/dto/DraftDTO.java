package org.isoft.panelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 草稿数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftDTO {
    private String draftKey; // 草稿的唯一键
    private Object draftData; // 草稿数据（可以是Step1DTO、Step2DTO等）
    private String stepType; // 步骤类型：step1, step2, step3等
}

