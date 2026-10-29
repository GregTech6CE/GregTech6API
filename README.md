# GregTech6API

GT6CE 的独立公共 API 仓库，提供纯 Java 契约与按 Minecraft/加载器版本编译的游戏接口。内容表、执行引擎、服务实现、SPI provider 和游戏资产由 [GregTech6](https://github.com/GregTech6CE/GregTech6) 主库维护。

## 源码与构建

| 目录 | 内容 |
| --- | --- |
| `common/src/main/java/gregtech/api/` | Java 17 公共模型、能源协议、机器与材料声明、附属 SPI |
| `common/src/test/` | 仅依赖公共 JAR 的消费者与契约回归 |
| `minecraft/src/main/java/gregtech/api/` | Minecraft 游戏侧 API，经 Stonecutter 生成两个版本 |

在仓库根运行，Windows 使用 `gradlew.bat`：

```bash
./gradlew -p common check assemble
# 游戏 API 按目标选择一条
./gradlew :api-platform:1.20.1-forge:build
./gradlew :api-platform:1.21.1-neoforge:build
```

纯 API 命令不配置 Minecraft。Forge 使用 Java 17，NeoForge 使用 Java 21；可通过 `JAVA17_HOME`、`JAVA21_HOME` 提供工具链。共享输入中的 `//?` 分支由 Stonecutter 处理，不直接删除注释中的另一版本源码。

| 坐标 | 产物目录 |
| --- | --- |
| `gregtech:gt6ce-api:0.2.0` | `common/build/libs/` |
| `gregtech:gt6ce-addon-sdk-1.20.1-forge:0.2.0` | `minecraft/versions/1.20.1-forge/build/libs/` |
| `gregtech:gt6ce-addon-sdk-1.21.1-neoforge:0.2.0` | `minecraft/versions/1.21.1-neoforge/build/libs/` |

每项包含 binary、sources 和 javadoc JAR。游戏 SDK 使用开发环境的 Mojang 名称，仅声明公共库为传递依赖；消费者还需对应 Minecraft/加载器开发环境。这些坐标是发布配置，不表示已上传公共 Maven 服务。公共库的 publication 为 `api`，平台为 `addonApi`，发布仓库需另行配置。

## 主模组与附属接入

主库通过 Git 子模块锁定本仓库提交，并在同一 Gradle 构建中接入 `:api` 和 `:api-platform:<node>`；本仓库可以自行构建，不依赖主库。主模组负责运行时携带唯一一份 API，Forge 游戏 API 随主模组重映射，纯 Java API 保持原字节码。

附属使用 `compileOnly` 依赖匹配的 API；使用本地文件时同时提供公共库与目标平台 SDK。不要把 API 类重新打进附属，也不要把 SDK 放入 `mods/`。`AddonRuntime`、`AddonPlatform`、`AddonLoader` 由宿主在初始化阶段绑定；脱离宿主可使用声明和纯模型，但不能查询宿主材料、物品或配方。客户端接口只在客户端入口加载。

## 0.2 迁移与兼容

API 版本独立于主模组版本，以 `common/build.gradle` 为准，`GregTechAddons.apiVersion()` 对应 minor。使用 API 0.2 的附属必须配合提供该 API 的 GT6CE 构建；数字版本不能代替实际运行时与平台匹配。

- `BasicMachineParameters.Builder.energy(Tag, long)` 改为 `energy(String, long)`，例如 `energy(EnergyType.EU, 32)`。
- `BasicMachineParameters.energyTag()` 移除，改用 `energyType()`；Tag 转换留在宿主。
- `MachineControllerKind.builtIn(String)` 移出 API，附属应显式选择枚举值。
- 基础机器仍只接受已知能源，使用 `EnergyType.requireKnown`；原本支持自定义类型与 `*` 的行为声明保持不变。TU/HU 与显式/未声明能源范围保持原规则。
- `AddonLimits` 的 `MAX_PER_TICK`、`MAX_CAPACITY`、`MAX_EFFICIENCY`、`DEFAULT_EFFICIENCY` 不再是编译期常量：数值不变，但不能再用于 `switch` 的 case 标签、注解参数或其他常量表达式。重新编译后会在运行时读取宿主提供的当前值。

这些入口的旧二进制需要迁移后重新编译。包名、注册 ID、SPI 服务路径和配方单位不因拆仓改变。可变模型和内部辅助类不自动承诺长期兼容。

## 来源与许可

代码提取自 [GregTech6CE/GregTech6](https://github.com/GregTech6CE/GregTech6)，以 `e1dd804c258a10dddd84b47d3f92aed25655f40d` 为来源基线，包含独立 API 模块化修改。原作者与来源声明保留在源码和 NOTICE 中。代码采用 LGPL-3.0-or-later，见 [LICENSE](LICENSE) 与 [GPL-3.0.txt](GPL-3.0.txt)；本仓库不携带主模组游戏资产。
