package cn.iocoder.yudao.module.iot.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.script.IotProductScriptPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductScriptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 产品脚本信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotProductScriptMapper extends BaseMapperX<IotProductScriptDO> {

    default PageResult<IotProductScriptDO> selectPage(IotProductScriptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotProductScriptDO>()
                .eqIfPresent(IotProductScriptDO::getProductId, reqVO.getProductId())
                .eqIfPresent(IotProductScriptDO::getProductKey, reqVO.getProductKey())
                .eqIfPresent(IotProductScriptDO::getScriptType, reqVO.getScriptType())
                .eqIfPresent(IotProductScriptDO::getScriptLanguage, reqVO.getScriptLanguage())
                .eqIfPresent(IotProductScriptDO::getStatus, reqVO.getStatus())
                .eqIfPresent(IotProductScriptDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(IotProductScriptDO::getLastTestTime, reqVO.getLastTestTime())
                .betweenIfPresent(IotProductScriptDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotProductScriptDO::getId));
    }

}