package cn.iocoder.yudao.module.crm.dal.mysql.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypeQueryVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商机状态类型 Mapper
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessStatusTypeMapper extends BaseMapperX<CrmBusinessStatusTypeDO> {

    default PageResult<CrmBusinessStatusTypeDO> selectPage(CrmBusinessStatusTypePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmBusinessStatusTypeDO>()
                .orderByDesc(CrmBusinessStatusTypeDO::getId));
    }

    default List<CrmBusinessStatusTypeDO> selectList(CrmBusinessStatusTypeQueryVO queryVO) {
        return selectList(new LambdaQueryWrapperX<CrmBusinessStatusTypeDO>()
                .eqIfPresent(CrmBusinessStatusTypeDO::getStatus, queryVO.getStatus())
                .inIfPresent(CrmBusinessStatusTypeDO::getId, queryVO.getIdList()));
    }
}
