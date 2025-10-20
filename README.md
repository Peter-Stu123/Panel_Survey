# Panel Survey - 医疗问卷调查系统

## 📋 项目概述

Panel Survey 是一个专业的医疗问卷调查系统，专门为医疗研究人员和机构设计。系统集成了AI智能问卷生成、问卷网第三方平台、数据分析和报告生成等功能，为医疗偏好调查提供完整的解决方案。
<img width="2822" height="1255" alt="a331b38c232d4fb2cc992999954c02b" src="https://github.com/user-attachments/assets/f70c7a47-8f89-41cb-96e5-7379efa0ba7e" />

<img width="2848" height="1273" alt="image" src="https://github.com/user-attachments/assets/434a9bf0-9d79-40c0-a7f3-68e7e375ee78" />

### 🎯 核心功能
- **智能问卷生成**：基于OpenAI GPT模型，根据医疗研究需求自动生成专业问卷
- **六步式工作流**：从项目创建到报告生成的完整流程管理
- **第三方集成**：与问卷网(wenjuan.com)深度集成，支持问卷发布和数据同步
- **数据分析**：自动统计分析和可视化图表生成
- **多格式导出**：支持PDF、Excel等多种格式的报告导出
- **用户权限管理**：支持管理员和普通用户的角色管理

## 🛠 技术栈

### 后端技术栈
- **框架**: Spring Boot 3.4.10
- **Java版本**: JDK 17
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **ORM**: MyBatis 3.0.5
- **认证**: JWT (JSON Web Token)
- **API文档**: Knife4j + SpringDoc OpenAPI 3
- **AI集成**: Spring AI + OpenAI GPT
- **文档处理**: Apache POI (Excel), iText7 (PDF)
- **构建工具**: Maven

### 前端技术栈
- **框架**: Vue 3.5.22
- **构建工具**: Vite 7.1.7
- **UI组件库**: Element Plus 2.11.4
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.2.5
- **HTTP客户端**: Axios 1.6.5
- **图表库**: ECharts 6.0.0
- **文件处理**: XLSX 0.18.5, jsPDF 3.0.3
- **其他**: QRCode 1.5.4, html2canvas 1.4.1

### 开发工具
- **代码规范**: ESLint
- **包管理**: npm
- **版本控制**: Git
- **API测试**: Knife4j Web UI

## 🏗 系统架构

### 整体架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端 Vue.js   │────│  后端 Spring    │────│   MySQL数据库   │
│   Element Plus  │    │      Boot       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                    ┌─────────┼─────────┐
                    │         │         │
            ┌───────▼───┐ ┌───▼───┐ ┌───▼────┐
            │   Redis   │ │OpenAI │ │问卷网API│
            │   缓存    │ │  API  │ │        │
            └───────────┘ └───────┘ └────────┘
```

### 后端分层架构
```
Controller Layer (控制层)
    ├── AuthController (认证管理)
    ├── UserController (用户管理)
    ├── ProjectController (项目管理)
    ├── QuestionnaireController (问卷管理)
    ├── ResponseController (问卷填写)
    ├── ReportController (报告管理)
    └── DraftController (草稿管理)

Service Layer (业务层)
    ├── UserService (用户服务)
    ├── SurveyProjectService (项目服务)
    ├── QuestionnaireService (问卷服务)
    ├── ResponseService (回复服务)
    ├── ReportService (报告服务)
    ├── DraftService (草稿服务)
    └── WenjuanIntegrationService (问卷网集成)

Mapper Layer (数据访问层)
    ├── UserMapper
    ├── SurveyProjectMapper
    ├── SurveyQuestionnaireMapper
    ├── SurveyQuestionMapper
    ├── SurveyResponseMapper
    ├── SurveyAnswerMapper
    └── SurveyReportMapper
```

## 📊 数据库设计

### 核心数据表
1. **user** - 用户表
2. **survey_project** - 问卷项目表
3. **survey_questionnaire** - 问卷表
4. **survey_question** - 问卷题目表
5. **survey_response** - 问卷回复表
6. **survey_answer** - 问卷答案明细表
7. **survey_report** - 分析报告表
8. **system_log** - 系统日志表

### 数据关系
```
User (1:N) SurveyProject (1:1) SurveyQuestionnaire (1:N) SurveyQuestion
                                        │
                                       (1:N)
                                        │
                              SurveyResponse (1:N) SurveyAnswer
                                        │
                                       (1:1)
                                        │
                                  SurveyReport
```

## 🔌 API接口文档

### 认证管理 (/api/auth)
- `POST /login` - 用户登录
- `POST /register` - 用户注册

### 用户管理 (/api/user)
- `GET /info` - 获取用户信息
- `GET /list` - 获取所有用户
- `GET /{id}` - 获取用户详情
- `PUT /{id}/status` - 更新用户状态
- `DELETE /{id}` - 删除用户

### 项目管理 (/api/project)
- `POST /step1` - 创建项目第一步
- `GET /list` - 获取用户所有项目
- `GET /{id}` - 获取项目详情
- `DELETE /{id}` - 删除项目

### 问卷管理 (/api/questionnaire)
- `POST /generate/{projectId}` - 生成问卷
- `GET /{id}` - 获取问卷详情
- `GET /project/{projectId}` - 根据项目ID获取问卷
- `PUT /question/{questionId}` - 更新问卷题目
- `POST /review/{id}` - AI审核问卷

### 问卷填写 (/api/survey)
- `GET /public/{questionnaireId}` - 获取公开问卷
- `POST /public/submit` - 提交问卷答案
- `GET /public/count/{questionnaireId}` - 获取问卷回答数量
- `GET /public/completed/{questionnaireId}` - 获取问卷完成数量

### 报告管理 (/api/report)
- `POST /generate/{questionnaireId}` - 生成调查报告
- `GET /{id}` - 获取报告详情
- `GET /questionnaire/{questionnaireId}` - 根据问卷ID获取报告
- `GET /export/excel/{reportId}` - 导出Excel报告
- `GET /export/pdf/{reportId}` - 导出PDF报告
- `POST /generate/excel/{questionnaireId}` - 从Excel生成报告

### 草稿管理 (/api/draft)
- `POST /save` - 保存草稿
- `GET /get` - 获取草稿
- `DELETE /delete` - 删除草稿
- `DELETE /clear` - 清空所有草稿

## 🔧 核心技术详解

### 1. JWT认证机制
- **JwtInterceptor**: 拦截所有API请求，验证JWT token
- **JwtUtil**: 提供token生成、解析、验证功能
- **配置**: 支持自定义token过期时间、密钥等

### 2. AI智能问卷生成
- **Spring AI集成**: 使用OpenAI GPT模型
- **Prompt工程**: 针对医疗问卷场景优化的提示词
- **结构化输出**: 生成标准化的问卷JSON格式

### 3. 问卷网第三方集成
- **WenjuanIntegrationService**: 封装问卷网API调用
- **WenjuanSignatureUtil**: 处理API签名认证
- **数据同步**: 支持问卷发布和答卷数据同步

### 4. Redis缓存策略
- **草稿保存**: 用户填写过程中的临时数据
- **Session管理**: 用户登录状态缓存
- **API限流**: 防止恶意请求

### 5. 文件处理能力
- **Excel导入导出**: Apache POI处理Excel文件
- **PDF生成**: iText7生成专业报告
- **二维码生成**: 问卷分享二维码

### 6. 数据可视化
- **ECharts集成**: 前端图表展示
- **统计分析**: 后端数据统计计算
- **多维度分析**: 支持多种统计维度

### 7. 六步式工作流
1. **Step1**: 项目基本信息创建
2. **Step2**: 详细参数配置
3. **Step3**: AI问卷生成
4. **Step4**: 问卷审核和发布
5. **Step5**: 数据收集监控
6. **Step6**: 报告生成和导出

## 🚀 部署说明

### 环境要求
- **Java**: JDK 17+
- **Node.js**: 20.19.0+ 或 22.12.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 后端部署
1. 配置数据库连接 (`application.properties`)
2. 配置Redis连接
3. 配置OpenAI API密钥
4. 配置问卷网API密钥
5. 运行 `mvn spring-boot:run`

### 前端部署
1. 安装依赖: `npm install`
2. 开发模式: `npm run dev`
3. 生产构建: `npm run build`

### 数据库初始化
1. 创建数据库: `panel_survey`
2. 执行 `schema.sql` 初始化表结构
3. 默认管理员账号: admin/admin123

## 📝 使用流程

### 研究人员使用流程
1. **注册登录**: 创建账号并登录系统
2. **创建项目**: 填写研究项目基本信息
3. **配置参数**: 设置问卷详细参数
4. **生成问卷**: AI自动生成专业问卷
5. **审核发布**: 审核问卷内容并发布到问卷网
6. **数据收集**: 分享问卷链接收集数据
7. **分析报告**: 生成统计分析报告

### 受试者填写流程
1. **访问链接**: 通过分享链接或二维码访问
2. **填写问卷**: 按照指导填写问卷内容
3. **提交答案**: 完成后提交问卷答案

## 🔒 安全特性

- **JWT认证**: 无状态token认证机制
- **密码加密**: BCrypt加密存储用户密码
- **CORS配置**: 跨域请求安全控制
- **SQL注入防护**: MyBatis参数化查询
- **XSS防护**: 前端输入验证和转义
- **API限流**: Redis实现请求频率限制

## 📈 性能优化

- **数据库索引**: 关键字段建立索引
- **Redis缓存**: 热点数据缓存
- **连接池**: 数据库连接池优化
- **异步处理**: 耗时操作异步执行
- **CDN加速**: 静态资源CDN分发

## 🐛 错误处理

- **全局异常处理**: GlobalExceptionHandler统一处理
- **业务异常**: 自定义业务异常类型
- **日志记录**: 详细的操作日志记录
- **用户友好**: 前端友好的错误提示

## 📚 开发文档

### API文档访问
- **Knife4j**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### 项目结构
```
Panel_Survey/
├── Backend/Penal_Survey/          # 后端项目
│   ├── src/main/java/             # Java源码
│   ├── src/main/resources/        # 配置文件
│   └── pom.xml                    # Maven配置
└── Front/Panel_Survey/            # 前端项目
    ├── src/                       # Vue源码
    ├── public/                    # 静态资源
    └── package.json               # npm配置
```

