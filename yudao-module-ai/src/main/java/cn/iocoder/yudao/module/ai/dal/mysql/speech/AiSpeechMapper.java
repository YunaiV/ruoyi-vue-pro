package cn.iocoder.yudao.module.ai.dal.mysql.speech;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.speech.AiSpeechDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 语音 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiSpeechMapper extends BaseMapperX<AiSpeechDO> {

    default PageResult<AiSpeechDO> selectPage(AiSpeechPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiSpeechDO>()
                .eqIfPresent(AiSpeechDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiSpeechDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(AiSpeechDO::getStatus, reqVO.getStatus())
                .likeIfPresent(AiSpeechDO::getInputText, reqVO.getInputText())
                .betweenIfPresent(AiSpeechDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiSpeechDO::getId));
    }

    default PageResult<AiSpeechDO> selectPageByUserId(Long userId, PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiSpeechDO>()
                .eq(AiSpeechDO::getUserId, userId)
                .orderByDesc(AiSpeechDO::getId));
    }

    /**
     * 查询已有任务编号的记录：指定状态，用于查询异步任务的结果
     */
    default List<AiSpeechDO> selectListByStatusAndTaskIdNotNull(Integer status, int limit) {
        return selectListLimitN(new LambdaQueryWrapperX<AiSpeechDO>()
                .eq(AiSpeechDO::getStatus, status)
                .isNotNull(AiSpeechDO::getTaskId)
                .orderByAsc(AiSpeechDO::getId), limit);
    }

    default List<AiSpeechDO> selectListByStatusAndCreateTimeLt(Integer status, LocalDateTime createTime) {
        return selectList(new LambdaQueryWrapperX<AiSpeechDO>()
                .eq(AiSpeechDO::getStatus, status)
                .lt(AiSpeechDO::getCreateTime, createTime));
    }

    /**
     * 条件更新：仍为 whereStatus 时才更新，避免迟到的结果覆盖终态
     *
     * @return 影响行数；为 0 表示状态已被其它执行者改变
     */
    default int updateByIdAndStatus(Long id, Integer whereStatus, AiSpeechDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiSpeechDO>()
                .eq(AiSpeechDO::getId, id)
                .eq(AiSpeechDO::getStatus, whereStatus));
    }

}
