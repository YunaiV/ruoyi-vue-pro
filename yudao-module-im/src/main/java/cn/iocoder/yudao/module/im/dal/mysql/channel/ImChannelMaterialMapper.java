package cn.iocoder.yudao.module.im.dal.mysql.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IM 频道素材 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImChannelMaterialMapper extends BaseMapperX<ImChannelMaterialDO> {

    default Long selectCountByChannelId(Long channelId) {
        return selectCount(ImChannelMaterialDO::getChannelId, channelId);
    }

    default PageResult<ImChannelMaterialDO> selectPage(ImChannelMaterialPageReqVO reqVO) {
        LambdaQueryWrapperX<ImChannelMaterialDO> wrapper = new LambdaQueryWrapperX<ImChannelMaterialDO>()
                .eqIfPresent(ImChannelMaterialDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(ImChannelMaterialDO::getType, reqVO.getType())
                .likeIfPresent(ImChannelMaterialDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(ImChannelMaterialDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImChannelMaterialDO::getId);
        // TODO @AI：不用排除；列表问题不大的；
        // 列表查询排除富文本 content 字段，避免 mediumtext 拖垮分页响应
        wrapper.select(ImChannelMaterialDO::getId, ImChannelMaterialDO::getChannelId,
                ImChannelMaterialDO::getType, ImChannelMaterialDO::getTitle,
                ImChannelMaterialDO::getCoverUrl, ImChannelMaterialDO::getSummary,
                ImChannelMaterialDO::getUrl, ImChannelMaterialDO::getCreator,
                ImChannelMaterialDO::getCreateTime, ImChannelMaterialDO::getUpdater,
                ImChannelMaterialDO::getUpdateTime, ImChannelMaterialDO::getDeleted);
        return selectPage(reqVO, wrapper);
    }

}
