package cn.iocoder.yudao.module.mes.dal.mysql.qc.indicatorresult;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 检验结果记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesQcIndicatorResultMapper extends BaseMapperX<MesQcIndicatorResultDO> {

    default PageResult<MesQcIndicatorResultDO> selectPage(MesQcIndicatorResultPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesQcIndicatorResultDO>()
                .eqIfPresent(MesQcIndicatorResultDO::getQcId, reqVO.getQcId())
                .eqIfPresent(MesQcIndicatorResultDO::getQcType, reqVO.getQcType())
                .likeIfPresent(MesQcIndicatorResultDO::getCode, reqVO.getCode())
                .eqIfPresent(MesQcIndicatorResultDO::getItemId, reqVO.getItemId())
                .orderByDesc(MesQcIndicatorResultDO::getId));
    }

}
