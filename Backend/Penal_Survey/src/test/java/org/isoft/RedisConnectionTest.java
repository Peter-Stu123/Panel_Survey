package org.isoft;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis连接测试
 * 用于验证Redis配置是否正确
 */
@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        System.out.println("========== 开始测试Redis连接 ==========");

        // 1. 测试Redis是否可以连接
        assertNotNull(redisTemplate, "RedisTemplate注入失败");
        System.out.println("✓ RedisTemplate注入成功");

        // 2. 测试写入
        String testKey = "panel_survey:test:connection";
        String testValue = "Hello Redis!";

        try {
            redisTemplate.opsForValue().set(testKey, testValue);
            System.out.println("✓ Redis写入成功: " + testKey + " = " + testValue);
        } catch (Exception e) {
            fail("Redis写入失败: " + e.getMessage());
        }

        // 3. 测试读取
        try {
            Object value = redisTemplate.opsForValue().get(testKey);
            assertNotNull(value, "Redis读取失败");
            assertEquals(testValue, value.toString(), "读取的值与写入的值不一致");
            System.out.println("✓ Redis读取成功: " + value);
        } catch (Exception e) {
            fail("Redis读取失败: " + e.getMessage());
        }

        // 4. 测试删除
        try {
            Boolean deleted = redisTemplate.delete(testKey);
            assertTrue(deleted != null && deleted, "Redis删除失败");
            System.out.println("✓ Redis删除成功");
        } catch (Exception e) {
            fail("Redis删除失败: " + e.getMessage());
        }

        // 5. 验证删除
        Object value = redisTemplate.opsForValue().get(testKey);
        assertNull(value, "删除后仍能读取到数据");
        System.out.println("✓ 验证删除成功");

        System.out.println("========== Redis连接测试全部通过！ ==========");
        System.out.println("\nRedis配置信息:");
        System.out.println("- Redis已正确连接");
        System.out.println("- 可以正常读写数据");
        System.out.println("- 草稿功能已经可以使用！");
    }

    @Test
    public void testDraftKeyPattern() {
        System.out.println("========== 测试草稿Key格式 ==========");

        Long userId = 1L;
        String stepType = "step1";
        String draftKey = "panel_survey:draft:" + userId + ":" + stepType;

        // 模拟草稿数据
        String draftData = "{\"projectName\":\"测试项目\",\"objectiveType\":\"treatment_preference\"}";

        // 保存
        redisTemplate.opsForValue().set(draftKey, draftData);
        System.out.println("✓ 保存草稿: " + draftKey);

        // 读取
        Object retrieved = redisTemplate.opsForValue().get(draftKey);
        assertNotNull(retrieved);
        System.out.println("✓ 读取草稿: " + retrieved);

        // 清理
        redisTemplate.delete(draftKey);
        System.out.println("✓ 清理测试数据");

        System.out.println("========== 草稿Key格式测试通过！ ==========");
    }
}

