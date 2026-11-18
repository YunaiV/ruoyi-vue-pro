package cn.iocoder.yudao.module.ai.dal.mysql.video;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.video.vo.AiVideoPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.video.vo.AiVideoPublicPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 视频 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiVideoMapper extends BaseMapperX<AiVideoDO> {

    default PageResult<AiVideoDO> selectPage(AiVideoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiVideoDO>()
                .eqIfPresent(AiVideoDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiVideoDO::getType, reqVO.getType())
                .eqIfPresent(AiVideoDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(AiVideoDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiVideoDO::getPublicStatus, reqVO.getPublicStatus())
                .likeIfPresent(AiVideoDO::getPrompt, reqVO.getPrompt())
                .betweenIfPresent(AiVideoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiVideoDO::getId));
    }

    default PageResult<AiVideoDO> selectPageByUserId(Long userId, Integer type, PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiVideoDO>()
                .eq(AiVideoDO::getUserId, userId)
                .eqIfPresent(AiVideoDO::getType, type)
                .orderByDesc(AiVideoDO::getId));
    }

    default PageResult<AiVideoDO> selectPageByPublic(AiVideoPublicPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiVideoDO>()
                .eq(AiVideoDO::getPublicStatus, true)
                .eq(AiVideoDO::getStatus, AiVideoStatusEnum.SUCCESS.getStatus())
                .eqIfPresent(AiVideoDO::getType, reqVO.getType())
                .likeIfPresent(AiVideoDO::getPrompt, reqVO.getPrompt())
                .orderByDesc(AiVideoDO::getId));
    }

    default List<AiVideoDO> selectListByStatus(Collection<Integer> statuses, int limit) {
        return selectListLimitN(new LambdaQueryWrapperX<AiVideoDO>()
                .in(AiVideoDO::getStatus, statuses)
                .orderByAsc(AiVideoDO::getId), limit);
    }

    default List<AiVideoDO> selectListByStatusAndCreateTimeLt(Collection<Integer> statuses, LocalDateTime createTime) {
        return selectList(new LambdaQueryWrapperX<AiVideoDO>()
                .in(AiVideoDO::getStatus, statuses)
                .lt(AiVideoDO::getCreateTime, createTime));
    }

    default int updateByIdAndStatus(Long id, Integer whereStatus, AiVideoDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiVideoDO>()
                .eq(AiVideoDO::getId, id)
                .eq(AiVideoDO::getStatus, whereStatus));
    }

}
