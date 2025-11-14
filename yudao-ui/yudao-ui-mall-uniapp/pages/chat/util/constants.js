export const KeFuMessageContentTypeEnum = {
  TEXT: 1, // 文本消息
  IMAGE: 2, // 图片消息
  VOICE: 3, // 语音消息
  VIDEO: 4, // 视频消息
  SYSTEM: 5, // 系统消息
  // ========== 商城特殊消息 ==========
  PRODUCT: 10,//  商品消息
  ORDER: 11,//  订单消息"
};
export const UserTypeEnum = {
  MEMBER: 1, // 会员 面向 c 端，普通用户
  ADMIN: 2, // 管理员 面向 b 端，管理后台
};
// Promotion 的 WebSocket 消息类型枚举类
export const WebSocketMessageTypeConstants = {
  KEFU_MESSAGE_TYPE: 'kefu_message_type', // 客服消息类型
  KEFU_MESSAGE_ADMIN_READ: 'kefu_message_read_status_change' // 客服消息管理员已读
}
