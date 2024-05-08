package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * mj 保存 components 记录
 *
 *  "components": [
 *           {
 *             "custom_id": "MJ::JOB::upsample::1::5d32f4e8-8d2f-4bef-82d8-bf517e3c3660",
 *             "style": 2,
 *             "label": "U1",
 *             "type": 2
 *           },
 *     ]
 *
 * @author fansili
 * @time 2024/5/8 14:44
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiImageMidjourneyOperationsVO {

    private String custom_id;
    private String style;
    private String label;
    private String type;
}
