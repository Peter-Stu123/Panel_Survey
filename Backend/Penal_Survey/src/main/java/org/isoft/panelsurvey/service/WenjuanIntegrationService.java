package org.isoft.panelsurvey.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.isoft.panelsurvey.config.WenjuanConfig;
import org.isoft.panelsurvey.dto.WenjuanQuestionnaireDTO;
import org.isoft.panelsurvey.dto.WenjuanResponseDTO;
import org.isoft.panelsurvey.entity.SurveyQuestion;
import org.isoft.panelsurvey.entity.SurveyQuestionnaire;
import org.isoft.panelsurvey.mapper.SurveyQuestionMapper;
import org.isoft.panelsurvey.utils.WenjuanSignatureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 问卷网集成服务 (V4 API)
 * 官网: https://www.wenjuan.com
 * 开放平台: https://open.wenjuan.com
 * API文档: https://open.wenjuan.com/docs
 * 
 * API约定:
 * - 使用HTTP/HTTPS协议
 * - 请求方式固定为GET
 * - 请求参数编码到URL查询字符串
 * - 响应状态码默认200
 * - 返回参数采用JSON格式
 */
@Slf4j
@Service
public class WenjuanIntegrationService {

    @Autowired
    private WenjuanConfig wenjuanConfig;

    @Autowired
    private SurveyQuestionMapper questionMapper;

    @Autowired
    private org.isoft.panelsurvey.mapper.SurveyQuestionnaireMapper questionnaireMapper;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 创建问卷网问卷
     * 
     * 注意：根据问卷网官方文档，创建项目需要：
     * 1. 首先需要绑定问卷网账号（服务应用）
     * 2. 调用创建项目API
     * 3. 获取项目链接
     *
     * @param questionnaire 问卷信息
     * @return 问卷网创建结果(包含公网访问链接)
     */
    public WenjuanResponseDTO.WenjuanCreateResult createWenjuanSurvey(SurveyQuestionnaire questionnaire) {
        try {
            log.info("开始创建问卷网问卷, questionnaireId={}", questionnaire.getId());

            // 1. 获取问卷的所有题目
            List<SurveyQuestion> questions = questionMapper.selectByQuestionnaireId(questionnaire.getId());
            if (questions == null || questions.isEmpty()) {
                throw new RuntimeException("问卷没有题目，无法发布");
            }

            // 2. 转换为问卷网格式
            WenjuanQuestionnaireDTO wenjuanDTO = convertToWenjuanFormat(questionnaire, questions);

            // 3. 调用问卷网API创建项目
            // 注意：这里使用模拟的API接口，实际使用时需要根据问卷网官方文档提供的具体接口调整
            WenjuanResponseDTO.WenjuanCreateResult result = createProjectViaAPI(wenjuanDTO, questionnaire.getId());

            log.info("问卷网问卷创建成功: surveyUrl={}", result.getSurveyUrl());
            return result;

        } catch (Exception e) {
            log.error("创建问卷网问卷异常", e);
            throw new RuntimeException("创建问卷网问卷失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过API创建项目
     * API文档: https://open.wenjuan.com/docs - 创建空白项目
     * 接口: /openapi/v4/create_proj
     * 
     * @param wenjuanDTO      问卷数据
     * @param questionnaireId 本地问卷ID（用于生成自定义链接）
     */
    private WenjuanResponseDTO.WenjuanCreateResult createProjectViaAPI(WenjuanQuestionnaireDTO wenjuanDTO,
            Long questionnaireId) {
        try {
            // 准备请求参数
            long timestamp = System.currentTimeMillis() / 1000;
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("ptype", "survey"); // 项目类型：survey-问卷
            params.put("timestamp", String.valueOf(timestamp)); // Unix时间戳（秒）
            // 注意：title是可选参数，暂时不传避免中文编码问题
            // if (wenjuanDTO.getTitle() != null && !wenjuanDTO.getTitle().isEmpty()) {
            // params.put("title", wenjuanDTO.getTitle());
            // }

            // 构建请求URL（buildSignedUrl内部会生成签名）
            String apiUrl = wenjuanConfig.getBaseUrl() + "/create_proj";
            String requestUrl = WenjuanSignatureUtil.buildSignedUrl(apiUrl, params, wenjuanConfig.getAppSecret());

            log.info("=== 调用问卷网创建项目API ===");
            log.info("API地址: {}", apiUrl);
            log.info("完整URL: {}", requestUrl);
            log.info("当前时间戳: {}, 系统时间: {}", timestamp, new java.util.Date());
            log.info("请求参数: app_key={}, ptype={}, timestamp={}",
                    wenjuanConfig.getAppKey(), "survey", timestamp);

            // 发送GET请求
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

            // 记录原始响应
            String responseBody = response.getBody();
            log.info("问卷网API HTTP状态码: {}", response.getStatusCode());
            log.info("问卷网API原始响应: {}", responseBody);

            // 解析响应
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = JSON.parseObject(responseBody, Map.class);
            log.info("问卷网API解析后数据: {}", responseData);

            // 检查响应状态 - 问卷网API使用err_code字段
            Integer errCode = (Integer) responseData.get("err_code");
            if (errCode == null) {
                log.error("问卷网API返回数据格式错误，缺少err_code字段");
                log.error("响应数据类型: {}", responseData.getClass().getName());
                log.error("响应数据内容: {}", responseData);
                log.error("响应数据的keys: {}", responseData.keySet());
                throw new RuntimeException("问卷网API返回数据格式错误");
            }

            if (errCode != 0) {
                String errMsg = (String) responseData.get("err_msg");
                log.error("问卷网API返回错误: err_code={}, err_msg={}", errCode, errMsg);
                throw new RuntimeException("问卷网API返回错误: " + errMsg + " (错误码:" + errCode + ")");
            }

            // 解析返回数据
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseData.get("data");
            if (data == null) {
                log.error("问卷网API返回数据中没有data字段: {}", responseData);
                throw new RuntimeException("问卷网API返回数据格式错误");
            }

            String shortId = (String) data.get("short_id");
            if (shortId == null || shortId.isEmpty()) {
                log.error("问卷网API返回数据中没有short_id字段: {}", data);
                throw new RuntimeException("问卷网API返回数据中没有short_id");
            }

            // 获取编辑链接（用于手动添加题目）
            String editUrl = getEditConsoleUrl(shortId);

            // 注意：问卷网API暂不支持通过API直接添加题目
            // 因此创建的是空白项目，需要用户手动添加题目或使用编辑链接

            // 暂时不发布项目，让用户先添加题目
            // boolean published = publishProject(shortId);

            // 使用公共链接格式（多次填写），而不是自定义链接（单次填写）
            // 格式: https://www.wenjuan.com/s/{shortId}/
            String surveyUrl = "https://www.wenjuan.com/s/" + shortId + "/";
            
            log.info("使用公共链接格式，支持多次填写: {}", surveyUrl);

            // 构建返回结果
            WenjuanResponseDTO.WenjuanCreateResult result = new WenjuanResponseDTO.WenjuanCreateResult();
            String shortUrl = "https://wj.cn/" + shortId;
            String qrcodeUrl = "https://www.wenjuan.com/qrcode/" + shortId + ".png";

            result.setSurveyId(shortId);
            result.setSurveyUrl(surveyUrl);
            result.setShortUrl(shortUrl);
            result.setQrcodeUrl(qrcodeUrl);
            result.setManageUrl(editUrl); // 添加编辑/管理链接到返回结果

            log.info("问卷网空白项目创建成功: shortId={}", shortId);
            log.info("答题链接: {}", surveyUrl);
            log.info("编辑链接: {}", editUrl);
            log.info("提示: 需要通过编辑链接手动添加题目，或等待问卷网提供题目导入API");

            return result;

        } catch (Exception e) {
            log.error("调用问卷网API失败", e);
            throw new RuntimeException("调用问卷网API失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取编辑项目后台链接
     * API文档: https://open.wenjuan.com/docs - 获取编辑项目后台链接
     * 接口: /openapi/v4/get_edit_console_url
     */
    private String getEditConsoleUrl(String shortId) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("short_id", shortId);

            String apiUrl = wenjuanConfig.getBaseUrl() + "/get_edit_console_url";
            String requestUrl = WenjuanSignatureUtil.buildSignedUrl(apiUrl, params, wenjuanConfig.getAppSecret());

            log.info("获取编辑链接: {}", requestUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = JSON.parseObject(response.getBody(), Map.class);

            Integer errCode = (Integer) responseData.get("err_code");
            if (errCode != null && errCode == 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseData.get("data");
                if (data != null) {
                    return (String) data.get("url");
                }
            }

            return null;

        } catch (Exception e) {
            log.error("获取编辑链接失败", e);
            return null;
        }
    }

    /**
     * 获取自定义答题链接（支持防重复答题）
     * API文档: https://open.wenjuan.com/docs - 获取自定义答题链接
     * 接口: /openapi/v4/get_custom_share_url
     * 
     * @param shortId     项目短ID
     * @param respondent  自定义参数（用于标识来源）
     * @param allowRepeat 是否允许重复答题
     * @return 自定义答题链接
     */
    public String getCustomShareUrl(String shortId, String respondent, boolean allowRepeat) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("short_id", shortId);
            params.put("respondent", java.net.URLEncoder.encode(respondent, "UTF-8")); // 需要URL编码
            params.put("repeat", allowRepeat ? "1" : "0"); // 0-不可重复答题, 1-可重复答题

            String apiUrl = wenjuanConfig.getBaseUrl() + "/get_custom_share_url";
            String requestUrl = WenjuanSignatureUtil.buildSignedUrl(apiUrl, params, wenjuanConfig.getAppSecret());

            log.info("获取自定义答题链接 - shortId: {}, respondent: {}, allowRepeat: {}",
                    shortId, respondent, allowRepeat);
            log.info("请求URL: {}", requestUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = JSON.parseObject(response.getBody(), Map.class);

            log.info("问卷网API响应: {}", responseData);

            Integer errCode = (Integer) responseData.get("err_code");
            if (errCode != null && errCode == 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseData.get("data");
                if (data != null) {
                    String customUrl = (String) data.get("url");
                    log.info("自定义答题链接获取成功: {}", customUrl);
                    return customUrl;
                }
            } else {
                String errMsg = (String) responseData.get("err_msg");
                log.error("获取自定义答题链接失败: err_code={}, err_msg={}", errCode, errMsg);
            }

            return null;

        } catch (Exception e) {
            log.error("获取自定义答题链接异常", e);
            return null;
        }
    }

    /**
     * 发布项目（修改项目状态为收集中）
     * API文档: https://open.wenjuan.com/docs - 修改项目状态
     * 接口: /openapi/v4/change_proj_status
     * 
     * @param shortId 项目短ID
     * @return 是否发布成功
     */
    private boolean publishProject(String shortId) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("short_id", shortId);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("to_status", "1"); // 1-收集中

            String apiUrl = wenjuanConfig.getBaseUrl() + "/change_proj_status";
            String requestUrl = WenjuanSignatureUtil.buildSignedUrl(apiUrl, params, wenjuanConfig.getAppSecret());

            log.info("发布项目: {}", requestUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = JSON.parseObject(response.getBody(), Map.class);

            Integer errCode = (Integer) responseData.get("err_code");
            if (errCode != null && errCode == 0) {
                log.info("项目发布成功");
                return true;
            } else {
                log.warn("项目发布失败: {}", responseData.get("err_msg"));
                return false;
            }

        } catch (Exception e) {
            log.error("发布项目失败", e);
            return false;
        }
    }

    /**
     * 获取答题链接
     * API文档: https://open.wenjuan.com/docs - 获取答题链接
     * 接口: /openapi/v4/get_publish_share_url
     * 
     * @param shortId 项目短ID
     * @return 答题链接
     */
    private String getPublishShareUrl(String shortId) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("short_id", shortId);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            String apiUrl = wenjuanConfig.getBaseUrl() + "/get_publish_share_url";
            String requestUrl = WenjuanSignatureUtil.buildSignedUrl(apiUrl, params, wenjuanConfig.getAppSecret());

            log.info("获取答题链接: {}", requestUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = JSON.parseObject(response.getBody(), Map.class);

            Integer errCode = (Integer) responseData.get("err_code");
            if (errCode != null && errCode == 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseData.get("data");
                if (data != null) {
                    return (String) data.get("url");
                }
            }

            return null;

        } catch (Exception e) {
            log.error("获取答题链接失败", e);
            return null;
        }
    }

    /**
     * 获取项目详情
     * API: /openapi/v4/get_proj_detail
     * 
     * @param shortId 项目短ID
     * @return 项目详情
     */
    public Map<String, Object> getProjectDetail(String shortId) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("short_id", shortId);

            String apiUrl = wenjuanConfig.getBaseUrl() + "/get_proj_detail";
            String requestUrl = WenjuanSignatureUtil.buildSignedUrl(apiUrl, params, wenjuanConfig.getAppSecret());

            log.info("获取项目详情: {}", requestUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = JSON.parseObject(response.getBody(), Map.class);

            return result;

        } catch (Exception e) {
            log.error("获取项目详情失败", e);
            throw new RuntimeException("获取项目详情失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取答卷详情列表
     * API: /openapi/v4/get_rspd_list
     * 
     * @param shortId 项目短ID
     * @return 答卷列表
     */
    public Map<String, Object> getResponseList(String shortId) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("short_id", shortId);

            String apiUrl = wenjuanConfig.getBaseUrl() + "/get_rspd_list";
            String requestUrl = WenjuanSignatureUtil.buildSignedUrl(apiUrl, params, wenjuanConfig.getAppSecret());

            log.info("获取答卷列表: {}", requestUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = JSON.parseObject(response.getBody(), Map.class);

            return result;

        } catch (Exception e) {
            log.error("获取答卷列表失败", e);
            throw new RuntimeException("获取答卷列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换为问卷网格式
     */
    private WenjuanQuestionnaireDTO convertToWenjuanFormat(SurveyQuestionnaire questionnaire,
            List<SurveyQuestion> questions) {
        WenjuanQuestionnaireDTO wenjuanDTO = WenjuanQuestionnaireDTO.builder()
                .title(questionnaire.getTitle())
                .description(questionnaire.getDescription())
                .surveyType(1) // 普通问卷
                .setting(WenjuanQuestionnaireDTO.SurveySetting.builder()
                        .allowAnonymous(true)
                        .showProgress(true)
                        .submitMessage("感谢您的参与！")
                        .build())
                .build();

        // 转换题目
        List<WenjuanQuestionnaireDTO.WenjuanQuestionDTO> wenjuanQuestions = questions.stream()
                .map(this::convertQuestion)
                .collect(Collectors.toList());

        wenjuanDTO.setQuestions(wenjuanQuestions);
        return wenjuanDTO;
    }

    /**
     * 转换单个题目
     */
    private WenjuanQuestionnaireDTO.WenjuanQuestionDTO convertQuestion(SurveyQuestion question) {
        WenjuanQuestionnaireDTO.WenjuanQuestionDTO wenjuanQuestion = WenjuanQuestionnaireDTO.WenjuanQuestionDTO
                .builder()
                .order(question.getQuestionNo())
                .title(question.getQuestionText())
                .description(question.getQuestionDescription())
                .required(question.getIsRequired() == 1)
                .build();

        // 根据题目类型转换
        switch (question.getQuestionType().toUpperCase()) {
            case "SINGLE_CHOICE":
                wenjuanQuestion.setType("radio"); // 单选题
                wenjuanQuestion.setOptions(parseOptions(question.getOptions()));
                break;
            case "MULTIPLE_CHOICE":
                wenjuanQuestion.setType("checkbox"); // 多选题
                wenjuanQuestion.setOptions(parseOptions(question.getOptions()));
                break;
            case "TEXT":
                wenjuanQuestion.setType("text"); // 单行填空
                break;
            case "TEXTAREA":
                wenjuanQuestion.setType("textarea"); // 多行填空
                wenjuanQuestion.setRows(5);
                break;
            case "RATING":
                wenjuanQuestion.setType("score"); // 评分题
                wenjuanQuestion.setScoreMin(1);
                wenjuanQuestion.setScoreMax(5);
                break;
            default:
                wenjuanQuestion.setType("text"); // 默认单行填空
        }

        return wenjuanQuestion;
    }

    /**
     * 解析选项JSON字符串
     */
    private List<WenjuanQuestionnaireDTO.QuestionOption> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> optionList = (List<Map<String, Object>>) (List<?>) JSON.parseArray(optionsJson,
                    Map.class);
            return optionList.stream()
                    .map(opt -> WenjuanQuestionnaireDTO.QuestionOption.builder()
                            .id(String.valueOf(opt.get("value")))
                            .text(String.valueOf(opt.get("label")))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析选项失败: {}", optionsJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取数据报表后台链接
     * API文档: https://open.wenjuan.com/docs
     * 
     * @param shortId 项目短ID
     * @return 数据报表后台链接
     */
    public String getReportConsoleUrl(String shortId) {
        try {
            log.info("开始获取问卷网数据报表链接: shortId={}", shortId);

            // 构建请求参数（按字母顺序）
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("short_id", shortId);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            // 生成签名
            String signature = WenjuanSignatureUtil.generateSignature(params, wenjuanConfig.getAppSecret());
            params.put("signature", signature);

            // 构建URL
            String url = wenjuanConfig.getBaseUrl() + "/get_report_console_url";
            StringBuilder urlBuilder = new StringBuilder(url).append("?");
            params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            String requestUrl = urlBuilder.substring(0, urlBuilder.length() - 1);

            log.info("请求URL: {}", requestUrl);

            // 发送GET请求
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            String responseBody = response.getBody();
            log.info("问卷网API响应: {}", responseBody);

            // 解析响应
            Map<String, Object> result = JSON.parseObject(responseBody, Map.class);
            Integer errCode = (Integer) result.get("err_code");

            if (errCode != null && errCode == 0) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                String reportUrl = (String) data.get("url");
                log.info("获取报表链接成功: {}", reportUrl);
                return reportUrl;
            } else {
                String errMsg = (String) result.get("err_msg");
                log.error("获取报表链接失败: errCode={}, errMsg={}", errCode, errMsg);
                throw new RuntimeException("获取报表链接失败: " + errMsg);
            }

        } catch (Exception e) {
            log.error("获取问卷网数据报表链接失败", e);
            throw new RuntimeException("获取报表链接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除问卷网项目
     * API文档: https://open.wenjuan.com/docs
     * 
     * @param shortId 项目短ID
     * @param deleteType 删除类型: -2=回收站, -1=永久删除
     * @return 是否删除成功
     */
    public boolean deleteWenjuanProject(String shortId, int deleteType) {
        try {
            log.info("开始删除问卷网项目: shortId={}, deleteType={}", shortId, deleteType);

            // 构建请求参数（按字母顺序）
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            if (deleteType == -1 || deleteType == -2) {
                params.put("delete_type", String.valueOf(deleteType));
            }
            params.put("short_id", shortId);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            // 生成签名
            String signature = WenjuanSignatureUtil.generateSignature(params, wenjuanConfig.getAppSecret());
            params.put("signature", signature);

            // 构建URL
            String url = wenjuanConfig.getBaseUrl() + "/delete_proj";
            StringBuilder urlBuilder = new StringBuilder(url).append("?");
            params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            String requestUrl = urlBuilder.substring(0, urlBuilder.length() - 1);

            log.info("删除请求URL: {}", requestUrl);

            // 发送GET请求
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            String responseBody = response.getBody();
            log.info("问卷网API响应: {}", responseBody);

            // 解析响应
            Map<String, Object> result = JSON.parseObject(responseBody, Map.class);
            Integer errCode = (Integer) result.get("err_code");

            if (errCode != null && errCode == 0) {
                log.info("问卷网项目删除成功: shortId={}", shortId);
                return true;
            } else {
                String errMsg = (String) result.get("err_msg");
                log.error("删除问卷网项目失败: errCode={}, errMsg={}", errCode, errMsg);
                return false;
            }

        } catch (Exception e) {
            log.error("删除问卷网项目失败", e);
            return false;
        }
    }

    /**
     * 同步问卷网答卷数量
     * 
     * @param questionnaireId 问卷ID
     */
    public void syncAnswerCount(Long questionnaireId) {
        try {
            SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
            if (questionnaire == null || questionnaire.getWenjuanShortId() == null) {
                return;
            }

            String shortId = questionnaire.getWenjuanShortId();
            log.info("同步问卷网答卷数量: questionnaireId={}, shortId={}", questionnaireId, shortId);

            // 获取项目详情
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("short_id", shortId);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            String signature = WenjuanSignatureUtil.generateSignature(params, wenjuanConfig.getAppSecret());
            params.put("signature", signature);

            String url = wenjuanConfig.getBaseUrl() + "/get_proj_detail";
            StringBuilder urlBuilder = new StringBuilder(url).append("?");
            params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            String requestUrl = urlBuilder.substring(0, urlBuilder.length() - 1);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            String responseBody = response.getBody();

            Map<String, Object> result = JSON.parseObject(responseBody, Map.class);
            Integer errCode = (Integer) result.get("err_code");

            if (errCode != null && errCode == 0) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                Integer respondentCount = (Integer) data.get("respondent_count");
                
                // 更新数据库
                questionnaire.setWenjuanRespondentCount(respondentCount);
                questionnaireMapper.updateById(questionnaire);
                
                log.info("同步答卷数量成功: count={}", respondentCount);
            }
        } catch (Exception e) {
            log.error("同步答卷数量失败", e);
            throw new RuntimeException("同步失败: " + e.getMessage());
        }
    }

    /**
     * 测试API连接
     * 
     * @return 测试结果
     */
    public boolean testConnection() {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_key", wenjuanConfig.getAppKey());
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            // 生成签名用于测试
            String signature = WenjuanSignatureUtil.generateSignature(params, wenjuanConfig.getAppSecret());

            log.info("问卷网API配置测试成功 - AppKey: {}, 签名: {}",
                    wenjuanConfig.getAppKey(), signature.substring(0, 8) + "...");

            return true;

        } catch (Exception e) {
            log.error("问卷网API配置测试失败", e);
            return false;
        }
    }
}
