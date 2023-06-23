package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.PageDecorateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.decorate.PageDecorateMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.module.promotion.convert.decorate.DecoratePageConvert.INSTANCE;

/**
 * @author jason
 */
@Service
public class DecoratePageServiceImpl implements DecoratePageService {
    @Resource
    private PageDecorateMapper pageDecorateMapper;
    @Override
    public List<PageDecorateDO> testReq(DecoratePageReqVO reqVO) {
        return INSTANCE.convert(reqVO.getType(), reqVO.getComponents());
    }

    @Override
    public DecoratePageRespVO testResp(Integer type) {
        return INSTANCE.convert2(type,pageDecorateMapper.selectByPageType(type));
    }
}
