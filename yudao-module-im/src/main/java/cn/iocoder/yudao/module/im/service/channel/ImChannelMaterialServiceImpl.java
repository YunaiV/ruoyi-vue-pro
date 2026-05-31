package cn.iocoder.yudao.module.im.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.dal.mysql.channel.ImChannelMaterialMapper;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImChannelMessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.IM_CHANNEL_MATERIAL_NOT_EXISTS;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.IM_CHANNEL_MATERIAL_USED;

/**
 * IM 频道素材 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImChannelMaterialServiceImpl implements ImChannelMaterialService {

    @Resource
    private ImChannelMaterialMapper channelMaterialMapper;
    @Resource
    private ImChannelService channelService;
    @Resource
    private ImChannelMessageMapper channelMessageMapper;

    // ==================== 用户端 ====================

    @Override
    public ImChannelMaterialDO validateMaterialExists(Long id) {
        ImChannelMaterialDO material = channelMaterialMapper.selectById(id);
        if (material == null) {
            throw exception(IM_CHANNEL_MATERIAL_NOT_EXISTS);
        }
        return material;
    }

    @Override
    public List<ImChannelMaterialDO> getMaterialList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return channelMaterialMapper.selectByIds(ids);
    }

    @Override
    public Long getMaterialCountByChannelId(Long channelId) {
        return channelMaterialMapper.selectCountByChannelId(channelId);
    }

    // ==================== 管理后台 ====================

    @Override
    public List<ImChannelMaterialDO> getMaterialListByChannelId(Long channelId) {
        return channelMaterialMapper.selectListByChannelId(channelId);
    }

    @Override
    public PageResult<ImChannelMaterialDO> getMaterialPage(ImChannelMaterialPageReqVO reqVO) {
        return channelMaterialMapper.selectPage(reqVO);
    }

    @Override
    public ImChannelMaterialDO getMaterial(Long id) {
        return channelMaterialMapper.selectById(id);
    }

    @Override
    public Long createMaterial(ImChannelMaterialSaveReqVO reqVO) {
        // 1. 校验所属频道存在
        channelService.validateChannelExists(reqVO.getChannelId());

        // 2. 插入素材
        ImChannelMaterialDO material = BeanUtils.toBean(reqVO, ImChannelMaterialDO.class);
        channelMaterialMapper.insert(material);
        return material.getId();
    }

    @Override
    public void updateMaterial(ImChannelMaterialSaveReqVO reqVO) {
        // 1.1 校验存在
        validateMaterialExists(reqVO.getId());
        // 1.2 校验所属频道存在
        channelService.validateChannelExists(reqVO.getChannelId());

        // 2. 更新素材
        ImChannelMaterialDO updateObj = BeanUtils.toBean(reqVO, ImChannelMaterialDO.class);
        channelMaterialMapper.updateById(updateObj);
    }

    @Override
    public void deleteMaterial(Long id) {
        validateMaterialExists(id);
        // 防止删除素材导致历史 channel_message 反查不到内容
        if (channelMessageMapper.selectCountByMaterialId(id) > 0) {
            throw exception(IM_CHANNEL_MATERIAL_USED);
        }
        channelMaterialMapper.deleteById(id);
    }

}
