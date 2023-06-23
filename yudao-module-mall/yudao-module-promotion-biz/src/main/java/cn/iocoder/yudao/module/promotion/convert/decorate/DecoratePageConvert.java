package cn.iocoder.yudao.module.promotion.convert.decorate;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.promotion.api.decorate.dto.PageComponentDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.PageDecorateDO;
import cn.iocoder.yudao.module.promotion.enums.decorate.PageComponentEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DecoratePageConvert {

    DecoratePageConvert INSTANCE = Mappers.getMapper(DecoratePageConvert.class);

    default List<PageDecorateDO> convert(Integer type, List<DecoratePageReqVO.ComponentReqVO> components) {
        return CollectionUtils.convertList(components, c -> {
            PageComponentEnum component = PageComponentEnum.getOfCode(c.getComponentCode());
            if (component == null) {
                return null;
            }
            return new PageDecorateDO().setComponentCode(c.getComponentCode())
                    .setType(type).setComponentValue(c.getValue());
        });
    }

    default DecoratePageRespVO convert2(Integer type, List<PageDecorateDO> doList) {
        List<DecoratePageRespVO.ComponentRespVO> components = CollectionUtils.convertList(doList,
                item -> {
                    PageComponentEnum component = PageComponentEnum.getOfCode(item.getComponentCode());
                    if (component == null) {
                        return null;
                    }
                    @SuppressWarnings("rawtypes")
                    PageComponentDTO dto = component.parsePageComponent(item.getComponentValue());
                    return new DecoratePageRespVO.ComponentRespVO()
                            .setTitle(dto.getTitle()).setStyle(dto.getStyle()).setComponentCode(item.getComponentCode())
                            .setConfig(dto.getConfig()).setData(dto.getData());
                });
        return new DecoratePageRespVO().setType(type).setComponents(components);
    }
}
