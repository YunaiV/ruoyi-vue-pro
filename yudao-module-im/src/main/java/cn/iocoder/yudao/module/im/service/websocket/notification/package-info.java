/**
 * IM WebSocket 在线通知对象。
 *
 * <p>
 * 本包只放 WebSocket 外层通知对象，固定使用 im-notification 作为推送 type。
 * 具体业务 payload 放在子包中：
 * <ul>
 *     <li>{@code message}：私聊、群聊、频道消息通知，以及消息内容结构；</li>
 *     <li>{@code friend}：好友关系和好友申请通知；</li>
 *     <li>{@code group}：群资料、群成员、加群申请通知；</li>
 *     <li>{@code rtc}：实时通话通知。</li>
 * </ul>
 */
package cn.iocoder.yudao.module.im.service.websocket.notification;
