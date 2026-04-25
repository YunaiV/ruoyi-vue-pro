package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashion3dAssetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 服装 3D 资产 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashion3dAssetMapper extends BaseMapperX<AiFashion3dAssetDO> {

    @Select("SELECT * FROM ai_fashion_3d_asset WHERE user_id = #{userId} AND deleted = 0 ORDER BY id DESC")
    List<AiFashion3dAssetDO> selectListByUserId(@Param("userId") Long userId);

}
