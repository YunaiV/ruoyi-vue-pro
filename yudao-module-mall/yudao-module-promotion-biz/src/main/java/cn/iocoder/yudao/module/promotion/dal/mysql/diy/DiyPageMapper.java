package cn.iocoder.yudao.module.promotion.dal.mysql.diy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPagePageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 装修页面 Mapper
 *
 * @author owen
 */
@Mapper
public interface DiyPageMapper extends BaseMapperX<DiyPageDO> {

    default PageResult<DiyPageDO> selectPage(DiyPagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DiyPageDO>()
                .likeIfPresent(DiyPageDO::getName, reqVO.getName())
                .betweenIfPresent(DiyPageDO::getCreateTime, reqVO.getCreateTime())
                // 模板下面的页面，在模板中管理
                .isNull(DiyPageDO::getTemplateId)
                .orderByDesc(DiyPageDO::getId));
    }

    default List<DiyPageDO> selectListByTemplateId(Long templateId) {
        return selectList(DiyPageDO::getTemplateId, templateId);
    }

    default DiyPageDO selectByNameAndTemplateIdIsNull(String name) {
        return selectOne(new LambdaQueryWrapperX<DiyPageDO>()
                .eq(DiyPageDO::getName, name)
                .isNull(DiyPageDO::getTemplateId));
    }

}
