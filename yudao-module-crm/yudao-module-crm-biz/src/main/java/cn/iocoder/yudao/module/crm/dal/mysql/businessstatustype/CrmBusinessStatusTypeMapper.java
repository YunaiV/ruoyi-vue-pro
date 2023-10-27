package cn.iocoder.yudao.module.crm.dal.mysql.businessstatustype;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatustype.CrmBusinessStatusTypeDO;
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
                .likeIfPresent(CrmBusinessStatusTypeDO::getName, reqVO.getName())
//                .eqIfPresent(CrmBusinessStatusTypeDO::getDeptIds, reqVO.getDeptIds()) TODO 报错，临时注释掉
                .eqIfPresent(CrmBusinessStatusTypeDO::getStatus, reqVO.getStatus())
                .orderByDesc(CrmBusinessStatusTypeDO::getId));
    }

    default List<CrmBusinessStatusTypeDO> selectList(CrmBusinessStatusTypeExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CrmBusinessStatusTypeDO>()
                .likeIfPresent(CrmBusinessStatusTypeDO::getName, reqVO.getName())
                .eqIfPresent(CrmBusinessStatusTypeDO::getDeptIds, reqVO.getDeptIds())
                .eqIfPresent(CrmBusinessStatusTypeDO::getStatus, reqVO.getStatus())
                .orderByDesc(CrmBusinessStatusTypeDO::getId));
    }

    default List<CrmBusinessStatusTypeDO> getBusinessStatusTypeListByStatus(Integer status) {
        return selectList(CrmBusinessStatusTypeDO::getStatus, status.byteValue());
    }

}
