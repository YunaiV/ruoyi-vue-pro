package cn.iocoder.yudao.module.im.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel.ImChannelPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel.ImChannelSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IM 频道 Service 接口
 *
 * @author 芋道源码
 */
public interface ImChannelService {

    // ==================== 用户端 ====================

    /**
     * 按状态查询频道列表，按 sort 升序
     *
     * @param status 状态；对应 CommonStatusEnum
     * @return 频道列表
     */
    List<ImChannelDO> getChannelListByStatus(Integer status);

    /**
     * 按编号批量查询频道
     *
     * @param ids 频道编号列表
     * @return 频道列表
     */
    List<ImChannelDO> getChannelList(Collection<Long> ids);

    /**
     * 按编号批量查询频道 Map
     *
     * @param ids 频道编号列表
     * @return id -> 频道 Map
     */
    default Map<Long, ImChannelDO> getChannelMap(Collection<Long> ids) {
        return convertMap(getChannelList(ids), ImChannelDO::getId);
    }

    /**
     * 校验频道存在
     *
     * @param id 频道编号
     * @return 频道 DO
     */
    @SuppressWarnings("UnusedReturnValue")
    ImChannelDO validateChannelExists(Long id);

    // ==================== 管理后台 ====================

    /**
     * 分页查询频道
     *
     * @param reqVO 分页查询条件
     * @return 频道分页
     */
    PageResult<ImChannelDO> getChannelPage(ImChannelPageReqVO reqVO);

    /**
     * 获取频道详情
     *
     * @param id 频道编号
     * @return 频道 DO
     */
    ImChannelDO getChannel(Long id);

    /**
     * 新增频道
     *
     * @param reqVO 新增请求
     * @return 新增频道编号
     */
    Long createChannel(@Valid ImChannelSaveReqVO reqVO);

    /**
     * 修改频道
     *
     * @param reqVO 修改请求
     */
    void updateChannel(@Valid ImChannelSaveReqVO reqVO);

    /**
     * 删除频道；频道下有素材或消息时拒绝
     *
     * @param id 频道编号
     */
    void deleteChannel(Long id);

}
