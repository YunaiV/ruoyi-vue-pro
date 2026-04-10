package cn.iocoder.yudao.module.mes.dal.mysql.qc.rqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.MesQcRqcPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 退货检验单（RQC） Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesQcRqcMapper extends BaseMapperX<MesQcRqcDO> {

    default PageResult<MesQcRqcDO> selectPage(MesQcRqcPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesQcRqcDO>()
                .likeIfPresent(MesQcRqcDO::getCode, reqVO.getCode())
                .eqIfPresent(MesQcRqcDO::getSourceDocType, reqVO.getSourceDocType())
                .likeIfPresent(MesQcRqcDO::getSourceDocCode, reqVO.getSourceDocCode())
                .eqIfPresent(MesQcRqcDO::getItemId, reqVO.getItemId())
                .likeIfPresent(MesQcRqcDO::getBatchCode, reqVO.getBatchCode())
                .eqIfPresent(MesQcRqcDO::getCheckResult, reqVO.getCheckResult())
                .eqIfPresent(MesQcRqcDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MesQcRqcDO::getInspectorUserId, reqVO.getInspectorUserId())
                .orderByDesc(MesQcRqcDO::getId));
    }

    default MesQcRqcDO selectByCode(String code) {
        return selectOne(MesQcRqcDO::getCode, code);
    }

}
