# 锄大地卡牌游戏联机对战系统

## 项目概述
本项目实现了一个基于Java的锄大地卡牌游戏联机对战系统，支持多人在线对战。系统采用客户端-服务器架构，使用KryoNet作为网络通信框架。

## 技术栈
- Java 8
- KryoNet (网络通信)
- Kryo (序列化)
- MinLog (日志)

## 系统架构

### 1. 网络通信核心类
#### NetworkPacket
定义了所有网络数据包的结构：
- `JoinGameRequest`：玩家加入游戏请求
  - `playerName`：玩家名称
- `GameStarted`：游戏开始通知
  - `playerIds`：玩家ID列表
  - `initialHand`：初始手牌
- `PlayCardsRequest`：出牌请求
  - `playerId`：玩家ID
  - `cards`：要出的牌
- `PlayCardsResponse`：出牌响应
  - `isValid`：是否有效
  - `playerId`：玩家ID
  - `cards`：出的牌
- `PassRequest`：过牌请求
  - `playerId`：玩家ID
- `GameStateUpdate`：游戏状态更新
  - `currentPlayerId`：当前玩家ID
  - `lastPlayedCards`：上一手牌
  - `lastPlayerId`：上一玩家ID
  - `gameEnded`：游戏是否结束
  - `winnerId`：获胜者ID
- `PlayerDisconnected`：玩家断开连接通知
  - `playerId`：玩家ID

### 2. 服务器端实现
#### GameServer
游戏服务器类，负责：
- 启动服务器，监听端口
- 处理玩家加入请求
- 处理玩家出牌请求
- 处理玩家过牌请求
- 广播游戏状态更新
- 处理玩家断开连接

### 3. 客户端实现
#### GameClient
游戏客户端类，负责：
- 连接到服务器
- 发送加入游戏请求
- 发送出牌请求
- 发送过牌请求
- 处理游戏开始通知
- 处理出牌响应
- 处理游戏状态更新
- 处理玩家断开连接

### 4. 游戏规则实现
#### Rule接口
定义基本规则方法：
- `isValidPlay()`：判断出牌是否有效
- `getValidPlays()`：获取所有有效出牌
- `canCompare()`：判断两组牌是否可以比较
- `compareCards()`：比较两组牌的大小

#### 规则实现
- `NorthRule`：北方规则实现
- `SouthRule`：南方规则实现

### 5. 牌型判断
#### PokerPattern
牌型基类：
- `match()`：判断牌组是否符合当前牌型
- `getCritical()`：获取关键牌权重

#### PokerPatternMatcher
牌型匹配器：
- `matchPattern()`：判断牌组属于哪种牌型

#### PlayablePatternUtil
可出牌型工具类：
- `getAllValidPlays()`：获取所有可能的有效出牌

## 数据流程

### 客户端加入游戏
```
客户端 -> JoinGameRequest -> 服务器
服务器 -> GameStarted -> 客户端
```

### 玩家出牌
```
客户端 -> PlayCardsRequest -> 服务器
服务器 -> PlayCardsResponse -> 客户端
服务器 -> GameStateUpdate -> 所有客户端
```

### 玩家过牌
```
客户端 -> PassRequest -> 服务器
服务器 -> GameStateUpdate -> 所有客户端
```

### 玩家断开连接
```
服务器 -> PlayerDisconnected -> 所有客户端
```

## 游戏状态同步
- 服务器维护游戏状态
- 每次状态变化时广播GameStateUpdate
- 客户端根据GameStateUpdate更新本地状态
- 状态包含：当前玩家、上一手牌、上一玩家、游戏是否结束、获胜者

## 错误处理
- 服务器验证所有请求
- 客户端处理所有响应
- 断开连接时通知其他玩家
- 游戏结束时清理资源

## 构建和运行
1. 确保安装了Java 8或更高版本
2. 使用Maven构建项目：
   ```bash
   mvn clean package
   ```
3. 运行服务器：
   ```bash
   java -cp target/cddd-1.0-SNAPSHOT-jar-with-dependencies.jar Game.NetworkGameLauncher server
   ```
4. 运行客户端：
   ```bash
   java -cp target/cddd-1.0-SNAPSHOT-jar-with-dependencies.jar Game.NetworkGameLauncher client
   ```

## 注意事项
1. 服务器默认使用北方规则
2. 确保服务器端口未被占用
3. 客户端需要知道服务器的IP地址和端口
4. 游戏过程中保持网络连接稳定
