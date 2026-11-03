package cn.iocoder.yudao.module.report.dal.mysql.ureport2;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FilePageReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport2.UReport2FileDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * UReport2 报表模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface UReport2FileMapper extends BaseMapperX<UReport2FileDO> {

    default UReport2FileDO selectByName(String name) {
        return selectOne(UReport2FileDO::getName, name);
    }

    /**
     * 获得模板列表，不查询模板内容
     *
     * @return 模板列表
     */
    default List<UReport2FileDO> selectListWithoutContent() {
        return selectList(new LambdaQueryWrapperX<UReport2FileDO>()
                .orderByAsc(UReport2FileDO::getName)
                .select(UReport2FileDO::getId, UReport2FileDO::getName, UReport2FileDO::getUpdateTime));
    }

    /**
     * 获得模板分页，不查询模板内容
     *
     * @param reqVO 分页查询
     * @return 模板分页
     */
    default PageResult<UReport2FileDO> selectPage(UReport2FilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UReport2FileDO>()
                .likeIfPresent(UReport2FileDO::getName, reqVO.getName())
                .orderByDesc(UReport2FileDO::getId)
                .select(UReport2FileDO::getId, UReport2FileDO::getName,
                        UReport2FileDO::getCreateTime, UReport2FileDO::getUpdateTime));
    }

}
