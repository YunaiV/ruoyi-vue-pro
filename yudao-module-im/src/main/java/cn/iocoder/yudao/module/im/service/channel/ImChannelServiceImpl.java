package cn.iocoder.yudao.module.im.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel.ImChannelPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel.ImChannelSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelDO;
import cn.iocoder.yudao.module.im.dal.mysql.channel.ImChannelMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * IM 频道 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImChannelServiceImpl implements ImChannelService {

    @Resource
    private ImChannelMapper channelMapper;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private ImChannelMaterialService channelMaterialService;

    // ==================== 用户端 ====================

    @Override
    public List<ImChannelDO> getChannelListByStatus(Integer status) {
        return channelMapper.selectListByStatusOrderBySort(status);
    }

    @Override
    public List<ImChannelDO> getChannelList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return channelMapper.selectByIds(ids);
    }

    @Override
    public ImChannelDO validateChannelExists(Long id) {
        ImChannelDO channel = channelMapper.selectById(id);
        if (channel == null) {
            throw exception(IM_CHANNEL_NOT_EXISTS);
        }
        return channel;
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImChannelDO> getChannelPage(ImChannelPageReqVO reqVO) {
        return channelMapper.selectPage(reqVO);
    }

    @Override
    public ImChannelDO getChannel(Long id) {
        return channelMapper.selectById(id);
    }

    @Override
    public Long createChannel(ImChannelSaveReqVO reqVO) {
        // 校验 code 唯一
        validateCodeUnique(null, reqVO.getCode());

        // 插入
        ImChannelDO channel = BeanUtils.toBean(reqVO, ImChannelDO.class);
        channelMapper.insert(channel);
        return channel.getId();
    }

    @Override
    public void updateChannel(ImChannelSaveReqVO reqVO) {
        // 1.1 校验存在
        validateChannelExists(reqVO.getId());
        // 1.2 校验 code 唯一
        validateCodeUnique(reqVO.getId(), reqVO.getCode());

        // 2. 更新
        ImChannelDO updateObj = BeanUtils.toBean(reqVO, ImChannelDO.class);
        channelMapper.updateById(updateObj);
    }

    @Override
    public void deleteChannel(Long id) {
        // 1.1 校验存在
        validateChannelExists(id);
        // 1.2 防止误删频道导致历史素材 / 消息回查不到归属
        if (channelMaterialService.getMaterialCountByChannelId(id) > 0) {
            throw exception(IM_CHANNEL_HAS_MATERIAL);
        }

        // 2. 删除频道
        channelMapper.deleteById(id);
    }

    private void validateCodeUnique(Long id, String code) {
        ImChannelDO exist = channelMapper.selectByCode(code);
        if (exist == null) {
            return;
        }
        if (id == null || ObjUtil.notEqual(exist.getId(), id)) {
            throw exception(IM_CHANNEL_CODE_DUPLICATED, code);
        }
    }

}
