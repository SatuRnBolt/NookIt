# MySQL 初始化说明

该目录提供 StudyRoom 项目的 MySQL 8 初始化脚本。

## 文件

- `init.sql`：完整建库脚本，包含建库、建表、索引、约束和基础种子数据。

## 使用方式

如果本地已经安装并启动 MySQL 8，可以直接执行：

```bash
mysql -u root -p < mysql/init.sql
```

脚本默认会执行以下行为：

1. 删除已有的 `study_room` 数据库。
2. 重新创建 `study_room` 数据库。
3. 创建核心业务表、扩展表和统计支撑表。
4. 写入基础种子数据。

## 已初始化的内容

- 多校区/多组织层级基础表：`campuses`、`organizations`
- 用户与多端身份表：`users`、`auth_identities`、`user_contact_methods`
- RBAC：`roles`、`permissions`、`role_permissions`、`user_roles`
- 自习室与座位：`study_rooms`、`room_open_rules`、`seats`、`seat_maps`
- 预约链路：`reservations`、`reservation_slots`、`room_checkin_codes`
- 违约与申诉：`violations`、`violation_appeals`
- 通知、AI、审计、统计、系统配置支撑表

## 默认种子数据

脚本会写入以下演示数据：

- 1 个主校区
- 4 个组织节点：学校、图书馆、计算机学院、数学学院
- 3 个用户：系统管理员、院系管理员、学生
- 3 个预置角色：系统管理员、普通管理员、院系管理员
- 16 个权限点
- 2 个示例自习室：公共阅览区、静音仓
- 10 个示例座位和当日签到码
- 系统参数、功能开关、通知模板、字典项

## 需要你们接入时注意的点

1. `auth_identities.credential_hash` 里现在放的是占位字符串，不是可直接登录的真实 bcrypt 哈希。接入后端前，需要替换成真实密码哈希。
2. `reservation_slots` 用来承接“同一学生同一时间只能有一个有效预约”和“同一座位同一时段不能被重复预约”的唯一性约束。后端创建预约时应在同一事务里同时写入 `reservations` 和 `reservation_slots`。
3. `study_rooms.total_capacity` 是缓存字段，真实容量应以 `seats` 聚合为准。后端维护座位时同步更新即可。
4. 统计表是汇总表，不应当作实时真相来源。管理端报表可以读汇总表，业务流程仍应以事务表为准。
5. 如果后续要按迁移工具管理，建议把 `init.sql` 拆成 `001_base.sql`、`002_space.sql`、`003_reservation.sql`、`004_seed.sql` 等批次。

## 建议的后续落地顺序

1. 先按该脚本生成 SQLAlchemy ORM 模型。
2. 再实现预约事务、签到事务和违约事务的服务层。
3. 最后补报表汇总任务、通知投递任务和 AI 助手动作执行。