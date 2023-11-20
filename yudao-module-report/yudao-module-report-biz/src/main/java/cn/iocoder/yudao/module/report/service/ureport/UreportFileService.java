package cn.iocoder.yudao.module.report.service.ureport;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.*;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * Ureport2报表 Service 接口
 *
 * @author 芋道源码
 */
public interface UreportFileService {

    /**
     * 创建Ureport2报表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUreportFile(@Valid UreportFileSaveReqVO createReqVO);

    /**
     * 更新Ureport2报表
     *
     * @param updateReqVO 更新信息
     */
    void updateUreportFile(@Valid UreportFileSaveReqVO updateReqVO);

    /**
     * 删除Ureport2报表
     *
     * @param id 编号
     */
    void deleteUreportFile(Long id);

    /**
     * 获得Ureport2报表
     *
     * @param id 编号
     * @return Ureport2报表
     */
    UreportFileDO getUreportFile(Long id);

    /**
     * 获得Ureport2报表分页
     *
     * @param pageReqVO 分页查询
     * @return Ureport2报表分页
     */
    PageResult<UreportFileDO> getUreportFilePage(UreportFilePageReqVO pageReqVO);

}