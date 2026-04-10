package cn.iocoder.yudao.module.mes.dal.mysql.wm.packages;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.MesWmPackagePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.packages.MesWmPackageDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmPackageStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * MES 装箱单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmPackageMapper extends BaseMapperX<MesWmPackageDO> {

    default PageResult<MesWmPackageDO> selectPage(MesWmPackagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmPackageDO>()
                .likeIfPresent(MesWmPackageDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmPackageDO::getSalesOrderCode, reqVO.getSalesOrderCode())
                .eqIfPresent(MesWmPackageDO::getClientId, reqVO.getClientId())
                .eqIfPresent(MesWmPackageDO::getParentId, reqVO.getParentId())
                .eqIfPresent(MesWmPackageDO::getInspectorUserId, reqVO.getInspectorUserId())
                .orderByDesc(MesWmPackageDO::getId));
    }

    default MesWmPackageDO selectByCode(String code) {
        return selectOne(MesWmPackageDO::getCode, code);
    }

    default List<MesWmPackageDO> selectListByParentId(Long parentId) {
        return selectList(MesWmPackageDO::getParentId, parentId);
    }

    default List<MesWmPackageDO> selectListByParentIds(Collection<Long> parentIds) {
        return selectList(new LambdaQueryWrapperX<MesWmPackageDO>()
                .in(MesWmPackageDO::getParentId, parentIds));
    }

    default List<MesWmPackageDO> selectChildableList() {
        return selectList(new LambdaQueryWrapperX<MesWmPackageDO>()
                .eq(MesWmPackageDO::getParentId, MesWmPackageDO.PARENT_ID_ROOT)
                .eq(MesWmPackageDO::getStatus, MesWmPackageStatusEnum.FINISHED.getStatus())
                .orderByDesc(MesWmPackageDO::getId));
    }

}
