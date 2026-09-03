package cn.iocoder.yudao.module.pms.dal.mysql.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibraryPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Mapper
public interface PmsKnowledgeLibraryMapper extends BaseMapperX<PmsKnowledgeLibraryDO> {

    default List<Long> selectIdList(Boolean openStatus, Integer status) {
        return convertList(selectList(new LambdaQueryWrapperX<PmsKnowledgeLibraryDO>()
                .eq(PmsKnowledgeLibraryDO::getStatus, status)
                .eqIfPresent(PmsKnowledgeLibraryDO::getOpenStatus, openStatus)
                .select(PmsKnowledgeLibraryDO::getId)), PmsKnowledgeLibraryDO::getId);
    }

    /**
     * 分页查询知识库，并按公开状态和成员关系过滤可见范围
     *
     * @param pageReqVO 分页查询条件
     * @param memberLibraryIds 当前用户作为成员可访问的知识库编号集合
     * @param includeAll 是否包含全部知识库
     * @param filterLibraryIds 指定过滤的知识库编号集合
     * @param status 知识库状态
     * @return 知识库分页结果
     */
    default PageResult<PmsKnowledgeLibraryDO> selectPage(PmsKnowledgeLibraryPageReqVO pageReqVO,
                                                         Collection<Long> memberLibraryIds,
                                                         boolean includeAll,
                                                         Collection<Long> filterLibraryIds,
                                                         Integer status) {
        LambdaQueryWrapperX<PmsKnowledgeLibraryDO> query = new LambdaQueryWrapperX<PmsKnowledgeLibraryDO>()
                .eq(PmsKnowledgeLibraryDO::getStatus, status)
                .likeIfPresent(PmsKnowledgeLibraryDO::getName, pageReqVO.getName());
        if (filterLibraryIds != null) {
            query.in(PmsKnowledgeLibraryDO::getId, filterLibraryIds);
        } else if (!includeAll) {
            query.and(wrapper -> {
                wrapper.eq(PmsKnowledgeLibraryDO::getOpenStatus, true);
                if (CollUtil.isNotEmpty(memberLibraryIds)) {
                    wrapper.or().in(PmsKnowledgeLibraryDO::getId, memberLibraryIds);
                }
            });
        }
        query.orderByDesc(PmsKnowledgeLibraryDO::getCreateTime).orderByDesc(PmsKnowledgeLibraryDO::getId);
        return selectPage(pageReqVO, query);
    }

    default int updateForEdit(PmsKnowledgeLibraryDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<PmsKnowledgeLibraryDO>()
                .set(updateObj.getDescription() == null, PmsKnowledgeLibraryDO::getDescription, null)
                .set(updateObj.getCoverUrl() == null, PmsKnowledgeLibraryDO::getCoverUrl, null)
                .eq(PmsKnowledgeLibraryDO::getId, updateObj.getId()));
    }

    default int updateToRestoreById(Long id, Integer status) {
        return update(new LambdaUpdateWrapper<PmsKnowledgeLibraryDO>()
                .set(PmsKnowledgeLibraryDO::getStatus, status)
                .set(PmsKnowledgeLibraryDO::getDeleteUserId, null)
                .set(PmsKnowledgeLibraryDO::getDeleteTime, null)
                .eq(PmsKnowledgeLibraryDO::getId, id));
    }

}
