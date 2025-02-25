package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.somle.eccang.model.EccangOrderVO;
import com.somle.esb.model.OssData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EccangOrderPlatformShipDataJob extends EccangDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var reqVO = EccangOrderVO.builder()
            .condition(EccangOrderVO.Condition.builder()
                .platformShipDateStart(beforeYesterdayFirstSecond)
                .platformShipDateEnd(beforeYesterdayLastSecond)
                .build())
            .build();

        log.info(reqVO.toString());
        log.info(JsonUtils.toJsonString(reqVO));

        eccangService.getOrderPlusArchivePages(
                reqVO,
                beforeYesterday.getYear()
            )
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_platform_ship")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        return "data upload success";
    }
}