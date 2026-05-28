package cn.iocoder.yudao.module.yaya.controller.app.practice.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaAppFavoriteCreateReqVO {

    @NotNull
    private Long topicId;

}
