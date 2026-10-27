团队读书打卡小程序 · 完整开发文档（终版）

文档版本：v1.2
更新说明：

1. 彻底移除支付、订单、VIP、发票等商业化模块及字段。
2. 修正了数据库统计 SQL 的语法报错（兼容 MySQL 5.7 / 8.0，并区分纯 SQL 与 MyBatis 写法）。
3. 完善了液态玻璃 UI、暗/亮主题、悬浮导航栏的开发规范。

技术栈：Spring Boot 3.5 + UniApp (Vue 3) + MySQL 8 + Redis
UI 风格：液态玻璃（Liquid Glass）+ 悬浮底部导航栏 + 暗/亮主题切换

---

一、项目概述

1.1 产品定位

一款面向企业/团队内部的纯内部管理型读书打卡小程序。管理员统一记录打卡数据，队员上传读书打卡图片、朗读音频或心得笔记，支持开放书库选书、排行榜激励、可视化数据看板。核心目标是降低团队读书管理成本，提升成员阅读坚持率。不涉及任何支付、订单、VIP 及发票相关商业化功能。

1.2 角色定义

角色 权限说明
超级管理员 全部功能，含账号注销、初始数据导入、系统设置
团队管理员 布置/调整作业、导出打卡数据、设置打卡规则与模板、查看统计看板
普通队员 上传打卡（图片/音频/心得）、选书阅读、查看排行榜、个人成长

1.3 核心流程

```
队员登录 → 选择书籍（书库/自选）→ 上传打卡记录（朗读音频/心得笔记/图片）
    → 管理员审核/记录 → 打卡数据入库 → 排行榜/看板实时更新
```

---

二、功能清单（已移除支付相关）

2.1 核心打卡功能

· 管理员统一记录打卡
· 队员上传打卡（图片/朗读音频/心得笔记/学习录像）
· 开放书库选书、加入书架
· 排行榜（周/月/年，个人/小组/圈子）
· 可视化数据展示
· 职位分级，不同职位周打卡合格天数不同
· 查询队员周/月/年打卡天数

2.2 圈子/小组体系

· 部门/小组/学习圈三级结构
· 创建小组、邀请加入、退出小组
· 小组维度排行榜与活跃度对比
· 圈主/组长管理本圈成员与任务

2.3 个人中心体系

· 个人信息、我的收藏、我的推广
· 帮助与反馈、消息通知、个人成长
· 服务热线、我的地址
· 用户协议、关于小程序、设置

2.4 管理员后台面板

· 布置/调整学习作业
· 导出打卡数据（Excel）
· 读书 AI 考试 / 云培训 AI 考试
· 打卡规则设置（按职位配置合格天数）
· 初始数据导入
· 打卡模板设置
· 注销账号

2.5 主题与 UI 增强

· 暗/亮主题切换（跟随系统 + 手动切换）
· 液态玻璃卡片与悬浮导航栏

⚠️ 已移除模块：我的订单（待付款/待评价/售后）、开通 VIP、我的发票抬头、书籍价格展示与购买入口。

---

三、完整项目架构

3.1 后端架构（Spring Boot 3.5）

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

后端技术选型：

技术 用途 说明
Spring Boot 3.5 基础框架 JDK 17+
Sa-Token 认证与权限 使用 sa-token-spring-boot3-starter
MyBatis-Plus ORM 提供 CRUD 与分页
MySQL 8 主数据库 业务数据
Redis 缓存/排行榜/会话 排行榜用 ZSet
EasyExcel 打卡数据导出 管理员导出 Excel
Hutool 工具库 日期/加密/图片处理
WebSocket（可选） 实时打卡态势 看板数据推送

---

3.2 前端架构（UniApp Vue 3）

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
│   │   ├── mine/                        # 我的
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

前端技术选型：

技术 用途
Vue 3 + TypeScript 基础框架
Pinia 状态管理
uni-ui（官方） 基础组件
uView Plus 补充组件
fc-liquid-glass 液态玻璃效果插件
ECharts（uni-app 版） 统计图表
Vite 构建工具

---

四、功能细节与页面设计（无支付相关）

4.1 首页（对应截图 3）

顶部区域：

· 用户头像 + 昵称 + 等级标签（自由版）
· 背景图（可更换）
· 统计数字：关注 / 粉丝 / 获赞 / 积分 / 打卡
· 快捷入口：我的收藏 | 联系客服（在线 06:00–23:00）

~~我的订单~~：已移除

功能列表：~~开通 VIP~~（已移除） → 我的推广 → 帮助与反馈 → 个人信息 → 消息通知 → 个人成长 → 服务热线 → 我的地址 → ~~我的发票抬头~~（已移除） → 用户协议 → 关于小程序 → 设置

底部：悬浮导航栏（首页 / 通讯录 / 打卡 / 书城 / 我的）

4.2 书城（对应截图 4）

· 搜索框（书籍名称）
· 分类 Tab：经营之道 / 人生哲学 / 领导力 / 自我精进 / 社会…
· 书籍卡片：封面 + 书名 + 作者/出版社 + 电子书标签 + 打卡人数 + “加入书架” 按钮
· 已移除：价格展示（¥）、购物车购买按钮

4.3 打卡动态流（对应截图 1、2）

· 顶部 Tab：任务 / 动态 / 看板 / 统计 / 圈简介 / 书架
· 动态 Tab 下二级 Tab：诵读 / 心得
· 打卡卡片：用户头像+昵称+日期+已打卡天数+浏览数 + 章节标题 + 音频播放器（诵读）或文字内容（心得）+ 点赞/收藏/评论/分享/编辑

4.4 看板（对应截图 1）

· 顶部统计：今日打卡 2/4694、总完成率 0%、活跃小组 0组
· 实时打卡态势：听录音 2/4694 (0%)、诵读文章 2/4694、写心得 0/4694
· Top 5 小组活跃度对比（进度条/柱状图）

4.5 统计（对应截图 2）

· 恭喜横幅（最佳分享）
· 三块数据卡：我已打卡天数、我在小组排名、我在圈子排名
· 本周学习标兵：前三名头像 + 皇冠（A先生/B先生/C先生）
· 打卡率图表：切换本年/本月/本周，柱状图展示

4.6 圈简介/小组成员（对应截图 2）

· 顶部 Tab：圈子 / 小组 / 部门
· 当前部门：环境试验科学事业部
· 邀请加入学习圈（按钮）+ 退出学习圈
· 共 4694 人

4.7 书架（对应截图 2）

· Tab：我的藏书 / 小组合集 / 精选集
· 空状态：“暂无书籍，当前分类还没有可阅读的书籍”

4.8 管理员功能（对应截图 1）

· 布置学习作业 / 调整学习作业
· 导出打卡数据（Excel）
· 读书 AI 考试 / 云培训 AI 考试
· 打卡规则（按职位设置每周合格天数）
· 初始数据导入
· 打卡模板设置
· 注销账号

---

五、数据库设计（核心表）

5.1 核心业务表清单

序号 表名 说明
1 sys_user 用户表
2 circle 圈子/部门
3 t_group 小组
4 book 书籍
5 book_chapter 书籍章节
6 book_shelf 书架关联
7 checkin_record 打卡记录（核心）
8 checkin_rule 打卡规则（按职位）
9 ranking_snapshot 排行榜快照
10 study_job 学习作业
11 notification 消息通知
12 user_like 点赞
13 user_favorite 收藏
14 checkin_comment 评论
15 user_follow 用户关注
16 checkin_template 打卡模板
17 user_growth_log 积分成长记录
18 user_checkin_daily_stat 每日打卡统计宽表
19 sys_oper_log 操作日志

共 19 张表，详细建表 SQL 见附件（或上一次回复内容）。关键索引说明：

· checkin_record 建立联合索引 (user_id, checkin_date)、(circle_id, checkin_date)，覆盖周/月/年统计查询。
· book 表建立 FULLTEXT 索引，配合 ngram 分词器支持中文书名搜索。
· 统一使用 deleted 字段做逻辑删除，MyBatis-Plus 配置 @TableLogic。

---

六、常用统计 SQL（修正兼容版）

注意：以下 SQL 已修正之前 #{} 占位符和 COUNT(DISTINCT a,b) 导致的报错。

· 纯 SQL 调试请直接使用以下代码（数字 1 为示例参数）。
· MyBatis 环境中，请将数字替换为 #{userId}、#{circleId} 等占位符，且以下语法兼容 MySQL 5.7 与 8.0。

6.1 查询用户周/月/年打卡天数

```sql
-- 本周打卡天数
SELECT COUNT(DISTINCT checkin_date) AS week_days
FROM checkin_record
WHERE user_id = 1
  AND status = 1
  AND checkin_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
  AND checkin_date <= CURDATE();

-- 本月打卡天数
SELECT COUNT(DISTINCT checkin_date) AS month_days
FROM checkin_record
WHERE user_id = 1
  AND status = 1
  AND DATE_FORMAT(checkin_date, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m');

-- 本年打卡天数
SELECT COUNT(DISTINCT checkin_date) AS year_days
FROM checkin_record
WHERE user_id = 1
  AND status = 1
  AND YEAR(checkin_date) = YEAR(CURDATE());
```

6.2 圈子今日打卡态势（看板）

```sql
SELECT 
  COUNT(DISTINCT CASE WHEN checkin_type = 2 THEN user_id END) AS audio_users,
  COUNT(DISTINCT CASE WHEN checkin_type = 3 THEN user_id END) AS note_users,
  COUNT(DISTINCT user_id) AS total_users
FROM checkin_record
WHERE circle_id = 1
  AND checkin_date = CURDATE()
  AND status = 1;
```

6.3 小组周排行榜（兼容 MySQL 5.7）

原错误写法：COUNT(DISTINCT r.user_id, r.checkin_date)（MySQL 5.7 不支持多列去重）
修正写法：使用 CONCAT 拼接后去重。

```sql
SELECT 
  r.group_id,
  g.name AS group_name,
  COUNT(DISTINCT r.user_id) AS active_users,
  COUNT(DISTINCT CONCAT(r.user_id, '_', r.checkin_date)) AS total_checkins
FROM checkin_record r
LEFT JOIN t_group g ON r.group_id = g.id
WHERE r.circle_id = 1
  AND r.status = 1
  AND r.checkin_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
GROUP BY r.group_id, g.name
ORDER BY total_checkins DESC
LIMIT 10;
```

6.4 打卡率趋势（近30天）

```sql
SELECT 
  checkin_date,
  COUNT(DISTINCT user_id) AS checkin_users
FROM checkin_record
WHERE circle_id = 1
  AND status = 1
  AND checkin_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY checkin_date
ORDER BY checkin_date;
```

---

七、开发规范与 UI 细节

7.1 液态玻璃效果实现

· 优先使用插件：fc-liquid-glass（全端适配，暗黑模式自动切换）
· 降级方案：非 iOS 26 设备使用 backdrop-filter: blur(20px) + 半透明白/深色背景
· 适用组件：悬浮导航栏、卡片、弹窗、按钮
· 注意：大量重叠玻璃组件可能影响性能，列表项避免滥用

7.2 主题切换规范

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

· 所有颜色使用 CSS 变量
· 图片资源准备两套或使用 CSS filter 适配

7.3 悬浮导航栏规范

· 高度：约 60px + 底部安全区
· 背景：液态玻璃效果 + 圆角
· 位置：position: fixed; bottom: 20px; left: 16px; right: 16px; border-radius: 30px;
· 中间打卡按钮：凸起圆形，使用渐变/玻璃高光
· 切换页面用 uni.switchTab（tabBar 页面）或 uni.navigateTo

7.4 接口规范

· 统一响应格式：{ code: 200, message: "success", data: {...} }
· 分页：{ records: [], total: 0, current: 1, size: 10 }
· 认证：Header 携带 satoken（Sa-Token 默认 token 名）
· 打卡上传：图片/音频走 OSS/本地存储，接口只传 URL

---

八、补充技术栈清单

类别 技术 用途
后端 Sa-Token 认证鉴权
后端 MyBatis-Plus ORM 与分页
后端 EasyExcel 导出打卡数据
后端 Redis + ZSet 排行榜实时计算
前端 Pinia 状态管理
前端 uni-ui + uView Plus UI 组件基础
前端 fc-liquid-glass 液态玻璃效果
前端 ECharts（uni-app 版） 统计图表
构建 Vite UniApp Vue 3 构建

---

九、开发里程碑建议

阶段 内容 周期
P0 后端基础框架 + 登录认证 + 用户/圈子/小组表 1 周
P1 书城/书架 + 打卡上传（图片/音频/心得） + 动态流 2 周
P2 排行榜 + 统计看板 + 管理员后台（规则/作业/导出） 2 周
P3 液态玻璃 UI + 悬浮导航栏 + 暗黑模式 1 周
P4 消息通知 + 个人成长 + 联调测试 + 上线 1 周

---

总结：本终版文档已彻底移除支付功能，全面梳理了前后端架构，并针对之前 SQL 调试中出现的语法兼容问题给出了修正方案。开发时建议按照 P0 至 P4 的里程碑稳步推进，重点保障打卡核心链路的流畅性。