package org.isoft.panelsurvey.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionnaire {
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private String introduction;
    private String instruction;
    private Integer version;
    private Integer isAiReviewed;
    private String aiReviewResult;
    private String aiRefinedContent;
    private String status;
    private String surveyUrl; // 问卷网公网链接
    private String qrCodePath;

    // 问卷网集成字段
    private String wenjuanShortId; // 问卷网项目短ID
    private String wenjuanProjectId; // 问卷网项目ID
    private Integer wenjuanRespondentCount; // 问卷网答卷数量
    private LocalDateTime lastSyncTime; // 最后同步时间
    private Integer isWenjuanPublished; // 是否已发布到问卷网 (0-未发布, 1-已发布)

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
