package cn.iocoder.yudao.module.mes.dal.mysql.qc.iqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo.MesQcIqcPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 来料检验单（IQC） Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesQcIqcMapper extends BaseMapperX<MesQcIqcDO> {

    default PageResult<MesQcIqcDO> selectPage(MesQcIqcPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesQcIqcDO>()
                .likeIfPresent(MesQcIqcDO::getCode, reqVO.getCode())
                .eqIfPresent(MesQcIqcDO::getVendorId, reqVO.getVendorId())
                .likeIfPresent(MesQcIqcDO::getVendorBatch, reqVO.getVendorBatch())
                .eqIfPresent(MesQcIqcDO::getItemId, reqVO.getItemId())
                .eqIfPresent(MesQcIqcDO::getCheckResult, reqVO.getCheckResult())
                .betweenIfPresent(MesQcIqcDO::getReceiveDate, reqVO.getReceiveDate())
                .betweenIfPresent(MesQcIqcDO::getInspectDate, reqVO.getInspectDate())
                .eqIfPresent(MesQcIqcDO::getInspectorUserId, reqVO.getInspectorUserId())
                .orderByDesc(MesQcIqcDO::getId));
    }

    default MesQcIqcDO selectByCode(String code) {
        return selectOne(MesQcIqcDO::getCode, code);
    }

    default Long selectCountByVendorId(Long vendorId) {
        return selectCount(MesQcIqcDO::getVendorId, vendorId);
    }

}
