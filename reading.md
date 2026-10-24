# 团队读书打卡小程序 · 完整开发文档（无支付版）

**文档版本**：v1.1（移除支付与商业化模块）  
**技术栈**：Spring Boot 3.5 + UniApp (Vue 3) + MySQL 8 + Redis  
**UI 风格**：液态玻璃（Liquid Glass）+ 悬浮底部导航栏 + 暗/亮主题切换

---

## 一、项目概述

### 1.1 产品定位
一款面向企业/团队内部的**纯内部管理型**读书打卡小程序。管理员统一记录打卡数据，队员上传读书打卡图片、朗读音频或心得笔记，支持开放书库选书、排行榜激励、可视化数据看板。核心目标是降低团队读书管理成本，提升成员阅读坚持率。**不涉及任何支付、订单、VIP 及发票相关商业化功能。**

### 1.2 角色定义
| 角色 | 权限说明 |
|------|----------|
| 超级管理员 | 全部功能，含账号注销、初始数据导入、系统设置 |
| 团队管理员 | 布置/调整作业、导出打卡数据、设置打卡规则与模板、查看统计看板 |
| 普通队员 | 上传打卡（图片/音频/心得）、选书阅读、查看排行榜、个人成长 |

### 1.3 核心流程
```
队员登录 → 选择书籍（书库/自选）→ 上传打卡记录（朗读音频/心得笔记/图片）
    → 管理员审核/记录 → 打卡数据入库 → 排行榜/看板实时更新
```

---

## 二、功能清单（已移除支付相关）

### 2.1 核心打卡功能
- 管理员统一记录打卡
- 队员上传打卡（图片/朗读音频/心得笔记/学习录像）
- 开放书库选书、加入书架
- 排行榜（周/月/年，个人/小组/圈子）
- 可视化数据展示
- 职位分级，不同职位周打卡合格天数不同
- 查询队员周/月/年打卡天数

### 2.2 圈子/小组体系
- 部门/小组/学习圈三级结构
- 创建小组、邀请加入、退出小组
- 小组维度排行榜与活跃度对比
- 圈主/组长管理本圈成员与任务

### 2.3 个人中心体系
- 个人信息、我的收藏、我的推广
- 帮助与反馈、消息通知、个人成长
- 服务热线、我的地址
- 用户协议、关于小程序、设置

### 2.4 管理员后台面板
- 布置/调整学习作业
- 导出打卡数据（Excel）
- 读书 AI 考试 / 云培训 AI 考试
- 打卡规则设置（按职位配置合格天数）
- 初始数据导入
- 打卡模板设置
- 注销账号

### 2.5 主题与 UI 增强
- 暗/亮主题切换（跟随系统 + 手动切换）
- 液态玻璃卡片与悬浮导航栏

> ⚠️ **已移除模块**：我的订单（待付款/待评价/售后）、开通 VIP、我的发票抬头、书籍价格展示与购买入口。

---

## 三、完整项目架构

### 3.1 后端架构（Spring Boot 3.5）

```
team-reading-backend/
├── pom.xml
├── src/main/java/com/teamreading/
│   ├── TeamReadingApplication.java
│   ├── common/                          # 通用模块
│   │   ├── result/                      # 统一响应封装 (Result<T>, PageResult<T>)
│   │   ├── exception/                   # 全局异常处理 (@RestControllerAdvice)
│   │   ├── constant/                    # 常量（角色枚举、打卡类型）
│   │   └── utils/                       # 工具类（Excel导出、图片压缩、日期）
│   ├── config/                          # 配置
│   │   ├── SaTokenConfig.java           # Sa-Token 权限配置
│   │   ├── RedisConfig.java
│   │   ├── MybatisPlusConfig.java       # 分页插件、自动填充
│   │   └── CorsConfig.java
│   ├── controller/                      # 接口层
│   │   ├── AuthController.java          # 登录/注册
│   │   ├── UserController.java          # 用户/个人信息/地址
│   │   ├── CheckinController.java       # 打卡记录 CRUD
│   │   ├── BookController.java          # 书库/书籍详情/收藏/书架
│   │   ├── CircleController.java        # 圈子/小组/成员管理
│   │   ├── RankingController.java       # 排行榜（周/月/年）
│   │   ├── DashboardController.java     # 统计看板（打卡率、态势、活跃度）
│   │   ├── AdminController.java         # 管理员作业/规则/导出/导入
│   │   └── NotifyController.java        # 消息通知
│   ├── service/
│   │   ├── impl/
│   │   └── ...                          # 业务接口
│   ├── mapper/                          # MyBatis-Plus Mapper
│   ├── entity/                          # 数据库实体
│   ├── dto/                             # 请求/响应 DTO
│   └── job/                             # 定时任务（每日打卡统计、周报生成）
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    └── mapper/                          # 复杂 SQL（统计查询）
```

**后端技术选型**：

| 技术 | 用途 | 说明 |
|------|------|------|
| Spring Boot 3.5 | 基础框架 | JDK 17+ |
| **Sa-Token** | 认证与权限 | 使用 `sa-token-spring-boot3-starter` |
| **MyBatis-Plus** | ORM | 提供 CRUD 与分页 |
| MySQL 8 | 主数据库 | 业务数据 |
| Redis | 缓存/排行榜/会话 | 排行榜用 ZSet |
| EasyExcel | 打卡数据导出 | 管理员导出 Excel |
| Hutool | 工具库 | 日期/加密/图片处理 |
| WebSocket（可选） | 实时打卡态势 | 看板数据推送 |

**权限模型**（Sa-Token）：
- 角色：`SUPER_ADMIN`、`TEAM_ADMIN`、`MEMBER`
- 权限码：`checkin:add`、`checkin:export`、`admin:rule`、`admin:job` 等
- 注解：`@SaCheckRole("TEAM_ADMIN")`、`@SaCheckPermission("checkin:export")`

---

### 3.2 前端架构（UniApp Vue 3）

```
team-reading-uniapp/
├── pages.json                           # 页面路由 + 自定义 tabBar 配置
├── manifest.json
├── App.vue                              # 全局主题监听、启动逻辑
├── main.ts
├── uni_modules/                         # easycom 组件
├── src/
│   ├── api/                             # 接口请求封装
│   │   ├── request.ts                   # uni.request 拦截器封装
│   │   ├── auth.ts
│   │   ├── checkin.ts
│   │   └── ...
│   ├── components/                      # 公共组件
│   │   ├── liquid-glass/                # 液态玻璃容器
│   │   ├── float-tabbar/                # 悬浮底部导航栏
│   │   ├── checkin-card/                # 打卡卡片（诵读/心得）
│   │   ├── ranking-list/                # 排行榜
│   │   └── theme-switch/                # 主题切换按钮
│   ├── pages/
│   │   ├── index/                       # 首页（任务/动态/看板/统计/圈简介/书架）
│   │   ├── checkin/                     # 打卡页
│   │   ├── bookshelf/                   # 书架
│   │   ├── ranking/                     # 排行榜
│   │   ├── mine/                        # 我的（已移除订单/VIP/发票）
│   │   ├── circle/                      # 圈简介/小组成员
│   │   └── admin/                       # 管理员功能（作业/规则/导出/导入）
│   ├── store/                           # Pinia
│   │   ├── user.ts                      # 用户信息、角色
│   │   ├── theme.ts                     # 主题状态（isDark）
│   │   └── checkin.ts                   # 打卡草稿
│   ├── styles/
│   │   ├── theme.scss                   # CSS 变量（亮/暗）
│   │   └── glass.scss                   # 液态玻璃公共样式
│   └── utils/
│       ├── auth.ts                      # 登录态管理
│       └── format.ts
```

**前端技术选型**：

| 技术 | 用途 |
|------|------|
| Vue 3 + TypeScript | 基础框架 |
| Pinia | 状态管理 |
| uni-ui（官方） | 基础组件 |
| uView Plus | 补充组件 |
| fc-liquid-glass | 液态玻璃效果插件 |
| ECharts（uni-app 版） | 统计图表 |
| Vite | 构建工具 |

---

## 四、功能细节与页面设计（无支付相关）

### 4.1 首页（对应截图 3）
**顶部区域**：
- 用户头像 + 昵称 + 等级标签（自由版）
- 背景图（可更换）
- 统计数字：关注 / 粉丝 / 获赞 / 积分 / 打卡
- 快捷入口：我的收藏 | 联系客服（在线 06:00–23:00）

**~~我的订单~~**：**已移除**（原待付款、待评价、售后模块）

**功能列表**：~~开通 VIP~~（已移除） → 我的推广 → 帮助与反馈 → 个人信息 → 消息通知 → 个人成长 → 服务热线 → 我的地址 → ~~我的发票抬头~~（已移除） → 用户协议 → 关于小程序 → 设置

**底部**：悬浮导航栏（首页 / 通讯录 / 打卡 / 书城 / 我的）

### 4.2 书城（对应截图 4）
- 搜索框（书籍名称）
- 分类 Tab：经营之道 / 人生哲学 / 领导力 / 自我精进 / 社会…
- 书籍卡片：封面 + 书名 + 作者/出版社 + 电子书标签 + 打卡人数 + 加入书架按钮
- **已移除**：价格展示（`¥`）、购物车购买按钮 → 替换为**“加入书架”**或**“立即阅读”**

### 4.3 打卡动态流（对应截图 1、2）
- 顶部 Tab：任务 / 动态 / 看板 / 统计 / 圈简介 / 书架
- 动态 Tab 下二级 Tab：诵读 / 心得
- 打卡卡片：用户头像+昵称+日期+已打卡天数+浏览数 + 章节标题 + 音频播放器（诵读）或文字内容（心得）+ 点赞/收藏/评论/分享/编辑

### 4.4 看板（对应截图 1）
- 顶部统计：今日打卡 2/4694、总完成率 0%、活跃小组 0组
- **实时打卡态势**：听录音 2/4694 (0%)、诵读文章 2/4694、写心得 0/4694
- Top 5 小组活跃度对比（进度条/柱状图）

### 4.5 统计（对应截图 2）
- 恭喜横幅（最佳分享）
- 三块数据卡：我已打卡天数、我在小组排名、我在圈子排名
- **本周学习标兵**：前三名头像 + 皇冠（A先生/B先生/C先生）
- **打卡率图表**：切换本年/本月/本周，柱状图展示

### 4.6 圈简介/小组成员（对应截图 2）
- 顶部 Tab：圈子 / 小组 / **部门**
- 当前部门：环境试验科学事业部
- 邀请加入学习圈（按钮）+ 退出学习圈
- 共 4694 人

### 4.7 书架（对应截图 2）
- Tab：我的藏书 / 小组合集 / 精选集
- 空状态：“暂无书籍，当前分类还没有可阅读的书籍”

### 4.8 管理员功能（对应截图 1）
- 布置学习作业 / 调整学习作业
- 导出打卡数据（Excel）
- 读书 AI 考试 / 云培训 AI 考试
- 打卡规则（按职位设置每周合格天数）
- 初始数据导入
- 打卡模板设置
- 注销账号

---

## 五、数据库设计（核心表，已移除订单）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户 | id, nickname, avatar, role, position, circle_id, group_id, points, checkin_days |
| `circle` | 圈子/部门 | id, name, owner_id, member_count |
| `group` | 小组 | id, circle_id, name, member_count |
| `book` | 书籍 | id, title, author, publisher, cover, category, is_free, description |
| `book_shelf` | 书架关联 | user_id, book_id, type (我的藏书/小组合集/精选集) |
| `checkin_record` | 打卡记录 | id, user_id, book_id, chapter, type (诵读/心得/图片), content, media_url, audio_url, checkin_date, status |
| `checkin_rule` | 打卡规则 | id, position, required_days_per_week, required_days_per_month |
| `ranking_snapshot` | 排行榜快照 | id, type (week/month/year), scope (circle/group), user_id/group_id, rank, score |
| `notification` | 消息通知 | id, user_id, title, content, read_status |
| `job` | 学习作业 | id, circle_id, title, book_id, chapter, deadline |

> **已移除**：`order` 订单表、`invoice` 发票表、VIP 相关字段。

---

## 六、开发规范与 UI 细节

### 6.1 液态玻璃效果实现
- **优先使用插件**：`fc-liquid-glass`（全端适配，暗黑模式自动切换）
- **降级方案**：非 iOS 26 设备使用 `backdrop-filter: blur(20px)` + 半透明白/深色背景
- **适用组件**：悬浮导航栏、卡片、弹窗、按钮
- **注意**：大量重叠玻璃组件可能影响性能，列表项避免滥用

### 6.2 主题切换规范
```scss
// theme.scss
:root, .theme-light {
  --bg-primary: #ffffff;
  --text-primary: #333333;
  --glass-bg: rgba(255,255,255,0.65);
  --glass-border: rgba(255,255,255,0.4);
}
.theme-dark {
  --bg-primary: #121212;
  --text-primary: #e0e0e0;
  --glass-bg: rgba(30,30,30,0.65);
  --glass-border: rgba(255,255,255,0.1);
}
```
- 所有颜色使用 CSS 变量
- 图片资源准备两套或使用 CSS filter 适配

### 6.3 悬浮导航栏规范
- 高度：约 60px + 底部安全区
- 背景：液态玻璃效果 + 圆角
- 位置：`position: fixed; bottom: 20px; left: 16px; right: 16px; border-radius: 30px;`
- 中间打卡按钮：凸起圆形，使用渐变/玻璃高光
- 切换页面用 `uni.switchTab`（tabBar 页面）或 `uni.navigateTo`

### 6.4 接口规范
- 统一响应格式：`{ code: 200, message: "success", data: {...} }`
- 分页：`{ records: [], total: 0, current: 1, size: 10 }`
- 认证：Header 携带 `satoken`（Sa-Token 默认 token 名）
- 打卡上传：图片/音频走 OSS/本地存储，接口只传 URL

---

## 七、补充技术栈清单

| 类别 | 技术 | 用途 |
|------|------|------|
| 后端 | Sa-Token | 认证鉴权 |
| 后端 | MyBatis-Plus | ORM 与分页 |
| 后端 | EasyExcel | 导出打卡数据 |
| 后端 | Redis + ZSet | 排行榜实时计算 |
| 前端 | Pinia | 状态管理 |
| 前端 | uni-ui + uView Plus | UI 组件基础 |
| 前端 | fc-liquid-glass | 液态玻璃效果 |
| 前端 | ECharts（uni-app 版） | 统计图表 |
| 构建 | Vite | UniApp Vue 3 构建 |

---

## 八、开发里程碑建议

| 阶段 | 内容 | 周期 |
|------|------|------|
| P0 | 后端基础框架 + 登录认证 + 用户/圈子/小组表 | 1 周 |
| P1 | 书城/书架 + 打卡上传（图片/音频/心得） + 动态流 | 2 周 |
| P2 | 排行榜 + 统计看板 + 管理员后台（规则/作业/导出） | 2 周 |
| P3 | 液态玻璃 UI + 悬浮导航栏 + 暗黑模式 | 1 周 |
| P4 | 消息通知 + 个人成长 + 联调测试 + 上线 | 1 周 |

---

**变更说明**：本版本移除了所有支付相关模块（订单、VIP、发票、书籍价格与购买入口），数据库删除 `order`、`invoice` 表，个人中心与管理员功能列表同步精简，其余核心打卡、排行榜、统计看板、圈子小组与 UI 规范保持不变，确保小程序为纯内部管理工具属性。
# 团队读书打卡小程序 · 数据库 SQL 脚本

**数据库**：MySQL 8.0+  
**字符集**：utf8mb4 / utf8mb4_general_ci  
**存储引擎**：InnoDB  
**版本**：v1.1（已移除订单、VIP、发票等支付相关表）

---

## 一、建库语句

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `team_reading` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE `team_reading`;
```

---

## 二、核心业务表

### 2.1 用户表 `sys_user`

```sql
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `open_id` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID',
  `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信UnionID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号(后台账号)',
  `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(BCrypt加密,后台账号用)',
  `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: SUPER_ADMIN/TEAM_ADMIN/MEMBER',
  `position` VARCHAR(50) DEFAULT '普通员工' COMMENT '职位(用于打卡规则匹配)',
  `circle_id` BIGINT DEFAULT NULL COMMENT '所属圈子/部门ID',
  `group_id` BIGINT DEFAULT NULL COMMENT '所属小组ID',
  `background_img` VARCHAR(500) DEFAULT '' COMMENT '个人主页背景图',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
  `checkin_days` INT NOT NULL DEFAULT 0 COMMENT '累计打卡天数',
  `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '获赞数',
  `address` VARCHAR(255) DEFAULT '' COMMENT '我的地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

---

### 2.2 圈子/部门表 `circle`

```sql
DROP TABLE IF EXISTS `circle`;
CREATE TABLE `circle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '圈子ID',
  `name` VARCHAR(100) NOT NULL COMMENT '圈子/部门名称',
  `cover` VARCHAR(500) DEFAULT '' COMMENT '封面图',
  `description` VARCHAR(500) DEFAULT '' COMMENT '简介',
  `owner_id` BIGINT NOT NULL COMMENT '圈主(管理员)ID',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数',
  `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子/部门表';
```

---

### 2.3 小组表 `t_group`

> 表名用 `t_group`，避免与 MySQL 保留字 `GROUP` 冲突。

```sql
DROP TABLE IF EXISTS `t_group`;
CREATE TABLE `t_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '小组ID',
  `circle_id` BIGINT NOT NULL COMMENT '所属圈子ID',
  `name` VARCHAR(100) NOT NULL COMMENT '小组名称',
  `description` VARCHAR(500) DEFAULT '' COMMENT '小组简介',
  `leader_id` BIGINT DEFAULT NULL COMMENT '组长ID',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数',
  `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_leader_id` (`leader_id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小组表';
```

---

### 2.4 书籍表 `book`

```sql
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
  `title` VARCHAR(200) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) DEFAULT '' COMMENT '作者',
  `publisher` VARCHAR(100) DEFAULT '' COMMENT '出版社',
  `cover` VARCHAR(500) DEFAULT '' COMMENT '封面图URL',
  `category` VARCHAR(50) DEFAULT '' COMMENT '分类: 经营之道/人生哲学/领导力/自我精进等',
  `book_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型 1电子书 2实体书',
  `description` TEXT COMMENT '书籍简介',
  `recommend_reason` VARCHAR(500) DEFAULT '' COMMENT '推荐理由',
  `chapter_count` INT NOT NULL DEFAULT 0 COMMENT '章节数',
  `checkin_count` INT NOT NULL DEFAULT 0 COMMENT '打卡人数',
  `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否开放书库 0否 1是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0下架 1上架',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_is_public` (`is_public`),
  FULLTEXT KEY `ft_title_author` (`title`, `author`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍表';
```

---

### 2.5 书籍章节表 `book_chapter`

```sql
DROP TABLE IF EXISTS `book_chapter`;
CREATE TABLE `book_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `book_id` BIGINT NOT NULL COMMENT '书籍ID',
  `chapter_no` INT NOT NULL DEFAULT 1 COMMENT '章节序号',
  `title` VARCHAR(200) NOT NULL COMMENT '章节标题',
  `content` LONGTEXT COMMENT '章节正文内容',
  `audio_url` VARCHAR(500) DEFAULT '' COMMENT '章节音频URL',
  `duration` INT DEFAULT 0 COMMENT '音频时长(秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍章节表';
```

---

### 2.6 书架关联表 `book_shelf`

```sql
DROP TABLE IF EXISTS `book_shelf`;
CREATE TABLE `book_shelf` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '书籍ID',
  `shelf_type` TINYINT NOT NULL DEFAULT 1 COMMENT '书架类型 1我的藏书 2小组合集 3精选集',
  `source` TINYINT NOT NULL DEFAULT 1 COMMENT '来源 1开放书库 2管理员推荐',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '阅读进度(章节序号)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book_type` (`user_id`, `book_id`, `shelf_type`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书架关联表';
```

---

### 2.7 打卡记录表 `checkin_record`

```sql
DROP TABLE IF EXISTS `checkin_record`;
CREATE TABLE `checkin_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '打卡记录ID',
  `user_id` BIGINT NOT NULL COMMENT '打卡用户ID',
  `circle_id` BIGINT NOT NULL COMMENT '所属圈子ID(冗余)',
  `group_id` BIGINT DEFAULT NULL COMMENT '所属小组ID(冗余)',
  `book_id` BIGINT NOT NULL COMMENT '书籍ID',
  `chapter_id` BIGINT DEFAULT NULL COMMENT '章节ID',
  `chapter_title` VARCHAR(200) DEFAULT '' COMMENT '章节标题(冗余)',
  `checkin_type` TINYINT NOT NULL DEFAULT 1 COMMENT '打卡类型 1图片 2朗读音频 3心得笔记 4学习录像',
  `content` TEXT COMMENT '打卡内容(心得文字)',
  `media_url` VARCHAR(500) DEFAULT '' COMMENT '图片/视频URL(多图逗号分隔)',
  `audio_url` VARCHAR(500) DEFAULT '' COMMENT '音频URL',
  `audio_duration` INT DEFAULT 0 COMMENT '音频时长(秒)',
  `checkin_date` DATE NOT NULL COMMENT '打卡日期(用于统计去重)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0待审核 1已通过 2已驳回',
  `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核人ID(管理员代打卡记录)',
  `audit_remark` VARCHAR(255) DEFAULT '' COMMENT '审核备注',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开到动态 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `checkin_date`),
  KEY `idx_circle_date` (`circle_id`, `checkin_date`),
  KEY `idx_group_date` (`group_id`, `checkin_date`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_checkin_type` (`checkin_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';
```

---

### 2.8 打卡规则表 `checkin_rule`

```sql
DROP TABLE IF EXISTS `checkin_rule`;
CREATE TABLE `checkin_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `circle_id` BIGINT NOT NULL COMMENT '所属圈子ID',
  `position` VARCHAR(50) NOT NULL COMMENT '职位名称',
  `required_days_per_week` INT NOT NULL DEFAULT 5 COMMENT '每周合格打卡天数',
  `required_days_per_month` INT NOT NULL DEFAULT 20 COMMENT '每月合格打卡天数',
  `required_days_per_year` INT NOT NULL DEFAULT 240 COMMENT '每年合格打卡天数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_circle_position` (`circle_id`, `position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡规则表(按职位配置)';
```

---

### 2.9 排行榜快照表 `ranking_snapshot`

> 用于缓存周/月/年排行榜结果，避免每次实时聚合。可由定时任务每日凌晨刷新，或读取 Redis ZSet。

```sql
DROP TABLE IF EXISTS `ranking_snapshot`;
CREATE TABLE `ranking_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rank_type` TINYINT NOT NULL COMMENT '排行类型 1周 2月 3年',
  `scope_type` TINYINT NOT NULL COMMENT '范围类型 1个人 2小组 3圈子',
  `scope_id` BIGINT NOT NULL COMMENT '范围ID(个人用户ID/小组ID/圈子ID)',
  `circle_id` BIGINT DEFAULT NULL COMMENT '所属圈子ID(便于筛选)',
  `group_id` BIGINT DEFAULT NULL COMMENT '所属小组ID(便于筛选)',
  `period_key` VARCHAR(20) NOT NULL COMMENT '周期标识 如 2026-W42 / 2026-10 / 2026',
  `score` INT NOT NULL DEFAULT 0 COMMENT '得分(打卡天数等)',
  `rank_no` INT NOT NULL DEFAULT 0 COMMENT '排名',
  `stat_date` DATE NOT NULL COMMENT '统计基准日',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_period_scope` (`rank_type`, `period_key`, `scope_type`),
  KEY `idx_circle_id` (`circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排行榜快照表';
```

---

### 2.10 学习作业表 `study_job`

```sql
DROP TABLE IF EXISTS `study_job`;
CREATE TABLE `study_job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `circle_id` BIGINT NOT NULL COMMENT '圈子ID',
  `group_id` BIGINT DEFAULT NULL COMMENT '指定小组ID(为空则全圈)',
  `title` VARCHAR(200) NOT NULL COMMENT '作业标题',
  `book_id` BIGINT NOT NULL COMMENT '指定书籍ID',
  `chapter_start` INT DEFAULT NULL COMMENT '起始章节序号',
  `chapter_end` INT DEFAULT NULL COMMENT '结束章节序号',
  `content` TEXT COMMENT '作业要求说明',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `deadline` DATE NOT NULL COMMENT '截止日期',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0草稿 1已发布 2已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习作业表';
```

---

### 2.11 消息通知表 `notification`

```sql
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` VARCHAR(500) NOT NULL COMMENT '内容',
  `notify_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型 1系统 2打卡提醒 3作业通知 4审核结果',
  `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `read_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';
```

---

### 2.12 用户互动表（点赞/收藏/评论）

```sql
-- 点赞表
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型 1打卡记录 2评论',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 收藏表（用于书籍、打卡记录收藏）
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `target_type` TINYINT NOT NULL COMMENT '目标类型 1书籍 2打卡记录',
  `target_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 评论表
DROP TABLE IF EXISTS `checkin_comment`;
CREATE TABLE `checkin_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `checkin_id` BIGINT NOT NULL COMMENT '打卡记录ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID(回复)',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0隐藏 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_checkin_id` (`checkin_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡评论表';
```

---

### 2.13 用户关注表 `user_follow`

```sql
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
  `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';
```

---

### 2.14 打卡模板表 `checkin_template`

```sql
DROP TABLE IF EXISTS `checkin_template`;
CREATE TABLE `checkin_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `circle_id` BIGINT NOT NULL COMMENT '圈子ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `checkin_types` VARCHAR(50) NOT NULL DEFAULT '1,2,3' COMMENT '允许的打卡类型(逗号分隔)',
  `require_audio` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填音频 0否 1是',
  `require_content` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填心得 0否 1是',
  `require_image` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填图片 0否 1是',
  `min_word_count` INT NOT NULL DEFAULT 0 COMMENT '心得最少字数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_circle_id` (`circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡模板表';
```

---

### 2.15 用户成长/积分记录表 `user_growth_log`

```sql
DROP TABLE IF EXISTS `user_growth_log`;
CREATE TABLE `user_growth_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `change_type` TINYINT NOT NULL COMMENT '变动类型 1打卡 2连续打卡奖励 3获赞 4作业完成 5管理员调整',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分变动值(正负)',
  `remark` VARCHAR(255) DEFAULT '' COMMENT '说明',
  `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成长积分记录表';
```

---

### 2.16 用户每日打卡统计表（宽表，用于看板加速） `user_checkin_daily_stat`

```sql
DROP TABLE IF EXISTS `user_checkin_daily_stat`;
CREATE TABLE `user_checkin_daily_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `circle_id` BIGINT NOT NULL,
  `group_id` BIGINT DEFAULT NULL,
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `has_checkin` TINYINT NOT NULL DEFAULT 0 COMMENT '当日是否打卡 0否 1是',
  `audio_count` INT NOT NULL DEFAULT 0 COMMENT '听录音数(当日)',
  `read_count` INT NOT NULL DEFAULT 0 COMMENT '诵读文章数(当日)',
  `note_count` INT NOT NULL DEFAULT 0 COMMENT '写心得数(当日)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`),
  KEY `idx_circle_date` (`circle_id`, `stat_date`),
  KEY `idx_group_date` (`group_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日打卡统计表(看板加速)';
```

---

### 2.17 操作日志表 `sys_oper_log`（管理端审计）

```sql
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '操作人ID',
  `module` VARCHAR(50) DEFAULT '' COMMENT '模块',
  `operation` VARCHAR(100) DEFAULT '' COMMENT '操作描述',
  `method` VARCHAR(200) DEFAULT '' COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
  `cost_time` BIGINT DEFAULT 0 COMMENT '耗时(ms)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

---

## 三、初始化种子数据

### 3.1 初始化超级管理员与示例用户

> 密码统一为 `123456` 的 BCrypt 加密值（示例，请上线前重置）  
> `$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2`

```sql
-- 超级管理员
INSERT INTO `sys_user` (`open_id`, `username`, `password`, `nickname`, `avatar`, `role`, `position`, `status`)
VALUES ('admin_openid_001', 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '', 'SUPER_ADMIN', '管理员', 1);

-- 团队管理员
INSERT INTO `sys_user` (`open_id`, `username`, `password`, `nickname`, `role`, `position`, `status`)
VALUES ('team_admin_openid', 'teamadmin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张经理', 'TEAM_ADMIN', '部门经理', 1);

-- 普通成员
INSERT INTO `sys_user` (`open_id`, `nickname`, `role`, `position`, `status`)
VALUES 
('member_openid_001', '小懒猪爱吃薯条', 'MEMBER', '普通员工', 1),
('member_openid_002', '沙驰飞飞', 'MEMBER', '普通员工', 1),
('member_openid_003', '细雨凝香', 'MEMBER', '普通员工', 1),
('member_openid_004', '小武17010494949', 'MEMBER', '普通员工', 1);
```

### 3.2 示例圈子与小组

```sql
INSERT INTO `circle` (`name`, `description`, `owner_id`, `member_count`, `invite_code`)
VALUES ('环境试验科学事业部', '团队读书学习圈', 2, 4, 'CIRCLE2026');

INSERT INTO `t_group` (`circle_id`, `name`, `leader_id`, `member_count`, `invite_code`)
VALUES 
(1, '环境一组', 3, 2, 'GROUP001'),
(1, '环境二组', 4, 2, 'GROUP002');
```

### 3.3 更新用户所属圈子/小组

```sql
UPDATE `sys_user` SET `circle_id` = 1, `group_id` = 1 WHERE `id` IN (1, 2, 3);
UPDATE `sys_user` SET `circle_id` = 1, `group_id` = 2 WHERE `id` IN (4, 5);
```

### 3.4 打卡规则初始化（按职位）

```sql
INSERT INTO `checkin_rule` (`circle_id`, `position`, `required_days_per_week`, `required_days_per_month`, `required_days_per_year`)
VALUES 
(1, '普通员工', 5, 20, 240),
(1, '部门经理', 6, 24, 280),
(1, '管理员', 7, 28, 330);
```

### 3.5 示例书籍（对应 UI 截图）

```sql
INSERT INTO `book` (`title`, `author`, `publisher`, `category`, `book_type`, `description`, `recommend_reason`, `checkin_count`, `is_public`)
VALUES 
('阿里局：中供系英雄志', '和阳', '广东经济出版社', '经营之道', 1, '讲述阿里中供铁军发展历程。', '', 0, 1),
('京瓷哲学', '(日) 稻盛和夫', '东方出版社', '人生哲学', 1, '稻盛和夫经营哲学。', '以“敬天爱人”哲学统一思想，激发内生动力，塑造高绩效团队，实现共同成长。', 0, 1),
('赌在技术开发上', '(日) 稻盛和夫', '机械工业出版社', '经营之道', 1, '技术开发与经营理念。', '汲取技术创新的信念与心法，锤炼极致“心”精神，赋能组织持续成长。', 0, 1),
('活法', '(日) 稻盛和夫', '东方出版社', '人生哲学', 1, '稻盛和夫的人生哲学。', '将感激刻于童心的“隐蔽念佛”。', 0, 1),
('日航的奇迹', '(日) 稻盛和夫', '东方出版社', '经营之道', 1, '日航重建奇迹。', '', 0, 1);
```

### 3.6 示例章节

```sql
INSERT INTO `book_chapter` (`book_id`, `chapter_no`, `title`, `content`)
VALUES 
(4, 1, '第一章 让梦想成真', '内容略...'),
(4, 2, '第二章 原理原则', '内容略...'),
(4, 3, '第三章 第5节：将感谢刻于童心的“隐蔽念佛”', '内容略...'),
(4, 4, '第四章 第13节：从自然界学习“知足”', '内容略...'),
(5, 1, '第七章 第四节', '内容略...');
```

### 3.7 示例打卡记录

```sql
INSERT INTO `checkin_record` (`user_id`, `circle_id`, `group_id`, `book_id`, `chapter_id`, `chapter_title`, `checkin_type`, `content`, `audio_url`, `checkin_date`, `status`)
VALUES 
(3, 1, 1, 4, 3, '第三章 第5节：将感谢刻于童心的“隐蔽念佛”', 3, '磨砺心志的“六项精进”...', '', '2026-10-21', 1),
(3, 1, 1, 5, 5, '第七章 第四节', 2, '', 'https://example.com/audio/demo.mp3', '2026-10-22', 1),
(4, 1, 1, 4, 3, '第三章 第5节：将感谢刻于童心的“隐蔽念佛”', 3, '当然，不仅领导者需要磨炼心志...', '', '2026-10-21', 1);
```

### 3.8 打卡模板

```sql
INSERT INTO `checkin_template` (`circle_id`, `name`, `checkin_types`, `require_content`, `min_word_count`)
VALUES (1, '默认打卡模板', '1,2,3', 1, 50);
```

---

## 四、常用统计 SQL 示例

### 4.1 查询用户周/月/年打卡天数

```sql
-- 本周打卡天数
SELECT COUNT(DISTINCT checkin_date) AS week_days
FROM checkin_record
WHERE user_id = #{userId} 
  AND status = 1
  AND checkin_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
  AND checkin_date <= CURDATE();

-- 本月打卡天数
SELECT COUNT(DISTINCT checkin_date) AS month_days
FROM checkin_record
WHERE user_id = #{userId}
  AND status = 1
  AND DATE_FORMAT(checkin_date, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m');

-- 本年打卡天数
SELECT COUNT(DISTINCT checkin_date) AS year_days
FROM checkin_record
WHERE user_id = #{userId}
  AND status = 1
  AND YEAR(checkin_date) = YEAR(CURDATE());
```

### 4.2 圈子今日打卡态势（看板）

```sql
SELECT 
  COUNT(DISTINCT CASE WHEN checkin_type = 2 THEN user_id END) AS audio_users,
  COUNT(DISTINCT CASE WHEN checkin_type = 3 THEN user_id END) AS note_users,
  COUNT(DISTINCT user_id) AS total_users
FROM checkin_record
WHERE circle_id = #{circleId}
  AND checkin_date = CURDATE()
  AND status = 1;
```

### 4.3 小组周排行榜

```sql
SELECT 
  r.group_id,
  g.name AS group_name,
  COUNT(DISTINCT r.user_id) AS active_users,
  COUNT(DISTINCT r.user_id, r.checkin_date) AS total_checkins
FROM checkin_record r
LEFT JOIN t_group g ON r.group_id = g.id
WHERE r.circle_id = #{circleId}
  AND r.status = 1
  AND r.checkin_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
GROUP BY r.group_id, g.name
ORDER BY total_checkins DESC
LIMIT 10;
```

### 4.4 打卡率趋势（近30天）

```sql
SELECT 
  checkin_date,
  COUNT(DISTINCT user_id) AS checkin_users
FROM checkin_record
WHERE circle_id = #{circleId}
  AND status = 1
  AND checkin_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY checkin_date
ORDER BY checkin_date;
```

---

## 五、索引与性能说明

1. **高频查询字段必须建索引**：`checkin_record` 的 `(user_id, checkin_date)`、`(circle_id, checkin_date)` 联合索引覆盖周/月/年统计。
2. **排行榜用 Redis ZSet**：实时排行写入 Redis（Key 如 `rank:circle:{id}:week:2026-W42`），MySQL 的 `ranking_snapshot` 仅做持久化与历史查询。
3. **看板统计**：使用 `user_checkin_daily_stat` 宽表，由定时任务（每日 0:05）从 `checkin_record` 聚合写入，避免实时 `COUNT(DISTINCT)` 扫描大表。
4. **逻辑删除**：统一用 `deleted` 字段，MyBatis-Plus 全局配置 `@TableLogic`。
5. **全文检索**：`book` 表建立 `FULLTEXT` 索引，配合 `ngram` 分词器支持中文书名搜索（需 MySQL 配置 `ngram_token_size`，默认 2）。
6. **字符集**：全库 `utf8mb4`，支持 emoji 昵称与打卡内容。

---

## 六、表清单总览

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | `sys_user` | 用户表 |
| 2 | `circle` | 圈子/部门 |
| 3 | `t_group` | 小组 |
| 4 | `book` | 书籍 |
| 5 | `book_chapter` | 书籍章节 |
| 6 | `book_shelf` | 书架关联 |
| 7 | `checkin_record` | 打卡记录（核心） |
| 8 | `checkin_rule` | 打卡规则（按职位） |
| 9 | `ranking_snapshot` | 排行榜快照 |
| 10 | `study_job` | 学习作业 |
| 11 | `notification` | 消息通知 |
| 12 | `user_like` | 点赞 |
| 13 | `user_favorite` | 收藏 |
| 14 | `checkin_comment` | 评论 |
| 15 | `user_follow` | 用户关注 |
| 16 | `checkin_template` | 打卡模板 |
| 17 | `user_growth_log` | 积分成长记录 |
| 18 | `user_checkin_daily_stat` | 每日打卡统计宽表 |
| 19 | `sys_oper_log` | 操作日志 |

> **共 19 张表**，已完全移除订单、发票、VIP 等支付相关表与字段。脚本可直接在 MySQL 8.0 中按顺序执行，种子数据用于本地联调与 UI 还原演示。