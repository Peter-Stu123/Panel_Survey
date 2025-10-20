package org.isoft.panelsurvey.service;

/**
 * 草稿服务接口
 */
public interface DraftService {

    /**
     * 保存草稿到Redis
     * 
     * @param userId    用户ID
     * @param stepType  步骤类型
     * @param draftData 草稿数据
     * @return 是否保存成功
     */
    boolean saveDraft(Long userId, String stepType, Object draftData);

    /**
     * 获取草稿
     * 
     * @param userId   用户ID
     * @param stepType 步骤类型
     * @return 草稿数据
     */
    Object getDraft(Long userId, String stepType);

    /**
     * 删除草稿
     * 
     * @param userId   用户ID
     * @param stepType 步骤类型
     * @return 是否删除成功
     */
    boolean deleteDraft(Long userId, String stepType);

    /**
     * 清除用户所有草稿
     * 
     * @param userId 用户ID
     * @return 是否清除成功
     */
    boolean clearAllDrafts(Long userId);
}

