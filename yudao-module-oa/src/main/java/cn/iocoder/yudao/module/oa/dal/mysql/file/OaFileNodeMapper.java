package cn.iocoder.yudao.module.oa.dal.mysql.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.node.OaFileNodePageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileNodeDO;
import cn.iocoder.yudao.module.oa.enums.file.OaFileNodeStatusEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileScopeEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 云盘文件节点 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaFileNodeMapper extends BaseMapperX<OaFileNodeDO> {

    default List<OaFileNodeDO> selectListByParentId(Long parentId) {
        return selectList(OaFileNodeDO::getParentId, parentId);
    }

    default List<OaFileNodeDO> selectListByCreatorAndTypeForUpdate(String creator, Integer type) {
        return selectList(new LambdaQueryWrapperX<OaFileNodeDO>().eq(OaFileNodeDO::getCreator, creator)
                .eq(OaFileNodeDO::getType, type).orderByAsc(OaFileNodeDO::getId).last("FOR UPDATE"));
    }

    default List<OaFileNodeDO> selectListByCreatorAndStatus(String creator, Integer status) {
        return selectList(OaFileNodeDO::getCreator, creator, OaFileNodeDO::getStatus, status);
    }

    default Long selectSumSizeByCreatorAndType(String creator, Integer type) {
        // TODO @AI：不要写在一条里；分成多个；看看能不能去掉 COALESCE
        return selectOne(new QueryWrapperX<OaFileNodeDO>().select("COALESCE(SUM(size), 0) AS size")
                .eq("creator", creator).eq("type", type)).getSize();
    }

    default PageResult<OaFileNodeDO> selectPageByParentIdAndCreatorAndStatus(
            OaFileNodePageReqVO reqVO, String creator, Integer status) {
        return selectPage(reqVO, buildCandidateQuery(reqVO)
                .eq(OaFileNodeDO::getParentId, reqVO.getParentId())
                .eqIfPresent(OaFileNodeDO::getCreator, creator).eq(OaFileNodeDO::getStatus, status)
                .orderByAsc(OaFileNodeDO::getType)
                .orderByDesc(OaFileNodeDO::getCreateTime, OaFileNodeDO::getId));
    }

    default List<OaFileNodeDO> selectListByParentIds(Collection<Long> parentIds) {
        return selectList(OaFileNodeDO::getParentId, parentIds);
    }

    default List<OaFileNodeDO> selectListByCreatorAndTypeAndStatus(String creator, Integer type, Integer status) {
        return selectList(new LambdaQueryWrapperX<OaFileNodeDO>().eq(OaFileNodeDO::getCreator, creator)
                .eq(OaFileNodeDO::getType, type).eq(OaFileNodeDO::getStatus, status));
    }

    default List<OaFileNodeDO> selectListByParentIdsAndTypeAndStatus(Collection<Long> parentIds, Integer type, Integer status) {
        return selectList(new LambdaQueryWrapperX<OaFileNodeDO>().in(OaFileNodeDO::getParentId, parentIds)
                .eq(OaFileNodeDO::getType, type).eq(OaFileNodeDO::getStatus, status));
    }

    default List<OaFileNodeDO> selectCandidateList(OaFileNodePageReqVO reqVO, Long userId,
                                                   Collection<Long> favoriteIds) {
        LambdaQueryWrapperX<OaFileNodeDO> query = buildCandidateQuery(reqVO);
        boolean rootQuery = reqVO.getParentId() == null || OaFileNodeDO.PARENT_ID_ROOT.equals(reqVO.getParentId());
        if (OaFileScopeEnum.RECYCLE.getScope().equals(reqVO.getScope())) {
            query.eq(OaFileNodeDO::getCreator, userId.toString())
                    .eq(OaFileNodeDO::getStatus, OaFileNodeStatusEnum.RECYCLED.getStatus());
        } else {
            query.eq(OaFileNodeDO::getStatus, OaFileNodeStatusEnum.NORMAL.getStatus());
            if (rootQuery && OaFileScopeEnum.MY.getScope().equals(reqVO.getScope())) {
                query.eq(OaFileNodeDO::getCreator, userId.toString());
            } else if (rootQuery && OaFileScopeEnum.SHARED.getScope().equals(reqVO.getScope())) {
                query.ne(OaFileNodeDO::getCreator, userId.toString());
            } else if (rootQuery && OaFileScopeEnum.FAVORITE.getScope().equals(reqVO.getScope())) {
                query.in(OaFileNodeDO::getId, favoriteIds);
            }
            // 共享入口可能处于任意层级，不能用 parentId = 0 提前过滤
            if (!(rootQuery && OaFileScopeEnum.SHARED.getScope().equals(reqVO.getScope()))) {
                query.eqIfPresent(OaFileNodeDO::getParentId, reqVO.getParentId());
            }
        }
        return selectList(query);
    }

    default List<OaFileNodeDO> selectListBySharedNodeIdsOrParentIdsAndCreatorNot(OaFileNodePageReqVO reqVO, Long userId,
                                                         Collection<Long> sharedNodeIds, Collection<Long> parentIds) {
        return selectList(buildCandidateQuery(reqVO)
                .eq(OaFileNodeDO::getStatus, OaFileNodeStatusEnum.NORMAL.getStatus())
                .ne(OaFileNodeDO::getCreator, userId.toString())
                .and(query -> query.in(CollUtil.isNotEmpty(sharedNodeIds), OaFileNodeDO::getId, sharedNodeIds)
                        .or(CollUtil.isNotEmpty(sharedNodeIds) && CollUtil.isNotEmpty(parentIds))
                        .in(CollUtil.isNotEmpty(parentIds), OaFileNodeDO::getParentId, parentIds)));
    }

    default LambdaQueryWrapperX<OaFileNodeDO> buildCandidateQuery(OaFileNodePageReqVO reqVO) {
        return new LambdaQueryWrapperX<OaFileNodeDO>().likeIfPresent(OaFileNodeDO::getName, reqVO.getName())
                .eqIfPresent(OaFileNodeDO::getCategory, reqVO.getCategory())
                .betweenIfPresent(OaFileNodeDO::getCreateTime, reqVO.getCreateTime());
    }

    default OaFileNodeDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(OaFileNodeDO::getId, id);
    }



}
