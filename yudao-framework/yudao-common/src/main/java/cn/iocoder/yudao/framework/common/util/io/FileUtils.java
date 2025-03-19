package cn.iocoder.yudao.framework.common.util.io;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.system.OsInfo;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * 文件工具类
 *
 * @author 芋道源码
 */
@Slf4j
public class FileUtils {

    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile(String data) {
        File file = createTempFile();
        // 写入内容
        FileUtil.writeUtf8String(data, file);
        return file;
    }

    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile(byte[] data) {
        File file = createTempFile();
        // 写入内容
        FileUtil.writeBytes(data, file);
        return file;
    }

    /**
     * 创建临时文件，无内容
     * 该文件会在 JVM 退出时，进行删除
     *
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile() {
        // 创建文件，通过 UUID 保证唯一
        File file = File.createTempFile(IdUtil.simpleUUID(), null);
        // 标记 JVM 退出时，自动删除
        file.deleteOnExit();
        return file;
    }

    /**
     * 生成文件路径
     *
     * @param content      文件内容
     * @param originalName 原始文件名
     * @return path，唯一不可重复
     */
    public static String generatePath(byte[] content, String originalName) {
        String sha256Hex = DigestUtil.sha256Hex(content);
        // 情况一：如果存在 name，则优先使用 name 的后缀
        if (StrUtil.isNotBlank(originalName)) {
            String extName = FileNameUtil.extName(originalName);
            return StrUtil.isBlank(extName) ? sha256Hex : sha256Hex + "." + extName;
        }
        // 情况二：基于 content 计算
        return sha256Hex + '.' + FileTypeUtil.getType(new ByteArrayInputStream(content));
    }

    /**
     * 获得  file 相对于 baseDir 的路径
     * @param baseDir  基础路径
     * @param file 完整路径
     * @return 相对路径
     * */
    public static String getRelativePath(File baseDir,File file)
    {
        String base=baseDir.getAbsolutePath();
        String full=file.getAbsolutePath();
        if(!full.startsWith(base)) {
            throw new IllegalArgumentException(full + " is not sub of "+base);
        }
        return full.substring(base.length());
    }

    /**
     * 获得相对于指定基础路径的文件
     */
    public static File resolveByPath(String basicDirPath, String... part) {
        part = ArrayUtils.unshift(part, basicDirPath);
        return  new File(StrUtils.joinPath(part));
    }

    /**
     * 获得相对于指定基础路径的文件
     */
    public static File resolveByPath(File basicDir, String... part) {
        return resolveByPath(basicDir.getAbsolutePath(), part);
    }

    /**
     * 获得相对于指定类所在目录的文件
     *
     * @param cls 类
     * @return 类所在的路径
     */
    public static File resolveByClass(Class cls, String... part) {
        File dir = resolveByClass(cls);
        return resolveByPath(dir.getParentFile(), part);
    }

    /**
     * 获得类所在的路径
     *
     * @param cls 类
     * @return 类所在的路径
     */
    public static File resolveByClass(Class cls) {

        String strURL = "";
        try {
            String strClassName = cls.getName();
            String strPackageName = "";
            if (cls.getPackage() != null) {
                strPackageName = cls.getPackage().getName();
            }

            String strClassFileName = "";
            if (!"".equals(strPackageName)) {
                strClassFileName = strClassName.substring(strPackageName.length() + 1, strClassName.length());
            } else {
                strClassFileName = strClassName;
            }
            try {
                URL url = cls.getResource(strClassFileName + ".class");
                if(url!=null) {
                    strURL = url.toString();
                } else {
                    return null;
                }
            } catch (Exception e){
                log.error("path resolve error",e);
                return null;
            }

            OsInfo os= new OsInfo();

            String _strURL=StrUtils.removeFirst(strURL, "file:/");
            if(!os.isWindows()) {
                _strURL="/"+_strURL;
            }
            File f=new File(_strURL);
            if(f.exists() && f.isFile()) {
                return f;
            }



            try {
                strURL = java.net.URLDecoder.decode(strURL, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("path resolve error", e);
            }

            return new File(strURL);
        } catch (Exception e) {
            log.error("path resolve error", e);
            return null;
        }

    }

    public static String changeExtName(String rel, String newExtName) {
        int i=rel.lastIndexOf(".");
        newExtName=StrUtils.removeFirst(newExtName, ".");
        if(i!=-1) {
            rel=rel.substring(0,i);
            return rel+"."+newExtName;
        } else {
            return rel+"."+newExtName;
        }
    }
}
