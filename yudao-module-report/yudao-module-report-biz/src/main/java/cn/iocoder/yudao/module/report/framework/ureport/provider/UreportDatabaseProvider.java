/*
package cn.iocoder.yudao.module.report.framework.ureport.provider;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import cn.iocoder.yudao.module.report.dal.mysql.ureport.UreportFileMapper;
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

*/
/**
 * 提供数据库的存储方式
 * @author 赤焰
 *//*

@Slf4j
@Component
@ConfigurationProperties(prefix = "ureport.provider.database")
public class UreportDatabaseProvider implements ReportProvider{

    private static final String NAME = "数据库存储系统";

    private String prefix = "db:";

    private boolean disabled;

    @Resource
    private UreportFileMapper ureportFileMapper;

    @Override
    public InputStream loadReport(String file) {
        LambdaQueryWrapper<UreportFileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UreportFileDO::getFileName, getCorrectName(file));
        UreportFileDO reportDo = ureportFileMapper.selectOne(queryWrapper);
        if (reportDo == null) {
            throw exception(UREPORT_FILE_NOT_EXISTS);
        }
        byte[] content = reportDo.getFileContent();
        return new ByteArrayInputStream(content);
    }

    @Override
    public void deleteReport(String file) {
        LambdaQueryWrapper<UreportFileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UreportFileDO::getFileName, getCorrectName(file));
        ureportFileMapper.delete(queryWrapper);
    }

    @Override
    public List<ReportFile> getReportFiles() {
        LambdaQueryWrapper<UreportFileDO> reportFileQueryWrapper = new LambdaQueryWrapper<>();
        List<UreportFileDO> list = ureportFileMapper.selectList(reportFileQueryWrapper);
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
        Long count = ureportFileMapper.selectCount(queryWrapper);
        if(count>1){
            throw exception(UREPORT_FILE_EXISTS);
        }
        UreportFileDO ureportFileDO = ureportFileMapper.selectOne(queryWrapper);
        if (ureportFileDO == null) {
            ureportFileDO = new UreportFileDO();
            ureportFileDO.setFileName(file);
            ureportFileDO.setFileContent(content.getBytes());
            ureportFileMapper.insert(ureportFileDO);
        } else {
            ureportFileDO.setFileContent(content.getBytes());
            ureportFileMapper.update(ureportFileDO, queryWrapper);
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
        if (name.startsWith(prefix)) {
            name = name.substring(prefix.length(), name.length());
        }
        return name;
    }
}
*/
