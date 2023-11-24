package cn.iocoder.yudao.module.report.framework.ureport.core;

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

// TODO @赤焰：有了这个功能，是不是就不需要 UreportFileController？
/**
 * 基于数据库的 {@link ReportProvider} 实现类
 *
 * @author 赤焰
 */
@Component
@Slf4j
@Setter
@ConfigurationProperties(prefix = "ureport.provider.database") // TODO @赤焰：是不是单独搞个 UReportProperties 用来注入 prefix、disabled 等属性
public class UreportDatabaseProvider implements ReportProvider {

    private static final String NAME = "mysql-provider";

    // TODO @赤焰：IdTypeEnvironmentPostProcessor 参考下，通过 primary 数据源获取
    private String prefix = "mysql:";

    private boolean disabled = false;

    @Resource
    private UreportFileService ureportFileService;

    @Override
    public InputStream loadReport(String file) {
        // TODO @赤焰：是不是不需要这个 check 接口？直接使用 name 查询就 ok 了？
        // TODO @赤焰：变量的命名要注意，这里 reportDo 是不是叫 count 就好了？还有，这个方法的命名也不太对。
        Long reportDo = ureportFileService.checkExistByName(getCorrectName(file));
        if (reportDo <=0) { // TODO 赤焰：<= 0 注释空格
            throw exception(UREPORT_FILE_NOT_EXISTS);
        }
        // TODO @赤焰：queryUreportFileDoByName 改成 getUreportFileDoByName 更合适，保持整个项目的命名风格一致性。
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
        // TODO @赤焰：queryReportFileList 改成 getReportFileList 更合适，保持整个项目的命名风格一致性。
        List<UreportFileDO> list = ureportFileService.queryReportFileList();
        List<ReportFile> reportList = new ArrayList<>();
        if(CollUtil.isEmpty(list)){ // TODO 赤焰：CollUtil.isEmpty(list) 注意空格
            // TODO @赤焰：这里可以直接返回 Collections.emptyList();
            return reportList;
        }
        // TODO 赤焰：可以使用 CollectionUtils.converList
        for (UreportFileDO reportFile : list) {
            // TODO 赤焰：可以使用          DateUtils.of() 么？
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
        // TODO @赤焰：这里的逻辑，应该封装到 ureportFileService 中，这里只负责调用即可
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

    // TODO @赤焰：这个方法的注释，最好写下哈；
    private String getCorrectName(String name) {
        // TODO  赤焰：一些常用方法，可以用 hutool StrUtil.removePrefix()
        if (name.startsWith(getPrefix())) {
            name = name.substring(getPrefix().length());
        }
        return name;
    }

}
