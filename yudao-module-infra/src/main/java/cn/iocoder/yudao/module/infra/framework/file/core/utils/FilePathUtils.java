package cn.iocoder.yudao.module.infra.framework.file.core.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_PATH_INVALID;

/**
 * 文件路径工具类
 *
 * @author 芋道源码
 */
public class FilePathUtils {

    private FilePathUtils() {
    }

    /**
     * 校验文件名是否合法，禁止携带目录路径。
     *
     * @param name 文件名
     * @return 文件名
     */
    public static String validateFileName(String name) {
        if (StrUtil.isEmpty(name)) {
            return name;
        }
        if (!isPathValid(name) || StrUtil.contains(name, StrUtil.SLASH) || !StrUtil.equals(name, FileUtil.getName(name))) {
            throw exception(FILE_PATH_INVALID);
        }
        return name;
    }

    /**
     * 校验文件目录是否合法。
     *
     * @param directory 文件目录，允许为空
     * @return 是否合法
     */
    public static boolean isDirectoryValid(String directory) {
        return StrUtil.isEmpty(directory) || isPathValid(directory);
    }

    /**
     * 校验文件目录是否合法，不合法时抛出业务异常。
     *
     * @param directory 文件目录，允许为空
     */
    public static void validateDirectory(String directory) {
        if (!isDirectoryValid(directory)) {
            throw exception(FILE_PATH_INVALID);
        }
    }

    /**
     * 校验文件相对路径是否合法，不合法时抛出业务异常。
     *
     * @param path 文件相对路径
     */
    public static void validatePath(String path) {
        if (StrUtil.isEmpty(path) || !isPathValid(path)) {
            throw exception(FILE_PATH_INVALID);
        }
    }

    /**
     * 校验路径是否为安全的相对路径，禁止绝对路径、Windows 盘符、反斜杠、空路径段和目录穿越。
     *
     * @param path 路径
     * @return 是否合法
     */
    private static boolean isPathValid(String path) {
        // 不能以 / 或 \ 开头，避免传入绝对路径
        if (StrUtil.startWithAny(path, StrUtil.SLASH, "\\")) {
            return false;
        }
        // 不能包含反斜杠或空字符，避免绕过不同系统的路径解析
        if (StrUtil.contains(path, "\\") || path.indexOf('\0') >= 0) {
            return false;
        }
        // 不能是 Windows 盘符路径，例如 C:/test.jpg
        if (path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            return false;
        }
        try {
            // 使用 JDK Path 再兜底判断一次绝对路径
            if (Paths.get(path).isAbsolute()) {
                return false;
            }
        } catch (InvalidPathException ex) {
            return false;
        }
        // 不能包含空路径段、当前目录或上级目录，避免目录穿越
        for (String segment : path.split(StrUtil.SLASH, -1)) {
            if (StrUtil.isEmpty(segment) || ".".equals(segment) || "..".equals(segment)) {
                return false;
            }
        }
        return true;
    }

}
