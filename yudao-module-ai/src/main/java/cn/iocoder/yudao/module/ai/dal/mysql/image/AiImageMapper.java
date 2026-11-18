package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImagePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImagePublicPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 绘图 Mapper
 *
 * @author fansili
 */
// TODO DONE @AI：mapper 不写 test（已删除 AiImageMapperTest 与 H2 建表脚本）
@Mapper
public interface AiImageMapper extends BaseMapperX<AiImageDO> {

    /**
     * 按上游任务编号查询（含已结束的记录）：任务编号不唯一（中转返回 21 时会复用；不同中转也可能相同）
     */
    default List<AiImageDO> selectListByTaskId(String taskId) {
        return selectList(AiImageDO::getTaskId, taskId);
    }

    default PageResult<AiImageDO> selectPage(AiImagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiImageDO>()
                .eqIfPresent(AiImageDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiImageDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(AiImageDO::getMode, reqVO.getMode())
                .likeIfPresent(AiImageDO::getPrompt, reqVO.getPrompt())
                .eqIfPresent(AiImageDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiImageDO::getPublicStatus, reqVO.getPublicStatus())
                .betweenIfPresent(AiImageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiImageDO::getId));
    }

    default PageResult<AiImageDO> selectPageMy(Long userId, PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiImageDO>()
                .eq(AiImageDO::getUserId, userId)
                .orderByDesc(AiImageDO::getId));
    }

    default PageResult<AiImageDO> selectPage(AiImagePublicPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AiImageDO>()
                .eq(AiImageDO::getPublicStatus, Boolean.TRUE)
                .eq(AiImageDO::getStatus, AiImageStatusEnum.SUCCESS.getStatus()) // 只展示已完成的绘图
                .orderByDesc(AiImageDO::getId));
    }

    default List<AiImageDO> selectListByStatusAndPlatform(Integer status, String platform) {
        return selectList(AiImageDO::getStatus, status,
                AiImageDO::getPlatform, platform);
    }

    default List<AiImageDO> selectListByStatusAndCreateTimeLt(Integer status, LocalDateTime createTime) {
        return selectList(new LambdaQueryWrapperX<AiImageDO>()
                .eq(AiImageDO::getStatus, status)
                .lt(AiImageDO::getCreateTime, createTime));
    }

    default List<AiImageDO> selectListByParentId(Long parentId) {
        return selectList(new LambdaQueryWrapperX<AiImageDO>()
                .eq(AiImageDO::getParentId, parentId)
                .orderByAsc(AiImageDO::getId));
    }

    /**
     * 带状态条件更新：例如说只更新进行中的记录，避免超时、完成后的迟到回调覆盖
     *
     * @return 更新条数
     */
    default int updateByIdAndStatus(Long id, Integer whereStatus, AiImageDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiImageDO>()
                .eq(AiImageDO::getId, id)
                .eq(AiImageDO::getStatus, whereStatus));
    }

}
