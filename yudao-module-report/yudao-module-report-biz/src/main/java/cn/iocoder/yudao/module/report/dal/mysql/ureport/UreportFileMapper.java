package cn.iocoder.yudao.module.report.dal.mysql.ureport;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.*;

/**
 * Ureport2报表 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface UreportFileMapper extends BaseMapperX<UreportFileDO> {

    default PageResult<UreportFileDO> selectPage(UreportFilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UreportFileDO>()
                .likeIfPresent(UreportFileDO::getFileName, reqVO.getFileName())
                .eqIfPresent(UreportFileDO::getStatus, reqVO.getStatus())
                .eqIfPresent(UreportFileDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(UreportFileDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UreportFileDO::getId));
    }

}