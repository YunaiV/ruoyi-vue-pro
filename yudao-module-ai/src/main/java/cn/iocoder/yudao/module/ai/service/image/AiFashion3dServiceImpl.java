package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashion3dConvertReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashion3dResultRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashion3dAssetDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashion3dAssetMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiFashionTaskMapper;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * AI 服装 3D 转换服务实现类
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashion3dServiceImpl implements AiFashion3dService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${yudao.ai.stable-diffusion-web-ui.base-url:http://localhost:7860}")
    private String sdBaseUrl;

    @Resource
    private AiFashion3dAssetMapper assetMapper;

    @Resource
    private AiFashionTaskMapper taskMapper;

    @Resource
    private FileApi fileApi;

    @Override
    public AiFashion3dResultRespVO convert(Long userId, AiFashion3dConvertReqVO reqVO) {
        // 1. 确定来源图片 URL
        String sourceImageUrl = resolveSourceImageUrl(reqVO);

        // 2. 创建 PROCESSING 记录
        AiFashion3dAssetDO asset = new AiFashion3dAssetDO()
                .setUserId(userId)
                .setTaskId(reqVO.getTaskId())
                .setSourceImageUrl(sourceImageUrl)
                .setGridResolution(reqVO.getGridResolution() != null ? reqVO.getGridResolution() : 64)
                .setOutputFormat(StrUtil.blankToDefault(reqVO.getOutputFormat(), "OBJ"))
                .setColorChange(reqVO.getColorChange())
                .setStatus("PROCESSING");
        assetMapper.insert(asset);
        Long assetId = asset.getId();

        // 3. 异步执行（self-call 通过 Spring proxy 触发 @Async）
        getSelf().doConvertAsync(assetId, sourceImageUrl, reqVO, userId);

        // 4. 立即返回
        AiFashion3dResultRespVO resp = new AiFashion3dResultRespVO();
        resp.setId(assetId);
        resp.setStatus("PROCESSING");
        return resp;
    }

    @Override
    public AiFashion3dResultRespVO getResult(Long assetId) {
        AiFashion3dAssetDO asset = assetMapper.selectById(assetId);
        if (asset == null) {
            AiFashion3dResultRespVO resp = new AiFashion3dResultRespVO();
            resp.setStatus("NOT_FOUND");
            return resp;
        }
        return convertDOToRespVO(asset);
    }

    @Override
    public AiFashion3dResultRespVO changeColor(Long assetId, String colorHex) {
        AiFashion3dAssetDO asset = assetMapper.selectById(assetId);
        if (asset == null) {
            AiFashion3dResultRespVO resp = new AiFashion3dResultRespVO();
            resp.setStatus("NOT_FOUND");
            return resp;
        }
        // 构建新的请求，基于原始图片重新转换
        AiFashion3dConvertReqVO reqVO = new AiFashion3dConvertReqVO();
        reqVO.setImageUrl(asset.getSourceImageUrl());
        reqVO.setColorChange(colorHex);
        reqVO.setGridResolution(asset.getGridResolution());
        reqVO.setOutputFormat(asset.getOutputFormat());
        reqVO.setGenerateGif(Boolean.TRUE);
        reqVO.setGenerateMultiAngle(Boolean.TRUE);
        return convert(asset.getUserId(), reqVO);
    }

    @Async
    public void doConvertAsync(Long assetId, String sourceImageUrl, AiFashion3dConvertReqVO reqVO, Long userId) {
        long start = System.currentTimeMillis();
        try {
            // 1. 下载源图
            byte[] srcBytes = HttpUtil.downloadBytes(sourceImageUrl);
            BufferedImage srcImage = ImageIO.read(new ByteArrayInputStream(srcBytes));
            if (srcImage == null) {
                throw new IllegalStateException("图片解码失败：" + sourceImageUrl);
            }

            // 2. 生成深度图
            BufferedImage depthImage = AiFashionMeshGenerator.estimateDepthSimple(srcImage);
            byte[] depthBytes = imageToBytes(depthImage);
            String depthMapUrl = fileApi.createFile(depthBytes);

            // 3. 颜色变换（如果有）
            BufferedImage textureImage;
            if (StrUtil.isNotBlank(reqVO.getColorChange())) {
                textureImage = AiFashion3dColorChanger.changeColor(srcImage, reqVO.getColorChange());
            } else {
                textureImage = srcImage;
            }
            byte[] textureBytes = imageToBytes(textureImage);
            String textureUrl = fileApi.createFile(textureBytes);

            // 4. 生成网格
            int gridResolution = reqVO.getGridResolution() != null ? reqVO.getGridResolution() : 64;
            AiFashionMeshData mesh = AiFashionMeshGenerator.generateFromDepthMap(depthImage, gridResolution);

            // 5. 导出 OBJ
            String objStr = AiFashionObjExporter.exportObj(mesh, "texture.png");
            String objUrl = fileApi.createFile(objStr.getBytes());

            // 6. 导出 MTL
            String mtlStr = AiFashionObjExporter.exportMtl("texture.png");
            String mtlUrl = fileApi.createFile(mtlStr.getBytes());

            // 7. 多角度预览
            List<Integer> angles = reqVO.getRotationAngles();
            if (angles == null || angles.isEmpty()) {
                angles = List.of(0, 45, 90, 135, 180, 225, 270, 315);
            }
            Map<String, String> previewUrls = new LinkedHashMap<>();
            Map<Integer, BufferedImage> angleImages = new LinkedHashMap<>();
            for (Integer angle : angles) {
                BufferedImage rotated = generateAnglePreview(textureImage, angle);
                angleImages.put(angle, rotated);
                byte[] rotBytes = imageToBytes(rotated);
                String rotUrl = fileApi.createFile(rotBytes);
                previewUrls.put(String.valueOf(angle), rotUrl);
            }
            String previewUrlsJson = OBJECT_MAPPER.writeValueAsString(previewUrls);

            // 8. 旋转合成图（sprite sheet）
            String rotationGifUrl = null;
            if (Boolean.TRUE.equals(reqVO.getGenerateGif())) {
                byte[] spriteBytes = generateSpriteSheet(new ArrayList<>(angleImages.values()));
                rotationGifUrl = fileApi.createFile(spriteBytes);
            }

            long durationMs = System.currentTimeMillis() - start;

            // 9. 更新资产
            assetMapper.updateById(new AiFashion3dAssetDO()
                    .setId(assetId)
                    .setDepthMapUrl(depthMapUrl)
                    .setTextureUrl(textureUrl)
                    .setObjFileUrl(objUrl)
                    .setMtlFileUrl(mtlUrl)
                    .setPreviewUrlsJson(previewUrlsJson)
                    .setRotationGifUrl(rotationGifUrl)
                    .setVerticesCount(mesh.getVertices().size())
                    .setFacesCount(mesh.getFaces().size())
                    .setGridResolution(gridResolution)
                    .setStatus("SUCCESS")
                    .setDurationMs(durationMs));

            log.info("[3D Convert][assetId={}] 完成，耗时 {}ms", assetId, durationMs);

        } catch (Exception e) {
            log.error("[3D Convert][assetId={}] 失败", assetId, e);
            assetMapper.updateById(new AiFashion3dAssetDO()
                    .setId(assetId)
                    .setStatus("FAIL")
                    .setErrorMessage(e.getMessage())
                    .setDurationMs(System.currentTimeMillis() - start));
        }
    }

    // ==============================
    // 私有辅助方法
    // ==============================

    /** 确定来源图片 URL */
    private String resolveSourceImageUrl(AiFashion3dConvertReqVO reqVO) {
        if (reqVO.getTaskId() != null) {
            AiFashionTaskDO task = taskMapper.selectById(reqVO.getTaskId());
            if (task != null && StrUtil.isNotBlank(task.getFinalPicUrl())) {
                return task.getFinalPicUrl();
            }
        }
        return reqVO.getImageUrl();
    }

    /** 生成单角度预览图（Java AWT 旋转） */
    private BufferedImage generateAnglePreview(BufferedImage src, int angle) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        AffineTransform at = AffineTransform.getRotateInstance(
                Math.toRadians(angle), w / 2.0, h / 2.0);
        g2d.drawImage(src, at, null);
        g2d.dispose();
        return result;
    }

    /** 生成多角度合成图（sprite sheet） */
    private byte[] generateSpriteSheet(List<BufferedImage> frames) throws IOException {
        if (frames.isEmpty()) {
            return new byte[0];
        }
        int cols = (int) Math.ceil(Math.sqrt(frames.size()));
        int rows = (int) Math.ceil((double) frames.size() / cols);
        int fw = frames.get(0).getWidth();
        int fh = frames.get(0).getHeight();
        int sheetW = cols * fw;
        int sheetH = rows * fh;

        BufferedImage sheet = new BufferedImage(sheetW, sheetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = sheet.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, sheetW, sheetH);
        for (int i = 0; i < frames.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            g2d.drawImage(frames.get(i), col * fw, row * fh, null);
        }
        g2d.dispose();
        return imageToBytes(sheet);
    }

    /** BufferedImage → PNG bytes */
    private byte[] imageToBytes(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    /** DO → RespVO */
    private AiFashion3dResultRespVO convertDOToRespVO(AiFashion3dAssetDO asset) {
        AiFashion3dResultRespVO resp = new AiFashion3dResultRespVO();
        resp.setId(asset.getId());
        resp.setStatus(asset.getStatus());
        resp.setObjFileUrl(asset.getObjFileUrl());
        resp.setMtlFileUrl(asset.getMtlFileUrl());
        resp.setTextureUrl(asset.getTextureUrl());
        resp.setDepthMapUrl(asset.getDepthMapUrl());
        resp.setRotationGifUrl(asset.getRotationGifUrl());
        resp.setVerticesCount(asset.getVerticesCount());
        resp.setFacesCount(asset.getFacesCount());
        resp.setGridResolution(asset.getGridResolution());
        resp.setColorChange(asset.getColorChange());
        resp.setDurationMs(asset.getDurationMs());
        resp.setErrorMessage(asset.getErrorMessage());
        // 解析 previewUrlsJson
        if (StrUtil.isNotBlank(asset.getPreviewUrlsJson())) {
            try {
                Map<String, String> previewUrls = OBJECT_MAPPER.readValue(
                        asset.getPreviewUrlsJson(), new TypeReference<Map<String, String>>() {});
                resp.setPreviewUrls(previewUrls);
            } catch (Exception e) {
                log.warn("[3D Convert] previewUrlsJson 解析失败: {}", asset.getPreviewUrlsJson(), e);
            }
        }
        return resp;
    }

    /** 获取 Spring 代理自身（使 @Async 生效） */
    private AiFashion3dServiceImpl getSelf() {
        return cn.hutool.extra.spring.SpringUtil.getBean(AiFashion3dServiceImpl.class);
    }

}
