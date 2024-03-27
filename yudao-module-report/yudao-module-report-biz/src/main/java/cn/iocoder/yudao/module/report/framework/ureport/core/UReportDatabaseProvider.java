package cn.iocoder.yudao.module.report.framework.ureport.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;
import cn.iocoder.yudao.module.report.framework.ureport.config.UReportProperties;
import cn.iocoder.yudao.module.report.service.ureport.UReportDataService;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 基于数据库的 {@link ReportProvider} 实现类
 *
 * @author 赤焰
 */
// TODO @赤焰：这个 bean 的注解，交给 UReportConfiguration 搞
@Component
@Slf4j
@Setter
public class UReportDatabaseProvider implements ReportProvider {

    @Autowired
    private UReportProperties uReportProperties;
    @Resource
    private UReportDataService uReportDataService;

    @Override
    public InputStream loadReport(String name) {
        uReportDataService.validateUReportDataExists(getCorrectName(name));
        UReportDataDO uReportDataDO = uReportDataService.selectOneByName(getCorrectName(name));
        String content = uReportDataDO.getContent();
        return new ByteArrayInputStream(content.getBytes());
    }

    @Override
    public void deleteReport(String name) {
        uReportDataService.deleteByName(getCorrectName(name));
    }

    @Override
    public List<ReportFile> getReportFiles() {
        List<UReportDataDO> list = uReportDataService.getReportDataList();
        // TODO @赤焰：这里，不用判空，CollectionUtils.convertList 已经处理了哈。
        if(CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return convertList(list, report -> new ReportFile(report.getName(), DateUtils.of(report.getUpdateTime())));
    }

    @Override
    public void saveReport(String name, String content) {
        // TODO @赤焰：收到 uReportDataService 里面实现一个 saveUReportData 方法，然后这里调用即可。
        name = getCorrectName(name);
        UReportDataDO uReportDataDO = uReportDataService.selectOneByName(name);
        UReportDataSaveReqVO saveReqVO = new UReportDataSaveReqVO();
        if (uReportDataDO == null) {
            saveReqVO.setName(name);
            saveReqVO.setContent(content);
            saveReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
            uReportDataService.createUReportData(saveReqVO);
        } else {
            saveReqVO.setId(uReportDataDO.getId());
            saveReqVO.setName(name);
            saveReqVO.setContent(content);
            saveReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
            uReportDataService.updateUReportData(saveReqVO);
        }
    }

    @Override
    public String getName() {
        return uReportProperties.getName();
    }

    @Override
    public boolean disabled() {
        return uReportProperties.isDisabled();
    }

    @Override
    public String getPrefix() {
        return uReportProperties.getPrefix();
    }

    /**
     * 去除存储媒介，获取报表名字
     *
     * @param name 前端传入的报表带存储媒介的名字
     * @return 表名字
     */
    private String getCorrectName(String name) {
        return StrUtil.removePrefix(name,getPrefix());
    }

}
