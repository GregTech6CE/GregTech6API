# API 仓库工作指南

- 本仓库只维护 GT6CE 的公共 API，不放主模组内容表、执行引擎、加载器入口或游戏资源。
- `common/` 是 Java 17 公共协议、模型、声明和宿主服务接口，只依赖 JDK。它既可独立构建，也可作为主库的 `:api` 项目接入。
- `minecraft/src/main/java/` 是共享游戏侧 API，使用 Stonecutter 生成 Forge 1.20.1 / NeoForge 1.21.1 两个产物；保留条件标记与注释分支。不得引用主库实现。
- 文档使用中文，标识符沿用现有英文命名。开工先检查 `git status --short`，保留已有改动。
- 保留包名、注册 ID、SPI 服务名和单位。公开签名的不兼容修改必须更新 API 版本、迁移说明与调用方；不得承诺只换 JAR 即可兼容。
- 新的声明与构建代码对非法输入一律抛 `IllegalArgumentException`，不钳制、不静默替换为哨兵、不改写调用者数据。既有钳制行为（`GTMaterial` 的倍率、燃烧时间、粉碎与熔炼，`CrucibleContent` 的负数，`AddonRecipes.Line` 的时长与 presence 流体数量，`MaterialComponent` 的空材料哨兵）保持不变，并在 Javadoc 中写明；改动它们会改变下游结果。
- 宿主负责绑定服务和发现 provider。API 不自动加载 GT6CE，也不携带 provider 声明；客户端接口保持客户端入口隔离。
- 生产输入放对应源码集；重复运行的自动检查放 `src/test/` 并接入构建。临时日志、探针和下载放忽略的 `work/` 或 `build/`。
- 默认验证 `./gradlew -p common check assemble`，涉及游戏接口时选择一个相关节点的定向编译或 SDK 构建，不默认双平台全量验证。编译通过不等于游戏行为已验证。
- 未明确要求不提交、推送或发布。结束时说明改动、验证与未覆盖事项。
