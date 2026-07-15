# 高并发秒杀商城

从零实现的高并发秒杀商城系统，重点展示**技术选型能力**和**工程实践**。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.4 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.x（L2）+ Caffeine（L1 热点隔离） |
| 消息队列 | RocketMQ 5.x（待接入） |
| Binlog 监听 | Canal（待接入） |
| 限流 | Sentinel（待接入） |
| 前端 | Vue3 + Element Plus + Pinia（待开发） |
| 压测 | JMeter |

## 已实现功能

### 基础商城
- 用户注册/登录（JWT双Token + BCrypt）
- 商品列表/详情（Redis缓存 + 防雪崩）
- 购物车（Redis Hash）
- 普通下单（@Transactional + 乐观锁扣库存）

### 秒杀核心
- 秒杀活动管理 + 定时预热（@Scheduled）
- **Redis Lua 原子扣减**：库存校验+扣减+防重三合一
- **DB 乐观锁兜底**：双保险防超卖
- **Caffeine 本地缓存**：热点商品 JVM 堆内隔离
- **Redis Pub/Sub 失效广播**：多节点本地缓存同步

### 工程规范
- Result 统一返回 + ErrorCode 枚举 + 全局异常处理
- DTO/VO/Entity 数据对象严格分层
- RedisKeyUtil 统一生成 Redis Key
- SLF4J 日志规范

## 项目结构

```
com/seckill/mall/
├── common/          Result, ErrorCode, BusinessException, GlobalExceptionHandler
├── config/          Redis, Caffeine, WebMVC, CacheInvalidateListener
├── controller/      User, Product, Cart, Order, Seckill
├── service/impl/    业务逻辑
├── mapper/          MyBatis-Plus Mapper
├── model/
│   ├── entity/      User, Product, Order, SeckillActivity
│   ├── dto/         请求对象
│   └── vo/          视图对象
├── scheduler/       ActivityPreheatTask
└── util/            JwtUtil, RedisKeyUtil
```

## 快速启动

1. MySQL 8.0 + Redis 7.x 必须已启动
2. 创建数据库：`CREATE DATABASE seckill_db DEFAULT CHARSET utf8mb4;`
3. 执行 `src/main/resources/schema.sql` 建表（待整理）
4. 修改 `application.yml` 中的数据库密码
5. 启动 `SeckillMallApplication`
6. 访问 `http://localhost:8080`

## 核心技术决策

| 问题 | 方案A | 方案B | 选用 | 理由 |
|------|-------|-------|------|------|
| 超卖 | 分布式锁 | **Redis Lua** | Lua | 单线程原子，无锁竞争 |
| 一致性 | 延时双删 | **Canal+Binlog** | Canal | 不受主从延迟影响 |
| 热点Key | 纯Redis | **Caffeine二级** | Caffeine | 99%请求JVM返回 |
| 削峰 | 同步下单 | **RocketMQ** | MQ | 解耦+异步+限流 |
