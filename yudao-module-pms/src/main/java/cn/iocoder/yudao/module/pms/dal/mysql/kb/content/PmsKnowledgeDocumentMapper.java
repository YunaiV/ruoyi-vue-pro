package cn.iocoder.yudao.module.pms.dal.mysql.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentSearchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Mapper
public interface PmsKnowledgeDocumentMapper extends BaseMapperX<PmsKnowledgeDocumentDO> {

    default Set<Long> selectExistingPermissionIdSet(Collection<Long> permissionIds) {
        return convertSet(selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentDO>()
                .in(PmsKnowledgeDocumentDO::getPermissionId, permissionIds)
                .select(PmsKnowledgeDocumentDO::getPermissionId)), PmsKnowledgeDocumentDO::getPermissionId);
    }

    /**
     * 统计各知识库下不同文档类型的数量
     *
     * @param libraryIds 知识库编号集合
     * @param status 文档状态
     * @return 知识库编号 ->（文档类型 -> 文档数量）
     */
    default Map<Long, Map<Integer, Long>> selectTypeCountMapByLibraryIdsAndStatus(
            Collection<Long> libraryIds, Integer status) {
        if (CollUtil.isEmpty(libraryIds)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = selectMaps(new QueryWrapperX<PmsKnowledgeDocumentDO>()
                .select("library_id AS libraryId", "type", "COUNT(*) AS count")
                .in("library_id", libraryIds)
                .eq("status", status)
                .groupBy("library_id", "type"));
        Map<Long, Map<Integer, Long>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long libraryId = ((Number) row.get("libraryId")).longValue();
            Integer type = ((Number) row.get("type")).intValue();
            result.computeIfAbsent(libraryId, key -> new LinkedHashMap<>())
                    .put(type, ((Number) row.get("count")).longValue());
        }
        return result;
    }

    default List<PmsKnowledgeDocumentDO> selectListByLibraryIdAndStatus(Long libraryId, Integer status) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentDO>()
                .eq(PmsKnowledgeDocumentDO::getLibraryId, libraryId)
                .eq(PmsKnowledgeDocumentDO::getStatus, status)
                .orderByAsc(PmsKnowledgeDocumentDO::getCreateTime)
                .orderByAsc(PmsKnowledgeDocumentDO::getId));
    }

    default List<PmsKnowledgeDocumentDO> selectListByLibraryId(Long libraryId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentDO>()
                .eq(PmsKnowledgeDocumentDO::getLibraryId, libraryId)
                .orderByAsc(PmsKnowledgeDocumentDO::getId));
    }

    default List<PmsKnowledgeDocumentDO> selectListByParentIds(Collection<Long> parentIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentDO>()
                .in(PmsKnowledgeDocumentDO::getParentId, parentIds));
    }

    default List<PmsKnowledgeDocumentDO> selectListByFolderIds(Collection<Long> folderIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentDO>()
                .in(PmsKnowledgeDocumentDO::getFolderId, folderIds));
    }

    default int updateToRestoreByIds(Collection<Long> ids, Integer status) {
        return update(new LambdaUpdateWrapper<PmsKnowledgeDocumentDO>()
                .set(PmsKnowledgeDocumentDO::getStatus, status)
                .set(PmsKnowledgeDocumentDO::getDeleteUserId, null)
                .set(PmsKnowledgeDocumentDO::getDeleteTime, null)
                .in(PmsKnowledgeDocumentDO::getId, ids));
    }

    /**
     * 按标签和状态分页查询文档，并限制在用户可读的知识库和权限范围内
     *
     * @param pageReqVO 标签分页查询条件
     * @param readableLibraryIds 用户可读的知识库编号集合
     * @param readablePermissionIds 用户可读的内容权限编号集合
     * @param statuses 文档状态集合
     * @return 文档分页结果
     */
    default PageResult<PmsKnowledgeDocumentDO> selectPageByLabelIdAndStatuses(
            PmsKnowledgeDocumentLabelPageReqVO pageReqVO, Collection<Long> readableLibraryIds,
            Collection<Long> readablePermissionIds, Collection<Integer> statuses) {
        LambdaQueryWrapperX<PmsKnowledgeDocumentDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.in(PmsKnowledgeDocumentDO::getLibraryId, readableLibraryIds)
                .in(PmsKnowledgeDocumentDO::getPermissionId, readablePermissionIds)
                .in(PmsKnowledgeDocumentDO::getStatus, statuses)
                .orderByDesc(PmsKnowledgeDocumentDO::getUpdateTime)
                .orderByDesc(PmsKnowledgeDocumentDO::getId);
        queryWrapper.apply(MyBatisUtils.findInSet("label_ids"), pageReqVO.getLabelId());
        return selectPage(pageReqVO, queryWrapper);
    }

    /**
     * 分页查询文档，并限制在用户可读的知识库、权限和状态范围内
     *
     * @param pageReqVO 文档分页查询条件
     * @param readableLibraryIds 用户可读的知识库编号集合
     * @param readablePermissionIds 用户可读的内容权限编号集合
     * @param statuses 文档状态集合
     * @return 文档分页结果
     */
    default PageResult<PmsKnowledgeDocumentDO> selectPage(PmsKnowledgeDocumentSearchPageReqVO pageReqVO,
                                                          Collection<Long> readableLibraryIds,
                                                          Collection<Long> readablePermissionIds,
                                                          Collection<Integer> statuses) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<PmsKnowledgeDocumentDO>()
                .in(PmsKnowledgeDocumentDO::getLibraryId, readableLibraryIds)
                .in(PmsKnowledgeDocumentDO::getPermissionId, readablePermissionIds)
                .in(PmsKnowledgeDocumentDO::getStatus, statuses)
                .eqIfPresent(PmsKnowledgeDocumentDO::getLibraryId, pageReqVO.getLibraryId())
                .eqIfPresent(PmsKnowledgeDocumentDO::getCreator,
                        Convert.toStr(pageReqVO.getCreatorUserId()))
                .betweenIfPresent(PmsKnowledgeDocumentDO::getUpdateTime, pageReqVO.getUpdateTime())
                .and(StrUtil.isNotBlank(pageReqVO.getKeyword()),
                        wrapper -> wrapper.like(PmsKnowledgeDocumentDO::getTitle, pageReqVO.getKeyword())
                        .or().like(PmsKnowledgeDocumentDO::getContent, pageReqVO.getKeyword()))
                .orderByDesc(PmsKnowledgeDocumentDO::getUpdateTime)
                .orderByDesc(PmsKnowledgeDocumentDO::getId));
    }

}
