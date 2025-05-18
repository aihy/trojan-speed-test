# Trojan 延迟测试工具

基于 JavaFX 的 Trojan 代理服务器延迟测试工具。

## 截图

![screenshot](https://raw.githubusercontent.com/aihy/trojan-speed-test/refs/heads/master/screenshot.png)

## 运行环境

- Java 21 或更高版本
- Maven 3.6 或更高版本
- Trojan 客户端

## 安装说明

1. 从 [trojan-gfw/trojan releases](https://github.com/trojan-gfw/trojan/releases) 下载 Trojan 客户端

2. 按以下结构放置文件：
```
trojan-speed-test/
├── src/
├── trojan/
│   ├── examples/
│   ├── config.json
│   └── trojan (可执行文件)
├── pom.xml
└── README.md
```

3. 手动执行一次 trojan 试试，确保操作系统允许执行

## 运行方法

使用 Maven 启动应用：

```sh
mvn clean javafx:run
```

输入 clashx 的配置URL后，会自动保存在 `~/.config/trojan-speed-test/config.properties` 中，后续启动时会自动读取。