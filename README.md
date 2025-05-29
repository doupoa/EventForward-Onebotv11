# EventForward-Onebotv11

[English](#english)

本插件仅适用于 **Fabric 服务端**，遵循[Onebot v11](https://github.com/botuniverse/onebot-11)协议转发Minecraft事件至群聊

## 前置条件

- [Fabric Loader 1.21.5 或更高](https://fabricmc.net/)
- [Fabric API 0.119.5+1.21.5 或更高](https://modrinth.com/mod/fabric-api)
- [Java21 或更高](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
- [Onebot v11 服务器](https://github.com/NapNeko/NapCatQQ)

## 运行方法

1. 从发布页面下载插件的 `.jar` 文件。
2. 将 `.jar` 文件放入你的 Fabric 服务器的 `mods` 文件夹中。
3. 启动服务器，插件将自动加载。
4. 首次启动后，在服务器根目录下的 `config` 文件夹中会生成 `event-forward-obv11.json` 配置文件。
5. 根据需要修改配置文件中的参数。

## 构建方法

1. 克隆仓库到本地。
2. 在项目根目录下运行 `gradlew build` 命令进行构建。
3. 构建完成后，在 `build/libs` 文件夹中可以找到 `.jar` 文件。

## 支持转发的事件

- 玩家加入服务器 (PlayerJoin)
- 玩家离开服务器 (PlayerLeave)
- 玩家聊天 (PlayerChat)

## 转发协议

本插件支持两种协议，HTTP单向转发游戏事件，适用于只想获取游戏事件的需求。Websocket双向转发游戏事件和群聊消息，适用于需要与群聊互动的需求。

- **HTTP客户端**：本插件主动向 OneBot 服务器发送事件请求 （单向）
- **Websocket服务端**：OneBot 服务器主动连接本插件并交换数据 （双向）

## 配置文件

本插件在首次加载后将在服务端根目录下的`config`文件夹生成 `event-forward-obv11.json` 配置文件，内容如下：

```json5
{
  "obServer": "127.0.0.1",
  // Onebotv11服务器地址
  "obPort": 8080,
  // 服务器端口
  "obToken": "",
  // 鉴权token
  "forwardMethod": "http",
  // 请求方法 可选 http 和 ws
  "forwardGroup": "",
  // 要转发的群聊群号
  "adminUsers": [
    ""
  ],
  // 管理员账号
  "commandPrefix": [
    "!"
  ],
  // 指令前缀
  "messagePrefix":[
    "~","～"
  ]
  //消息前缀
}
```
后续更新插件时您只需覆盖旧版本插件,服务器启动时会自动合并旧配置至新文件.

## Websocket协议群聊指令

选用Websocket协议后，在群聊中您可以进行以下操作向服务端发送指令：

- **发送 `<消息前缀>[消息内容]`** ：发送以**消息前缀**开头的文本将会以全服广播的方式转发至服务端中。
- **回复机器人发送的内容并艾特**：在群聊中回复机器人的文本将会以全服广播的方式转发至服务端中。一般回复用户时会自带艾特，在此状态机器人才会转发消息至游戏。
- **发送 `<指令前缀>[指令] [参数]`**：发送以**指令前缀**开头的文本将会以指令的形式转发至服务端中(若未配置管理员账号将忽略)。
- **发送 `<指令前缀>list`**: 列出服务器所有成员，所有用户可使用。

## EventForward-Onebotv11 (English)<a id="english"/>

This plugin is designed exclusively for **Fabric servers** and forwards Minecraft events to group chats following
the [Onebot v11](https://github.com/botuniverse/onebot-11) protocol.

## Prerequisites

- [Fabric Loader 1.21.5 or higher](https://fabricmc.net/)
- [Fabric API 0.119.5+1.21.5 or higher](https://modrinth.com/mod/fabric-api)
- [Java 21 or higher](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
- [Onebot v11 server](https://github.com/NapNeko/NapCatQQ)

## How to Run

1. Download the `.jar` file from the releases page.
2. Place the `.jar` file in your Fabric server's `mods` folder.
3. Start the server; the plugin will load automatically.
4. After the first launch, a configuration file named `event-forward-obv11.json` will be generated in the `config`
   folder under the server root directory.
5. Modify the parameters in the configuration file as needed.

## Build Instructions

1. Clone the repository locally.
2. Run the `gradlew build` command in the project root directory.
3. After building, the `.jar` file can be found in the `build/libs` folder.

## Supported Events

- Player Join (PlayerJoin)
- Player Leave (PlayerLeave)
- Player Chat (PlayerChat)

## Forwarding Protocols

This plugin supports two protocols: HTTP for one-way event forwarding (suitable for simply receiving game events) and
Websocket for bidirectional event and group chat message forwarding (suitable for interactive scenarios).

- **HTTP Client**: The plugin actively sends event requests to the OneBot server (one-way).
- **Websocket Server**: The OneBot server actively connects to the plugin to exchange data (bidirectional).

## Configuration File

After the first load, the plugin generates a configuration file named `event-forward-obv11.json` in the `config` folder
under the server root directory. The content is as follows:

```json5
{
  "obServer": "127.0.0.1",
  // Onebotv11 server address
  "obPort": 8080,
  // Server port
  "obToken": "",
  // Authentication token
  "forwardMethod": "http",
  // Request method (options: http or ws)
  "forwardGroup": "",
  // Target group chat ID
  "adminUsers": [
    ""
  ],
  // Administrator accounts
  "commandPrefix": [
    "!"
  ],
  // instruction prefix
  "messagePrefix":[
    "~","～"
  ]
  //message prefix
}
```
When updating the plug-in later, you only need to overwrite the old version plug-in, and the old configuration will be automatically merged into the new file when the server starts.

## Websocket Protocol Group Chat Commands

When using the Websocket protocol, you can send the following commands to the server from the group chat:

- **send `<message prefix>[message content] `**: the text sent starting with**message prefix** will be forwarded to the server in the form of full service broadcast.
- **reply to the content sent by the robot and Aite**: the text replying to the robot in the group chat will be forwarded to the server in the form of full-service broadcast. Generally, when replying to the user, the robot will bring its own Aite, and in this state, the robot will forward the message to the game.
- **send `<instruction prefix>[instruction] [parameter] `**: text sent starting with **instruction prefix**will be forwarded to the server in the form of instructions (ignored if the administrator account is not configured).
- **send `<instruction prefix>list`**: lists all members of the server, which can be used by all users.