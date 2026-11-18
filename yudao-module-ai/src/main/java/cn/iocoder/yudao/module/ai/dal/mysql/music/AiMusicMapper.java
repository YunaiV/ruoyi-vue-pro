package cn.iocoder.yudao.module.ai.dal.mysql.music;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicPublicPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 音乐 Mapper
 *
 * @author xiaoxin
 */
@Mapper
public interface AiMusicMapper extends BaseMapperX<AiMusicDO> {

    default PageResult<AiMusicDO> selectPage(AiMusicPageReqVO reqVO) {
        String keyword = reqVO.getKeyword();
        return selectPage(reqVO, new LambdaQueryWrapperX<AiMusicDO>()
                .eqIfPresent(AiMusicDO::getUserId, reqVO.getUserId())
                .likeIfPresent(AiMusicDO::getTitle, reqVO.getTitle())
                .eqIfPresent(AiMusicDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiMusicDO::getGenerateMode, reqVO.getGenerateMode())
                .eqIfPresent(AiMusicDO::getPublicStatus, reqVO.getPublicStatus())
                .and(StrUtil.isNotBlank(keyword), w -> w.like(AiMusicDO::getPrompt, keyword)
                        .or().like(AiMusicDO::getLyric, keyword))
                .betweenIfPresent(AiMusicDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiMusicDO::getId));
    }

    default PageResult<AiMusicDO> selectPageByUserId(Long userId, PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiMusicDO>()
                .eq(AiMusicDO::getUserId, userId)
                .orderByDesc(AiMusicDO::getId));
    }

    // DONE @AI：By，不要 Public 放前面
    default PageResult<AiMusicDO> selectPageByPublic(AiMusicPublicPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiMusicDO>()
                .eq(AiMusicDO::getPublicStatus, true)
                .eq(AiMusicDO::getStatus, AiMusicStatusEnum.SUCCESS.getStatus())
                .likeIfPresent(AiMusicDO::getTitle, reqVO.getTitle())
                .orderByDesc(AiMusicDO::getId));
    }

    // DONE @AI：status 是传入的
    /**
     * 查询待查询中转的占位行：指定状态、已有 task_id、还未拆分（clip_id 为空）
     */
    default List<AiMusicDO> selectListForFetch(Integer status, int limit) {
        return selectListLimitN(new LambdaQueryWrapperX<AiMusicDO>()
                .eq(AiMusicDO::getStatus, status)
                .isNotNull(AiMusicDO::getTaskId)
                .isNull(AiMusicDO::getClipId)
                .orderByAsc(AiMusicDO::getId), limit);
    }

    default List<AiMusicDO> selectListByStatus(Integer status, int limit) {
        return selectListLimitN(new LambdaQueryWrapperX<AiMusicDO>()
                .eq(AiMusicDO::getStatus, status)
                .orderByAsc(AiMusicDO::getId), limit);
    }

    default List<AiMusicDO> selectListByStatusAndCreateTimeLt(Collection<Integer> statuses, LocalDateTime createTime) {
        return selectList(new LambdaQueryWrapperX<AiMusicDO>()
                .in(AiMusicDO::getStatus, statuses)
                .lt(AiMusicDO::getCreateTime, createTime));
    }

    default int updateByIdAndStatus(Long id, Integer whereStatus, AiMusicDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiMusicDO>()
                .eq(AiMusicDO::getId, id)
                .eq(AiMusicDO::getStatus, whereStatus));
    }

    // DONE @AI：status 是传入的
    /**
     * 条件更新占位行为第 1 首：只有仍为指定状态且未拆分（clip_id 为空）时才更新
     *
     * @return 影响行数；为 0 表示已被其它执行者拆分
     */
    default int updateByIdAndUnsplit(Long id, Integer whereStatus, AiMusicDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiMusicDO>()
                .eq(AiMusicDO::getId, id)
                .eq(AiMusicDO::getStatus, whereStatus)
                .isNull(AiMusicDO::getClipId));
    }

    // DONE @AI：updateForSave，对齐别的类似方法
    default int updateForSave(Long id, Integer whereStatus, AiMusicDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiMusicDO>()
                .eq(AiMusicDO::getId, id)
                .eq(AiMusicDO::getStatus, whereStatus)
                .set(updateObj.getCoverUrl() == null, AiMusicDO::getCoverUrl, null));
    }

    default void updatePlayCountIncr(Long id) {
        // DONE @AI：incr 不用 +1 这种的（setIncrBy）
        update(null, new LambdaUpdateWrapper<AiMusicDO>()
                .eq(AiMusicDO::getId, id)
                .setIncrBy(AiMusicDO::getPlayCount, 1));
    }

}
