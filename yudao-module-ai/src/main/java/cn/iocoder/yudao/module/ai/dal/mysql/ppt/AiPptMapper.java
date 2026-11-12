package cn.iocoder.yudao.module.ai.dal.mysql.ppt;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ppt.AiPptDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI PPT Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiPptMapper extends BaseMapperX<AiPptDO> {

    default PageResult<AiPptDO> selectPage(AiPptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiPptDO>()
                .eqIfPresent(AiPptDO::getUserId, reqVO.getUserId())
                .likeIfPresent(AiPptDO::getTitle, reqVO.getTitle())
                .eqIfPresent(AiPptDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AiPptDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiPptDO::getId));
    }

    default PageResult<AiPptDO> selectPageByUserId(Long userId, PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiPptDO>()
                .eq(AiPptDO::getUserId, userId)
                .orderByDesc(AiPptDO::getId));
    }

    default List<AiPptDO> selectListByStatusAndUpdateTimeLt(Integer status, LocalDateTime updateTime) {
        return selectList(new LambdaQueryWrapperX<AiPptDO>()
                .eq(AiPptDO::getStatus, status)
                .lt(AiPptDO::getUpdateTime, updateTime));
    }

    /**
     * 按状态、轮次条件更新；失败原因为空时清空
     *
     * @param whereUpdateTimeLt 更新时间早于该时间才更新，允许空
     */
    default int updateByIdAndStatusAndAttempt(AiPptDO updateObj, Integer whereStatus, Integer whereAttempt,
                                              LocalDateTime whereUpdateTimeLt) {
        return update(updateObj, new LambdaUpdateWrapper<AiPptDO>()
                .eq(AiPptDO::getId, updateObj.getId())
                .eq(AiPptDO::getStatus, whereStatus)
                .eq(AiPptDO::getAttempt, whereAttempt)
                .lt(whereUpdateTimeLt != null, AiPptDO::getUpdateTime, whereUpdateTimeLt)
                .set(updateObj.getErrorMessage() == null, AiPptDO::getErrorMessage, null));
    }

    default int deleteByIdAndStatusNe(Long id, Integer status) {
        return delete(new LambdaQueryWrapperX<AiPptDO>()
                .eq(AiPptDO::getId, id)
                .ne(AiPptDO::getStatus, status));
    }

}
