package cn.iocoder.yudao.module.market.convert.banner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.banner.vo.BannerCreateReqVO;
import cn.iocoder.yudao.module.market.controller.admin.banner.vo.BannerRespVO;
import cn.iocoder.yudao.module.market.controller.admin.banner.vo.BannerUpdateReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.banner.BannerDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Banner Convert
 *
 * @author xia
 */
@Mapper
public interface BannerConvert {

    BannerConvert INSTANCE = Mappers.getMapper(BannerConvert.class);


    List<BannerRespVO> convertList(List<BannerDO> list);

    PageResult<BannerRespVO> convertPage(PageResult<BannerDO> pageResult);

    BannerRespVO convert(BannerDO banner);

    BannerDO convert(BannerCreateReqVO createReqVO);

    BannerDO convert(BannerUpdateReqVO updateReqVO);

}
