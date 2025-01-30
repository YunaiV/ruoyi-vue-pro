//package com.somle.esb.job;
//
//import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
//import com.somle.amazon.model.AmazonShop;
//import com.somle.esb.model.OssData;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayInputStream;
//import java.time.LocalDate;
//import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//public class AmazonspReturnReportDataByFBAJob extends AmazonspDataJob {
//
//    @Override
//    public String execute(String param) throws Exception {
//        // 假设：刷新 token
////        refreshToken();
//
//        // 查询并按 regionCode 分组所有的 shops
//        Map<String, List<AmazonShop>> regionShopsMap = amazonService.spClient.getShops()
//                .collect(Collectors.groupingBy(shop -> shop.getCountry().getRegion().getCode()));
//
//        // 获取昨天的时间范围
//        String[] timeRange = getYesterdayUtcTimeRange();
//
//        // 遍历每个 regionCode，获取该 regionCode 下的所有 marketplaceIds，单独发送请求
//        regionShopsMap.forEach((regionCode, shops) -> shops.forEach(shop -> {
//            String marketplaceId = shop.getCountry().getMarketplaceId();
//
//            // 构建报告请求对象
//            AmazonSpReportSaveVO vo = AmazonSpReportSaveVO.builder()
//                    .reportType("GET_FBA_FULFILLMENT_CUSTOMER_RETURNS_DATA")
//                    .marketplaceIds(Collections.singletonList(marketplaceId))  // 单独处理一个 marketplaceId
//                    .dataStartTime(timeRange[0]).dataEndTime(timeRange[1]).build();
//
//            log.info("获取FBA退货单,开始日期:{},结束日期:{}, marketplaceId: {}", timeRange[0], timeRange[1], marketplaceId);
//
//            try {
//                // 获取报告内容
//                String reportString = amazonService.spClient.createAndGetReport(shop.getSeller(), vo, String.valueOf(false));
//
//                // 文件夹日期
//                LocalDate folderName = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).toLocalDate();
//
//                // 创建上传数据对象
//                OssData data = OssData.builder().database(DATABASE)
//                        .tableName("fba_return_report")
//                        .syncType("inc")
//                        .requestTimestamp(System.currentTimeMillis())
//                        .folderDate(folderName)
//                        .content(new ByteArrayInputStream(reportString.getBytes()))
//                        .headers(getHeaders(vo))
//                        .build();
//
//                // 上传数据到 OSS
//                service.send(data);
//
//            } catch (Exception e) {
//                // 错误处理
//                log.error("获取报告或上传失败, marketplaceId: {} - {}", marketplaceId, e.getMessage());
//                throw e;
//            }
//        }));
//
//        return "data upload success";
//    }
//
//
//    private static String[] getYesterdayUtcTimeRange() {
//        // 获取当前的 UTC 时间
//        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
//
//        // 获取昨天的开始时间 (00:00:00)
//        ZonedDateTime yesterdayStart = nowUtc.minusDays(1).toLocalDate().atStartOfDay(ZoneOffset.UTC);
//
//        // 获取昨天的结束时间 (23:59:59)
//        ZonedDateTime yesterdayEnd = yesterdayStart.plusDays(1).minusSeconds(1);
//
//        // 返回开始时间和结束时间的字符串表示
//        return new String[]{yesterdayStart.toString(),  // 开始时间
//                yesterdayEnd.toString()     // 结束时间
//        };
//    }
//
//    private static Map<String, String> getHeaders(AmazonSpReportSaveVO vo) {
//        Map<String, String> headers = new HashMap<>();
//        //oss文档要求添加x-oss-meta-前缀
//        headers.put("gReportType", vo.getReportType());
//        headers.put("marketplaceIds", vo.getMarketplaceIds().toString());
//        headers.put("dataStartTime", vo.getDataStartTime());
//        headers.put("dataEndTime", vo.getDataEndTime());
//        return headers;
//    }
//}
