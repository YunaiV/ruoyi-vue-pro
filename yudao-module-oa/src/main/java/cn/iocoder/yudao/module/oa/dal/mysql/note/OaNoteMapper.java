package cn.iocoder.yudao.module.oa.dal.mysql.note;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNotePageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 笔记 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaNoteMapper extends BaseMapperX<OaNoteDO> {

    default PageResult<OaNoteDO> selectMyPage(OaNotePageReqVO pageReqVO, Long userId, Collection<Long> ids) {
        return selectPage(pageReqVO, buildPageQuery(pageReqVO)
                .eq(OaNoteDO::getCreator, userId.toString())
                .in(OaNoteDO::getId, ids));
    }

    default PageResult<OaNoteDO> selectReceivedPage(OaNotePageReqVO pageReqVO, Collection<Long> ids, Long userId) {
        return selectPage(pageReqVO, buildPageQuery(pageReqVO).in(OaNoteDO::getId, ids)
                .ne(OaNoteDO::getCreator, userId.toString()));
    }

    default LambdaQueryWrapperX<OaNoteDO> buildPageQuery(OaNotePageReqVO pageReqVO) {
        return new LambdaQueryWrapperX<OaNoteDO>()
                .likeIfPresent(OaNoteDO::getTitle, pageReqVO.getTitle())
                .eqIfPresent(OaNoteDO::getCategoryId, pageReqVO.getCategoryId())
                .eqIfPresent(OaNoteDO::getType, pageReqVO.getType())
                .eqIfPresent(OaNoteDO::getPriority, pageReqVO.getPriority())
                .eqIfPresent(OaNoteDO::getFavorite, pageReqVO.getFavorite())
                .betweenIfPresent(OaNoteDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(OaNoteDO::getPriority)
                .orderByDesc(OaNoteDO::getCreateTime)
                .orderByDesc(OaNoteDO::getId);
    }

    default List<OaNoteDO> selectListByCategoryIdAndIds(Long categoryId, Collection<Long> ids) {
        return selectList(new LambdaQueryWrapperX<OaNoteDO>()
                .eq(OaNoteDO::getCategoryId, categoryId).in(OaNoteDO::getId, ids));
    }

    default void updateForSave(OaNoteDO note) {
        update(note, new LambdaUpdateWrapper<OaNoteDO>().eq(OaNoteDO::getId, note.getId())
                .set(note.getFileUrls() == null, OaNoteDO::getFileUrls, null));
    }

}
