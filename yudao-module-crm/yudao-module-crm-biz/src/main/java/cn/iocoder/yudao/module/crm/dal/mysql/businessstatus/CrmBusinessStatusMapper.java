package cn.iocoder.yudao.module.crm.dal.mysql.businessstatus;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatus.CrmBusinessStatusDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商机状态 Mapper
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessStatusMapper extends BaseMapperX<CrmBusinessStatusDO> {

    default PageResult<CrmBusinessStatusDO> selectPage(CrmBusinessStatusPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmBusinessStatusDO>()
                .eqIfPresent(CrmBusinessStatusDO::getTypeId, reqVO.getTypeId())
                .orderByDesc(CrmBusinessStatusDO::getId));
    }

    default List<CrmBusinessStatusDO> selectList(CrmBusinessStatusExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CrmBusinessStatusDO>()
                .eqIfPresent(CrmBusinessStatusDO::getTypeId, reqVO.getTypeId())
                .likeIfPresent(CrmBusinessStatusDO::getName, reqVO.getName())
                .eqIfPresent(CrmBusinessStatusDO::getPercent, reqVO.getPercent())
                .eqIfPresent(CrmBusinessStatusDO::getSort, reqVO.getSort())
                .orderByDesc(CrmBusinessStatusDO::getId));
    }

    default List<CrmBusinessStatusDO> getBusinessStatusListByTypeId(Integer typeId) {
        return selectList(CrmBusinessStatusDO::getTypeId, typeId);
    }

}
