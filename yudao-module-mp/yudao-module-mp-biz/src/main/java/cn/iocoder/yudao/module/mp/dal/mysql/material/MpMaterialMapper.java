package cn.iocoder.yudao.module.mp.dal.mysql.material;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.material.MpMaterialDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MpMaterialMapper extends BaseMapperX<MpMaterialDO> {

    default MpMaterialDO selectByAccountIdAndMediaId(Long accountId, String mediaId) {
        return selectOne(MpMaterialDO::getAccountId, accountId,
                MpMaterialDO::getMediaId, mediaId);
    }

}
