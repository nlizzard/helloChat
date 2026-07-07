# helloChat Docker Compose

在本目录执行：

```bash
docker compose up -d
```

Compose 会从源码构建业务镜像，并启动网关、认证服务、主业务服务、文件服务、3 个 Netty 聊天服务实例，以及 MySQL、Redis、RabbitMQ、ZooKeeper、Nacos 和 MinIO。MySQL 首次启动时会自动导入根目录的 `Hchat.sql`。

业务镜像在 Docker 构建阶段执行 Maven 打包，仓库里不需要提交 `target/` 目录，拉取项目后可直接在 `docker` 目录执行 `docker compose up -d`。RabbitMQ 的持久化数据位于 `docker/rabbitmq_data/mnesia`，`.erlang.cookie` 保留在容器内部以满足 RabbitMQ 的权限要求。

默认外部入口：

- HTTP 网关：http://localhost:1000
- Netty WebSocket：localhost:875、localhost:885、localhost:895
- MySQL：localhost:3306
- Redis：localhost:6379
- Nacos 控制台：http://localhost:18080
- Nacos 服务端口：localhost:18848
- RabbitMQ 控制台：http://localhost:15672，账号/密码 `nlizzard` / `nlizzard`
- MinIO 控制台：http://localhost:9001，账号/密码 `minio` / `miniominio`

默认会启动 3 个 Netty 实例，端口分别为 `875`、`885`、`895`。每个实例会把自己的 `NETTY_ADVERTISED_HOST + 端口` 注册到 ZooKeeper，`main-service` 的 `/chat/getNettyOnlineInfo` 会从这些节点中返回当前在线人数较少的节点。

如果客户端不在 Docker 主机本机运行，启动前设置 `NETTY_ADVERTISED_HOST` 为主机可访问 IP，例如：

```bash
NETTY_ADVERTISED_HOST=192.168.1.10 docker compose up -d
```

常用端口可通过环境变量覆盖：`GATEWAY_PORT`、`NETTY_PORT`、`NETTY_PORT_885`、`NETTY_PORT_895`、`MYSQL_PORT`、`REDIS_PORT`、`ZOOKEEPER_PORT`、`NACOS_CONSOLE_PORT`、`NACOS_PORT`、`NACOS_GRPC_PORT`、`RABBITMQ_AMQP_PORT`、`RABBITMQ_MANAGEMENT_PORT`、`MINIO_API_PORT`、`MINIO_CONSOLE_PORT`。
