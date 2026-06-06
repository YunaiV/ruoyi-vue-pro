package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 频道消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImChannelMessageMapper extends BaseMapperX<ImChannelMessageDO> {

    /**
     * 拉取指定用户应收的频道消息
     * <p>
     * 命中条件：id 大于游标 + (receiver_user_ids 为空表示全员 OR 逗号分隔列表里包含当前 userId)
     *
     * @param userId 当前用户编号
     * @param minId  游标；返回大于此值的消息
     * @param size   返回条数
     * @return 频道消息列表；按 id 升序
     */
    default List<ImChannelMessageDO> selectListByUserAndMinId(Long userId, Long minId, Integer size) {
        return selectList(new LambdaQueryWrapperX<ImChannelMessageDO>()
                .gt(ImChannelMessageDO::getId, minId)
                .and(w -> w.isNull(ImChannelMessageDO::getReceiverUserIds)
                        .or().eq(ImChannelMessageDO::getReceiverUserIds, "")
                        .or().apply(MyBatisUtils.findInSet("receiver_user_ids"), userId))
                .orderByAsc(ImChannelMessageDO::getId)
                .last("LIMIT " + size));
    }

    default Long selectCountByMaterialId(Long materialId) {
        return selectCount(ImChannelMessageDO::getMaterialId, materialId);
    }

    default PageResult<ImChannelMessageDO> selectPage(ImChannelMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImChannelMessageDO>()
                .eqIfPresent(ImChannelMessageDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(ImChannelMessageDO::getMaterialId, reqVO.getMaterialId())
                .betweenIfPresent(ImChannelMessageDO::getSendTime, reqVO.getSendTime())
                .orderByDesc(ImChannelMessageDO::getId));
    }

}
