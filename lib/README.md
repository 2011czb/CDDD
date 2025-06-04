# 项目依赖库

## KryoNet 网络库
项目使用 KryoNet 2.21 版本进行网络通信。需要以下 JAR 文件：

### 必需文件
将以下文件放置在 `lib/kryonet-2.21/` 目录下：

- kryonet-2.21.jar
- kryo-2.24.0.jar
- minlog-1.2.jar
- objenesis-2.1.jar
- jsonbeans-0.7.jar

### 下载地址
可以从以下地址下载所需文件：

1. KryoNet (包含所有依赖): https://github.com/EsotericSoftware/kryonet/releases/tag/2.21
2. 或者使用 Maven/Gradle 自动管理依赖（推荐）：
   ```gradle
   dependencies {
       implementation 'com.esotericsoftware:kryonet:2.22.0-RC1'
   }
   ```

### 目录结构
```
lib/
├── kryonet-2.21/
│   ├── kryonet-2.21.jar
│   ├── kryo-2.24.0.jar
│   ├── minlog-1.2.jar
│   ├── objenesis-2.1.jar
│   └── jsonbeans-0.7.jar
└── README.md
``` 