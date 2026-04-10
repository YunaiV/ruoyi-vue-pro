package cn.iocoder.yudao.module.mes.dal.mysql.wm.productissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.MesWmProductIssuePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 领料出库单 Mapper
 */
@Mapper
public interface MesWmProductIssueMapper extends BaseMapperX<MesWmProductIssueDO> {

    default PageResult<MesWmProductIssueDO> selectPage(MesWmProductIssuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmProductIssueDO>()
                .likeIfPresent(MesWmProductIssueDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmProductIssueDO::getName, reqVO.getName())
                .eqIfPresent(MesWmProductIssueDO::getWorkstationId, reqVO.getWorkstationId())
                .eqIfPresent(MesWmProductIssueDO::getWorkOrderId, reqVO.getWorkOrderId())
                .eqIfPresent(MesWmProductIssueDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesWmProductIssueDO::getIssueDate, reqVO.getIssueDate())
                .orderByDesc(MesWmProductIssueDO::getId));
    }

    default MesWmProductIssueDO selectByCode(String code) {
        return selectOne(MesWmProductIssueDO::getCode, code);
    }

}
