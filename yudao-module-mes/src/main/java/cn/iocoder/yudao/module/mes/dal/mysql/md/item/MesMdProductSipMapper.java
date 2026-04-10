package cn.iocoder.yudao.module.mes.dal.mysql.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sip.MesMdProductSipPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductSipDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 产品SIP Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesMdProductSipMapper extends BaseMapperX<MesMdProductSipDO> {

    default PageResult<MesMdProductSipDO> selectPage(MesMdProductSipPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesMdProductSipDO>()
                .eq(MesMdProductSipDO::getItemId, reqVO.getItemId())
                .likeIfPresent(MesMdProductSipDO::getTitle, reqVO.getTitle())
                .orderByAsc(MesMdProductSipDO::getSort));
    }

    default List<MesMdProductSipDO> selectByItemId(Long itemId) {
        return selectList(new LambdaQueryWrapperX<MesMdProductSipDO>()
                .eq(MesMdProductSipDO::getItemId, itemId)
                .orderByAsc(MesMdProductSipDO::getSort));
    }

    default Long selectCountByItemIdAndSort(Long itemId, Integer sort, Long excludeId) {
        return selectCount(new LambdaQueryWrapperX<MesMdProductSipDO>()
                .eq(MesMdProductSipDO::getItemId, itemId)
                .eq(MesMdProductSipDO::getSort, sort)
                .neIfPresent(MesMdProductSipDO::getId, excludeId));
    }

    default void deleteByItemId(Long itemId) {
        delete(MesMdProductSipDO::getItemId, itemId);
    }

}
