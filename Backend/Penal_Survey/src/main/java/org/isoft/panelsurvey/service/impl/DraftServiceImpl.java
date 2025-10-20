package org.isoft.panelsurvey.service.impl;

import org.isoft.panelsurvey.service.DraftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 草稿服务实现类
 */
@Service
public class DraftServiceImpl implements DraftService {

    private static final Logger log = LoggerFactory.getLogger(DraftServiceImpl.class);

    // Redis key前缀
    private static final String DRAFT_KEY_PREFIX = "panel_survey:draft:";

    // 草稿过期时间：24小时
    private static final long DRAFT_EXPIRE_HOURS = 24;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成Redis key
     */
    private String generateDraftKey(Long userId, String stepType) {
        return DRAFT_KEY_PREFIX + userId + ":" + stepType;
    }

    @Override
    public boolean saveDraft(Long userId, String stepType, Object draftData) {
        try {
            String key = generateDraftKey(userId, stepType);
            redisTemplate.opsForValue().set(key, draftData, DRAFT_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("草稿保存成功 - 用户ID: {}, 步骤: {}, Key: {}", userId, stepType, key);
            return true;
        } catch (Exception e) {
            log.error("草稿保存失败 - 用户ID: {}, 步骤: {}", userId, stepType, e);
            return false;
        }
    }

    @Override
    public Object getDraft(Long userId, String stepType) {
        try {
            String key = generateDraftKey(userId, stepType);
            Object draftData = redisTemplate.opsForValue().get(key);
            if (draftData != null) {
                log.info("草稿获取成功 - 用户ID: {}, 步骤: {}", userId, stepType);
            } else {
                log.info("未找到草稿 - 用户ID: {}, 步骤: {}", userId, stepType);
            }
            return draftData;
        } catch (Exception e) {
            log.error("草稿获取失败 - 用户ID: {}, 步骤: {}", userId, stepType, e);
            return null;
        }
    }

    @Override
    public boolean deleteDraft(Long userId, String stepType) {
        try {
            String key = generateDraftKey(userId, stepType);
            Boolean result = redisTemplate.delete(key);
            log.info("草稿删除 - 用户ID: {}, 步骤: {}, 结果: {}", userId, stepType, result);
            return result != null && result;
        } catch (Exception e) {
            log.error("草稿删除失败 - 用户ID: {}, 步骤: {}", userId, stepType, e);
            return false;
        }
    }

    @Override
    public boolean clearAllDrafts(Long userId) {
        try {
            String pattern = DRAFT_KEY_PREFIX + userId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("清除所有草稿 - 用户ID: {}, 删除数量: {}", userId, deletedCount);
                return true;
            }
            return true;
        } catch (Exception e) {
            log.error("清除所有草稿失败 - 用户ID: {}", userId, e);
            return false;
        }
    }
}

