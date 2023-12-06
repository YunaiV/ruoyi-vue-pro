package cn.iocoder.yudao.module.report.dal.mysql.ureport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataPageReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Ureport2报表 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface UReportDataMapper extends BaseMapperX<UReportDataDO> {

    default PageResult<UReportDataDO> selectPage(UReportDataPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UReportDataDO>()
                .likeIfPresent(UReportDataDO::getName, reqVO.getName())
                .eqIfPresent(UReportDataDO::getStatus, reqVO.getStatus())
                .eqIfPresent(UReportDataDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(UReportDataDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UReportDataDO::getId));
    }

    default List<UReportDataDO> selectListByName(String name) {
        return selectList(new LambdaQueryWrapperX<UReportDataDO>()
                .eqIfPresent(UReportDataDO::getName,name));
    }

    default UReportDataDO selectByName(String name){
        return selectOne(new LambdaQueryWrapperX<UReportDataDO>()
                .eqIfPresent(UReportDataDO::getName,name));
    }

    default int deleteByName(String name) {
        return delete(new LambdaQueryWrapperX<UReportDataDO>()
                .eqIfPresent(UReportDataDO::getName,name));
    }

}
