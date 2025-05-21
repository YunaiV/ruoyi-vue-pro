package cn.iocoder.yudao.module.infra.framework.file.core.client.s3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件预签名地址 Response DTO
 *
 * @author owen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilePresignedUrlRespDTO {

    /**
     * 文件上传 URL（用于上传）
     *
     * 例如说：
     */
    private String uploadUrl;

    /**
     * 文件 URL（用于读取、下载等）
     */
    private String url;

}
