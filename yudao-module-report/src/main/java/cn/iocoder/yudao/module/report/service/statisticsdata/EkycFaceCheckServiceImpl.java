package cn.iocoder.yudao.module.report.service.statisticsdata;

import cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck.vo.EkycChannelStatusCountDTO;
import cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck.vo.EkycChannelStatusSummaryRespVO;
import cn.iocoder.yudao.module.report.dal.mysql.statisticsdata.EkycFaceCheckMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 通用统计数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class EkycFaceCheckServiceImpl implements EkycFaceCheckService {

    @Resource
    private EkycFaceCheckMapper ekycFaceCheckMapper;

    @Override
    public EkycChannelStatusSummaryRespVO getChannelStatusSummary(LocalDate startDate, LocalDate endDate) {
        List<EkycChannelStatusCountDTO> rawStats = ekycFaceCheckMapper.selectChannelStatusSummary(startDate, endDate);

        // 初始化状态标签
        List<String> statusLabels = List.of("成功", "失败", "待处理", "异常");
        Map<String, List<Integer>> statusSeries = new LinkedHashMap<>();
        for (String label : statusLabels) {
            statusSeries.put(label, new ArrayList<>());
        }

        // 日期格式化器（裁剪掉时分秒）
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 按日期聚合状态数量
        Set<String> dates = new LinkedHashSet<>();
        Map<String, Map<String, Integer>> grouped = new HashMap<>();

        for (EkycChannelStatusCountDTO row : rawStats) {
            String rawDate = row.getDate(); // e.g. "2025-09-11 00:00:00"
            String date = LocalDateTime.parse(rawDate, inputFormat).format(outputFormat); // → "2025-09-11"
            String statusLabel = mapStatus(row.getStatus());
            Integer count = row.getCount();

            dates.add(date);
            grouped.computeIfAbsent(date, k -> new HashMap<>());
            grouped.get(date).put(statusLabel, count);
        }

        // 按顺序填充每个日期的状态数据
        for (String date : dates) {
            Map<String, Integer> counts = grouped.getOrDefault(date, Collections.emptyMap());
            for (String label : statusLabels) {
                statusSeries.get(label).add(counts.getOrDefault(label, 0));
            }
        }

        return new EkycChannelStatusSummaryRespVO(new ArrayList<>(dates), statusSeries);
    }

    private String mapStatus(Byte status) {
        return switch (status) {
            case 1 -> "成功";
            case 2 -> "失败";
            case 3 -> "待处理";
            case 4 -> "异常";
            default -> "未知";
        };
    }

}
