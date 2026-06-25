package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IM 频道消息 Service 接口
 *
 * @author 芋道源码
 */
public interface ImChannelMessageService {

    // ==================== 用户端 ====================

    /**
     * 拉取当前用户应收的频道消息（离线增量）
     *
     * @param userId 当前用户编号
     * @param minId  游标；返回大于此值的消息
     * @param size   返回条数
     * @return 频道消息列表；按 id 升序
     */
    List<ImChannelMessageDO> pullChannelMessageList(Long userId, Long minId, Integer size);

    /**
     * 上报频道消息已读位置；同步推 READ 事件给自己多端
     *
     * @param userId    当前用户编号
     * @param channelId 频道编号
     * @param messageId 已读到的最大消息编号
     */
    void readChannelMessages(Long userId, Long channelId, Long messageId);

    /**
     * 批量查询用户在多个频道下的已读游标
     *
     * @param userId     当前用户编号
     * @param channelIds 频道编号集合
     * @return channelId → 已读到的最大消息编号（未读到的频道不出现在返回 map 中）
     */
    Map<Long, Long> getChannelReadMaxMessageIdMap(Long userId, Collection<Long> channelIds);

    // ==================== 管理后台 ====================

    /**
     * 立即推送频道消息
     *
     * @param reqVO 推送请求
     * @return 消息编号
     */
    Long sendMessage(@Valid ImChannelMessageSendReqVO reqVO);

    /**
     * 分页查询消息
     *
     * @param reqVO 分页查询条件
     * @return 消息分页
     */
    PageResult<ImChannelMessageDO> getMessagePage(ImChannelMessagePageReqVO reqVO);

    /**
     * 删除消息
     *
     * @param id 消息编号
     */
    void deleteMessage(Long id);

}
