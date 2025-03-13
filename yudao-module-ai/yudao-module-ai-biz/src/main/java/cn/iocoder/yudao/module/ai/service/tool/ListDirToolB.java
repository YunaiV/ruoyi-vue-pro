package cn.iocoder.yudao.module.ai.service.tool;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 目录内容列表工具：列出指定目录的内容
 *
 * @author 芋道源码
 */
@Component("listDir")
public class ListDirToolB implements Function<ListDirToolB.Request, ListDirToolB.Response> {

    @Data
    @JsonClassDescription("列出指定目录的内容")
    public static class Request {

        /**
         * 要列出内容的目录路径
         */
        @JsonPropertyDescription("要列出内容的目录路径，例如说：/Users/yunai")
        private String path;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        /**
         * 目录内容列表
         */
        private List<File> files;

        @Data
        public static class File {

            /**
             * 是否为目录
             */
            private Boolean directory;

            /**
             * 名称
             */
            private String name;

            /**
             * 大小，仅对文件有效
             */
            private String size;

            /**
             * 最后修改时间
             */
            private String lastModified;

        }

    }

    @Override
    public Response apply(Request request) {
        // 校验目录存在
        String path = StrUtil.blankToDefault(request.getPath(), ".");
        Path dirPath = Paths.get(path);
        if (!FileUtil.exist(dirPath.toString()) || !FileUtil.isDirectory(dirPath.toString())) {
            return new Response(Collections.emptyList());
        }
        // 列出目录内容
        File[] files = dirPath.toFile().listFiles();
        if (ArrayUtil.isEmpty(files)) {
            return new Response(Collections.emptyList());
        }
        return new Response(convertList(Arrays.asList(files), file ->
                new Response.File().setDirectory(file.isDirectory()).setName(file.getName())
                        .setLastModified(LocalDateTimeUtil.format(LocalDateTimeUtil.of(file.lastModified()), NORM_DATETIME_PATTERN))));
    }

}