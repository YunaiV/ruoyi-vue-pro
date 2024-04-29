
# Midjourney 消息体解析

```text

{
  "op": 0,
  "s": 346,
  "t": "MESSAGE_CREATE",  // 消息类型
  "d": {
    "id": "1234498661476601907", // 唯一id
    "mention_everyone": false,
    "pinned": false,
    "channel_id": "1234380679576424448", // 频道id用来只区分自己频道
    "flags": 64,    // flags 未知
    "type": 20,     // 初步理解是用于区分消息类型
    "mention_roles": [],
    "nonce": "18",   // 重点，我们传过去的唯一id，用于区分不同的绘画
    "application_id": "936929561302675456",
    "edited_timestamp": null,
    "content": "",   // 消息内容
    "tts": false,
    "webhook_id": "936929561302675456",
    "mentions": [],
    "components": [],   // U1、U2等操作
    "attachments": [],  // 生成的图片附件
    "embeds": []        // 重点异常、提示、错误 都是在这个里面，这个里面有信息这个任务就不正常
  }
}
```

说明：
- t: 是消息类型(处理 create、和update)
  - MESSAGE_CREATE：会增加一条新的信息出来
  - MESSAGE_UPDATE：不会增加新的信息，在原来的信息上更新展示，比如进度条 10% 30%...
  - MESSAGE_DELETE 没啥用
- components：可以理解为可以操作的按钮
  - 情况1：正常只有图片完成才有对于操作
  - 情况2：异常情况可以 submit 当前任务
- attachments: 附件，在图片生成到50%左右就会开始有模糊的图片，这里面就会有照片的地址
- content：正常的消息内容在这里面，一般会是一串字符串，一般有 prompt、版本、进度、状态/生成类型
  - 内容示例：**海贼王，在赶海! --v 6.0** - <@975372485971312700> (0%) (relaxed)
  - 内容示例：**海贼王，在赶海! --v 6.0** - <@975372485971312700> (35%) (relaxed)
- embeds: <重点> 只有错误、警告、封号、异常里面才有信息，可以优先判断这里面有没有内容，就知道是否正常!

