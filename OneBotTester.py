import asyncio
import json
import time
import uuid
import websockets

from typing import Dict, Any, Union
import random


def get_sender():
    return {
        "user_id": random.randint(10000, 200000),
        "nickname": random.choice(["测试用户1", "测试用户2", "测试用户3"]),
        "card": random.choice(["测试内容1", "测试内容2", "测试内容3"]),
        "sex": random.choice(["male", "female", "unknown"]),
        "age": random.randint(1, 100),
        "area": "中国",
        "level": random.randint(1, 100),
        "role": random.choice(["owner", "admin", "member"]),
        "title": random.choice(["头衔1", "头衔2", "头衔", ""]),
    }


class OneBotV11Tester:
    def __init__(self, ws_url: str = "ws://127.0.0.1:8080/forward/v11"):
        self.ws_url = ws_url
        self.websocket = None
        self.group_id = 123456  # 默认测试群号
        self.bot_id = 1234567890  # 默认测试机器人ID
        self.user_id = 10001    # 默认测试用户ID
        self.admin_id = 10001  # 默认测试管理员ID

    async def connect(self):
        """连接到 WebSocket 服务器"""
        print(f"正在连接到 {self.ws_url}...")
        self.websocket = await websockets.connect(self.ws_url)
        print("连接成功!")

    async def send_message(self, message: Dict[str, Any]):
        """发送消息到服务器"""
        if not self.websocket:
            raise ConnectionError("未连接到 WebSocket 服务器")

        # 添加必要的消息ID
        message["id"] = str(uuid.uuid4())
        message["time"] = int(time.time())

        await self.websocket.send(json.dumps(message))
        print(f"\n已发送消息: {message}")

    async def build_group_message(self, message_id: int):
        if random.randint(0, 1):
            # 发送纯文本消息
            # 前缀为~或!的才能被服务器接收,其中~为发送消息，!为发送指令
            return random.choice(["这条消息不应该被转发到游戏内","~这条消息将会被转发至游戏1", "~这条消息将会被转发至游戏2", "!time set day", "!time set night","!list"])
        else:
            # 发送消息段
            message1 = [
                {
                    "type": "text",
                    "data": {
                        "text": "~[第一部分]"
                    }
                },
                {
                    "type": "image",
                    "data": {
                        "file": "123.jpg"
                    }
                },
                {
                    "type": "text",
                    "data": {
                        "text": "图片之后的部分，表情："
                    }
                },
                {
                    "type": "face",
                    "data": {
                        "id": "123"
                    }
                }
            ]
            message2 = [{
                "type": "reply",
                "data": {
                    "id": message_id
                }
            }, {
                "type": "text",
                "data": {
                    "text": "这条消息在群内被用户回复"
                }
            }]
            return message2 if random.random() > 0.8 else message1

    async def send_group_message(self):
        message_id = random.randint(100000, 999999),
        content = await self.build_group_message(message_id[0])
        """发送模拟群聊消息"""
        message = {
            "time": int(time.time()),
            "self_id": self.bot_id,
            "post_type": "message",
            "message_type": "group",
            "sub_type": "normal",
            "message_id": message_id,
            "group_id": self.group_id,
            "user_id": self.admin_id,
            "anonymous": None,
            "message": content,
            "raw_message": str(content),
            "font": 0,
            "sender": get_sender(),
        }
        await self.send_message(message)

    async def handle_server_request(self, data: Dict[str, Any]):
        """处理来自服务器的请求"""
        print(f"\n\033[0;34m收到服务器请求:\033[0m {data}")

        if data.get("action") == "send_group_msg":
            # 处理发送群消息的请求
            group_id = data["params"]["group_id"]
            message = data["params"]["message"]
            print(f"\n服务器请求发送群消息: 群 {group_id} - {message}")

            # 可以在这里构造响应
            response = {
                "status": "ok",
                "retcode": 0,
                "data": {"message_id": self.message_id},
                "echo": data.get("echo", "")
            }
            self.message_id += 1
            await self.send_message(response)

    async def listen(self):
        """监听服务器消息"""
        try:
            async for message in self.websocket:
                try:
                    data = json.loads(message)
                    if "action" in data:
                        # 这是API调用请求
                        await self.handle_server_request(data)
                    else:
                        # 这是事件或响应
                        print(f"\n\033[0;34m收到服务器消息: \033[0m{data}")
                except json.JSONDecodeError:
                    print(f"\n\033[0;31m收到非JSON消息:\033[0m {message}")
        except websockets.exceptions.ConnectionClosed:
            print("连接已关闭")

    async def run(self):
        """运行测试程序"""
        await self.connect()

        # 启动监听任务
        listen_task = asyncio.create_task(self.listen())

        try:
            # 每隔5秒发送一条测试消息
            counter = 1
            while True:
                await self.send_group_message()
                counter += 1
                await asyncio.sleep(5)
        except KeyboardInterrupt:
            print("正在关闭连接...")
            listen_task.cancel()
            await self.websocket.close()
            print("程序已退出")


async def main():
    tester = OneBotV11Tester()
    await tester.run()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("程序已终止")
