package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.AiDramaProjectMyPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.AiDramaProjectPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.AiDramaProjectPublicPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaProjectDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaDurationTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 短剧项目 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaProjectMapper extends BaseMapperX<AiDramaProjectDO> {

    default PageResult<AiDramaProjectDO> selectPage(AiDramaProjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiDramaProjectDO>()
                .eqIfPresent(AiDramaProjectDO::getUserId, reqVO.getUserId())
                .likeIfPresent(AiDramaProjectDO::getName, reqVO.getName())
                .eqIfPresent(AiDramaProjectDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiDramaProjectDO::getComposeStatus, reqVO.getComposeStatus())
                .eqIfPresent(AiDramaProjectDO::getPublicStatus, reqVO.getPublicStatus())
                .eqIfPresent(AiDramaProjectDO::getAspectRatio, reqVO.getAspectRatio())
                .betweenIfPresent(AiDramaProjectDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiDramaProjectDO::getId));
    }

    default PageResult<AiDramaProjectDO> selectPageByUserId(Long userId, AiDramaProjectMyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiDramaProjectDO>()
                .eq(AiDramaProjectDO::getUserId, userId)
                .likeIfPresent(AiDramaProjectDO::getName, reqVO.getName())
                .eqIfPresent(AiDramaProjectDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiDramaProjectDO::getComposeStatus, reqVO.getComposeStatus())
                .eqIfPresent(AiDramaProjectDO::getPublicStatus, reqVO.getPublicStatus())
                .orderByDesc(AiDramaProjectDO::getUpdateTime)
                .orderByDesc(AiDramaProjectDO::getId));
    }

    /**
     * 公开广场：已公开且成片已完成的项目；画幅按成片实际使用的；时长按 {@link AiDramaDurationTypeEnum} 分档
     */
    default PageResult<AiDramaProjectDO> selectPageByPublic(AiDramaProjectPublicPageReqVO reqVO) {
        LambdaQueryWrapperX<AiDramaProjectDO> query = new LambdaQueryWrapperX<AiDramaProjectDO>()
                .eq(AiDramaProjectDO::getPublicStatus, true)
                .eq(AiDramaProjectDO::getComposeStatus, AiDramaStatusEnum.SUCCESS.getStatus())
                .eqIfPresent(AiDramaProjectDO::getComposeAspectRatio, reqVO.getAspectRatio());
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            query.and(w -> w.like(AiDramaProjectDO::getName, reqVO.getKeyword())
                    .or().like(AiDramaProjectDO::getStyle, reqVO.getKeyword()));
        }
        AiDramaDurationTypeEnum durationType = AiDramaDurationTypeEnum.of(reqVO.getDurationType());
        if (durationType != null) {
            query.geIfPresent(AiDramaProjectDO::getComposeDuration, durationType.getMinDuration())
                    .ltIfPresent(AiDramaProjectDO::getComposeDuration, durationType.getMaxDuration());
        }
        return selectPage(reqVO, query.orderByDesc(AiDramaProjectDO::getUpdateTime)
                .orderByDesc(AiDramaProjectDO::getId));
    }

    default List<AiDramaProjectDO> selectListByUserId(Long userId) {
        return selectList(AiDramaProjectDO::getUserId, userId);
    }

    /**
     * 生成任务：有成片状态的项目
     */
    default PageResult<AiDramaProjectDO> selectTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                        Collection<Long> projectIds) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiDramaProjectDO>()
                .inIfPresent(AiDramaProjectDO::getId, projectIds)
                .eqIfPresent(AiDramaProjectDO::getComposeStatus, reqVO.getStatus())
                .betweenIfPresent(AiDramaProjectDO::getUpdateTime, reqVO.getUpdateTime())
                .isNotNull(AiDramaProjectDO::getComposeStatus)
                .orderByDesc(AiDramaProjectDO::getUpdateTime)
                .orderByDesc(AiDramaProjectDO::getId));
    }

    default List<AiDramaProjectDO> selectListByComposeStatusAndUpdateTimeLt(Integer composeStatus, LocalDateTime updateTime) {
        return selectList(new LambdaQueryWrapperX<AiDramaProjectDO>()
                .eq(AiDramaProjectDO::getComposeStatus, composeStatus)
                .lt(AiDramaProjectDO::getUpdateTime, updateTime));
    }

    /**
     * 按合成任务的条件更新：只更新状态未变、且仍是同一合成任务的项目（被标记失败后又重新合成时，任务编号已变）；任务编号为空时匹配为空的
     */
    // DONE @AI：是不是按照 id + status 就够了？没必要？whereComposeTaskId（评估后保留：管理员标记失败后再重新合成，状态会回到合成中，只有任务编号能区分旧合成，旧合成上传完成后不能把新一轮写成成功，见 AiDramaProjectServiceImplTest#testExecuteCompose_resubmittedDuringUpload；任务编号为空时按为空匹配，不放宽为只按状态）
    default int updateByIdAndComposeStatusAndTaskId(Long id, Integer whereComposeStatus, String whereComposeTaskId, AiDramaProjectDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaProjectDO>()
                .eq(AiDramaProjectDO::getId, id)
                .eq(AiDramaProjectDO::getComposeStatus, whereComposeStatus)
                .eq(whereComposeTaskId != null, AiDramaProjectDO::getComposeTaskId, whereComposeTaskId)
                .isNull(whereComposeTaskId == null, AiDramaProjectDO::getComposeTaskId));
    }

}
