package cn.iocoder.yudao.module.report.framework.ureport2.core.provider;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport2.UReport2FileDO;
import cn.iocoder.yudao.module.report.framework.ureport2.core.cache.UReport2ReportDefinitionCache;
import cn.iocoder.yudao.module.report.service.ureport2.UReport2FileService;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.UREPORT2_FILE_NOT_EXISTS;

/**
 * 基于数据库的 UReport2 {@link ReportProvider} 实现类
 *
 * 模板保存在 report_ureport_file 表，按当前租户隔离；模板名称带有 {@link #PREFIX} 前缀，例如说 db:demo.ureport.xml
 *
 * 模板名称的规范化（见 UReport2FileNameUtils）由 Service、模板缓存各自完成，本类直接传递 UReport2 给出的原始名称
 *
 * 注意：设计器加载存储列表时，会把本对象序列化成 JSON，所以不要增加其它 getter 方法
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class UReport2DatabaseReportProvider implements ReportProvider {

    public static final String PREFIX = "db:";

    private static final String NAME = "数据库";

    private final UReport2FileService ureport2FileService;
    private final UReport2ReportDefinitionCache reportDefinitionCache;

    @Override
    public InputStream loadReport(String file) {
        UReport2FileDO reportFile = ureport2FileService.getFileByName(removePrefix(file));
        if (reportFile == null) {
            throw exception(UREPORT2_FILE_NOT_EXISTS);
        }
        return new ByteArrayInputStream(reportFile.getContent().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void deleteReport(String file) {
        // 删除模板，并发布新的版本号，让所有实例的缓存失效；本实例的缓存，由 Service 在删除时移除，与管理后台的删除一致
        ureport2FileService.deleteFileByName(removePrefix(file));
    }

    @Override
    public List<ReportFile> getReportFiles() {
        return convertList(ureport2FileService.getFileList(),
                reportFile -> new ReportFile(reportFile.getName(), DateUtils.of(reportFile.getUpdateTime())));
    }

    @Override
    public void saveReport(String file, String content) {
        // 保存模板，并发布新的版本号，让所有实例的缓存失效
        String version = ureport2FileService.saveFile(new UReport2FileSaveReqVO().setName(removePrefix(file)).setContent(content));
        // 设计器随后会缓存本次解析的模板定义，所以使用本次保存发布的版本号，不能重新读取 Redis 中的最新版本号
        reportDefinitionCache.onFileSaved(file, version);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    private static String removePrefix(String file) {
        return StrUtil.removePrefix(file, PREFIX);
    }

}
