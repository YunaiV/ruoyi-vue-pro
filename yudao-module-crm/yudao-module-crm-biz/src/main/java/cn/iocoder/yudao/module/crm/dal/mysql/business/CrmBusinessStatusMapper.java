package cn.iocoder.yudao.module.crm.dal.mysql.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusQueryVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
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
                .orderByDesc(CrmBusinessStatusDO::getId));
    }

    default List<CrmBusinessStatusDO> selectList(CrmBusinessStatusQueryVO queryVO) {
        return selectList(new LambdaQueryWrapperX<CrmBusinessStatusDO>()
                .eqIfPresent(CrmBusinessStatusDO::getTypeId, queryVO.getTypeId())
                .inIfPresent(CrmBusinessStatusDO::getId, queryVO.getIdList())
                .orderByDesc(CrmBusinessStatusDO::getId));
    }

    default int delete(Long typeId) {
        return delete(CrmBusinessStatusDO::getTypeId, typeId);
    }

}
