package cn.iocoder.yudao.module.mes.dal.mysql.wm.barcode;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 条码配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmBarcodeConfigMapper extends BaseMapperX<MesWmBarcodeConfigDO> {

    default PageResult<MesWmBarcodeConfigDO> selectPage(MesWmBarcodeConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmBarcodeConfigDO>()
                .eqIfPresent(MesWmBarcodeConfigDO::getFormat, reqVO.getFormat())
                .eqIfPresent(MesWmBarcodeConfigDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MesWmBarcodeConfigDO::getAutoGenerateFlag, reqVO.getAutoGenerateFlag())
                .eqIfPresent(MesWmBarcodeConfigDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesWmBarcodeConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MesWmBarcodeConfigDO::getId));
    }

    default MesWmBarcodeConfigDO selectByBizType(Integer bizType) {
        return selectOne(MesWmBarcodeConfigDO::getBizType, bizType);
    }

}
