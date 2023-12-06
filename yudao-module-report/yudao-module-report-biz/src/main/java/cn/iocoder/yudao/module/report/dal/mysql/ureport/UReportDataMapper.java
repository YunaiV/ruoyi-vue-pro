package cn.iocoder.yudao.module.report.dal.mysql.ureport;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.*;

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

    /**
     * 根据名字查询报表
     * @param name 报表名字
     * @return
     */
    default List<UReportDataDO> selectByName(String name){
        return selectList(new LambdaQueryWrapperX<UReportDataDO>()
                .eqIfPresent(UReportDataDO::getName,name));
    }

    /**
     * 根据名字查询报表
     * @param name 报表名字
     * @return
     */
    default UReportDataDO selectOneByName(String name){
        return selectOne(new LambdaQueryWrapperX<UReportDataDO>()
                .eqIfPresent(UReportDataDO::getName,name));
    }


    /**
     * 根据名字删除报表
     * @param name 报表名字
     * @return
     */
    default int deleteByName(String name){
        return delete(new LambdaQueryWrapperX<UReportDataDO>()
                .eqIfPresent(UReportDataDO::getName,name));
    }

}
