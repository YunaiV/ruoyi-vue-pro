package cn.iocoder.yudao.module.wms.dal.mysql.external.storage;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.*;

/**
 * 外部存储库 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsExternalStorageMapper extends BaseMapperX<WmsExternalStorageDO> {

    default PageResult<WmsExternalStorageDO> selectPage(WmsExternalStoragePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsExternalStorageDO>()
                .eqIfPresent(WmsExternalStorageDO::getCode, reqVO.getCode())
                .likeIfPresent(WmsExternalStorageDO::getName, reqVO.getName())
                .eqIfPresent(WmsExternalStorageDO::getType, reqVO.getType())
                .eqIfPresent(WmsExternalStorageDO::getApiParameters, reqVO.getApiParameters())
                .betweenIfPresent(WmsExternalStorageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsExternalStorageDO::getId));
    }

    

}