package cn.iocoder.yudao;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 项目修改器，一键替换 Maven 的 groupId、artifactId，项目的 package 等
 * <p>
 * 通过修改 groupIdNew、artifactIdNew、projectBaseDirNew 三个变量
 *
 * @author 芋道源码
 */
@Slf4j
public class ProjectReactor {

    private static final String GROUP_ID = "cn.iocoder.boot";
    private static final String ARTIFACT_ID = "yudao";
    private static final String PACKAGE_NAME = "cn.iocoder.yudao";
    private static final String TITLE = "欣辰装点猫管理系统";

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        String projectBaseDir = getProjectBaseDir();

        // ========== 配置，需要你手动修改 ==========
        String groupIdNew = "cn.huwing.boot";
        String artifactIdNew = "zdm";
        String packageNameNew = "cn.huwing.zdm";
        String titleNew = "欣辰装点猫管理系统";
        String projectBaseDirNew = projectBaseDir + "-new"; // 一键改名后，“新”项目所在的目录
        log.info("[main][新项目路径地址]projectBaseDirNew: " + projectBaseDirNew);

        // 获得需要复制的文件
        log.info("[main][开始获得需要重写的文件]");
        Collection<File> files = listFiles(projectBaseDir);
        log.info("[main][需要重写的文件数量：{}，预计需要 5-10 秒]", files.size());
        // 写入文件
        files.forEach(file -> {
            String content = replaceFileContent(file, groupIdNew, artifactIdNew, packageNameNew, titleNew);
            writeFile(file, content, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew);
        });
        long end = System.currentTimeMillis();
        log.info("[main][重写完成]共耗时：" + (end - start) / 1000 + "秒");
    }

    private static String getProjectBaseDir() {
        return StrUtil.subBefore(new File(ProjectReactor.class.getClassLoader().getResource(File.separator).getFile()).getPath(), "\\yudao-server", false);
    }

    private static Collection<File> listFiles(String projectBaseDir) {
        Collection<File> files = FileUtils.listFiles(new File(projectBaseDir), null, true);
        // 移除 IDEA  Git GitHub 自身的文件; Node 编译出来的文件
        files = files.stream()
                .filter(file -> !file.getPath().contains("\\target\\")
                        && !file.getPath().contains("\\node_modules\\")
                        && !file.getPath().contains("\\.idea\\")
                        && !file.getPath().contains("\\.git\\")
                        && !file.getPath().contains("\\.github\\")
                        && !file.getPath().contains("\\dist\\"))
                .collect(Collectors.toList());
        return files;
    }

    private static String replaceFileContent(File file, String groupIdNew,
                                             String artifactIdNew, String packageNameNew,
                                             String titleNew) {
        return FileUtil.readString(file, StandardCharsets.UTF_8)
                .replaceAll(GROUP_ID, groupIdNew)
                .replaceAll(PACKAGE_NAME, packageNameNew)
                .replaceAll(ARTIFACT_ID, artifactIdNew) // 必须放在最后替换，因为 ARTIFACT_ID 太短！
                .replaceAll(StrUtil.upperFirst(ARTIFACT_ID), StrUtil.upperFirst(artifactIdNew))
                .replaceAll(TITLE, titleNew);
    }

    private static void writeFile(File file, String fileContent, String projectBaseDir,
                                  String projectBaseDirNew, String packageNameNew, String artifactIdNew) {
        String newPath = file.getPath().replace(projectBaseDir, projectBaseDirNew) // 新目录
                .replace(PACKAGE_NAME.replaceAll("\\.", Matcher.quoteReplacement(File.separator)),
                        packageNameNew.replaceAll("\\.", Matcher.quoteReplacement(File.separator)))
                .replace(ARTIFACT_ID, artifactIdNew) //
                .replaceAll(StrUtil.upperFirst(ARTIFACT_ID), StrUtil.upperFirst(artifactIdNew));
        FileUtil.writeUtf8String(fileContent, newPath);
    }
}