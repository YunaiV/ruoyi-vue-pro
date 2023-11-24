package cn.iocoder.yudao.module.report.framework.ureport.provider;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UreportFileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import cn.iocoder.yudao.module.report.service.ureport.UreportFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.UREPORT_FILE_EXISTS;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.UREPORT_FILE_NOT_EXISTS;

/**
 * 提供数据库的存储方式
 * @author 赤焰
 */
@Slf4j
@Setter
@Component
@ConfigurationProperties(prefix = "ureport.provider.database")
public class UreportDatabaseProvider implements ReportProvider{

    private static final String NAME = "mysql-provider";

    private String prefix = "mysql:";

    private boolean disabled = false;
    @Resource
    private UreportFileService ureportFileService;

    @Override
    public InputStream loadReport(String file) {
        Long reportDo = ureportFileService.checkExistByName(getCorrectName(file));
        if (reportDo <=0) {
            throw exception(UREPORT_FILE_NOT_EXISTS);
        }
        UreportFileDO ureportFileDO = ureportFileService.queryUreportFileDoByName(getCorrectName(file));
        byte[] content = ureportFileDO.getFileContent();
        return new ByteArrayInputStream(content);
    }

    @Override
    public void deleteReport(String file) {
        ureportFileService.deleteReportFileByName(getCorrectName(file));
    }

    @Override
    public List<ReportFile> getReportFiles() {
        List<UreportFileDO> list = ureportFileService.queryReportFileList();
        List<ReportFile> reportList = new ArrayList<>();
        if(CollUtil.isEmpty(list)){
            return reportList;
        }
        for (UreportFileDO reportFile : list) {
            LocalDateTime updateTime = reportFile.getUpdateTime();
            ZonedDateTime zdt = updateTime.atZone(ZoneId.systemDefault());
            Date date = Date.from(zdt.toInstant());
            reportList.add(new ReportFile(reportFile.getFileName(),date));
        }
        return reportList;

    }

    @Override
    public void saveReport(String file, String content) {
        file = getCorrectName(file);
        LambdaQueryWrapper<UreportFileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UreportFileDO::getFileName, file);
        Long count = ureportFileService.checkExistByName(file);
        if(count>1){
            throw exception(UREPORT_FILE_EXISTS);
        }
        UreportFileDO ureportFileDO = ureportFileService.queryUreportFileDoByName(file);
        UreportFileSaveReqVO saveReqVO = new UreportFileSaveReqVO();
        if (ureportFileDO == null) {
            saveReqVO.setFileName(file);
            saveReqVO.setFileContent(content);
            saveReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
            ureportFileService.createUreportFile(saveReqVO);
        } else {
            saveReqVO.setId(ureportFileDO.getId());
            saveReqVO.setFileName(file);
            saveReqVO.setFileContent(content);
            saveReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
            ureportFileService.updateUreportFile(saveReqVO);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean disabled() {
        return disabled;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    private String getCorrectName(String name) {
        if (name.startsWith(getPrefix())) {
            name = name.substring(getPrefix().length());
        }
        return name;
    }
}
