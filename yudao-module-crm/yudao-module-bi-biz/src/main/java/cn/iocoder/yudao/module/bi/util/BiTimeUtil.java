package cn.iocoder.yudao.module.bi.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiParams;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * bi时间工具类
 *
 * @author anhaohao
 */
public class BiTimeUtil {

    public static BiTimeEntity analyzeType(BiParams biParams) {
        // 解析时间
        BiTimeEntity biTimeEntity = analyzeTime(biParams);
        // 解析权限
        biTimeEntity.setUserIds(analyzeAuth(biParams));
        return biTimeEntity;
    }

    /**
     * 解析权限
     *
     * @param biParams bi参数
     * @return List<Long>
     */
    public static List<Long> analyzeAuth(BiParams biParams) {
        List<Long> userIdList = new ArrayList<>();
        Long deptId = biParams.getDeptId();
        Long userId = biParams.getUserId();
        Integer isUser = biParams.getIsUser();
        // 获取部门和用户的api
        DeptApi deptApi = SpringUtil.getBean("deptApiImpl");
        AdminUserApi adminUserApi = SpringUtil.getBean("adminUserApiImpl");
        // 0.部门 1.用户
        if (isUser == 0) {
            if (deptId == null) {
                deptId = adminUserApi.getUser(SecurityFrameworkUtils.getLoginUserId()).getDeptId();
            }
            List<DeptRespDTO> childDeptList = deptApi.getChildDeptList(deptId);
            List<Long> deptIds = new ArrayList<>();
            deptIds.add(deptId);
            if (childDeptList != null && !childDeptList.isEmpty()) {
                for (DeptRespDTO deptRespDTO : childDeptList) {
                    deptIds.add(deptRespDTO.getId());
                }
            }
            // 获取部门下的用户
            adminUserApi.getUserListByDeptIds(deptIds).forEach(adminUserRespDTO -> userIdList.add(adminUserRespDTO.getId()));
        } else {
            if (userId == null) {
                List<AdminUserRespDTO> userListBySubordinate = adminUserApi.getUserListBySubordinate(SecurityFrameworkUtils.getLoginUserId());
                userListBySubordinate.forEach(adminUserRespDTO -> userIdList.add(adminUserRespDTO.getId()));
            } else {
                userIdList.add(userId);
            }
        }
        return userIdList;
    }


    /**
     * 解析时间
     *
     * @param biParams bi参数
     * @return BiTimeEntity
     */
    public static BiTimeEntity analyzeTime(BiParams biParams) {
        Date beginDate = DateUtil.date();
        Date endDate = DateUtil.date();
        int cycleNum = 12;
        String sqlDateFormat = "%Y%m";
        String dateFormat = "yyyyMM";
        String type = biParams.getType();
        String startTime = biParams.getStartTime();
        String endTime = biParams.getEndTime();
        if (StrUtil.isNotEmpty(type)) {
            //1.今天 2.昨天 3.本周 4.上周 5.本月 6.上月 7.本季度 8.上季度 9.本年 10 上年
            switch (type) {
                case "1":
                    beginDate = DateUtil.beginOfDay(DateUtil.date());
                    endDate = DateUtil.endOfDay(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "2":
                    beginDate = DateUtil.beginOfDay(new Date(System.currentTimeMillis() - 86400000));
                    endDate = DateUtil.endOfDay(new Date(System.currentTimeMillis() - 86400000));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "3":
                    beginDate = DateUtil.beginOfWeek(DateUtil.date());
                    endDate = DateUtil.endOfWeek(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "4":
                    beginDate = DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    endDate = DateUtil.endOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "5":
                    beginDate = DateUtil.beginOfMonth(DateUtil.date());
                    endDate = DateUtil.endOfMonth(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "6":
                    beginDate = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    endDate = DateUtil.endOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "7":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.date());
                    endDate = DateUtil.endOfQuarter(DateUtil.date());
                    cycleNum = 3;
                    break;
                case "8":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -3));
                    endDate = DateUtil.endOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -3));
                    cycleNum = 3;
                    break;
                case "9":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    break;
                case "10":
                    beginDate = DateUtil.beginOfYear(DateUtil.offsetMonth(DateUtil.date(), -12));
                    endDate = DateUtil.endOfYear(DateUtil.offsetMonth(DateUtil.date(), -12));
                    break;
                default:
                    break;
            }
        } else if (StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime)) {
            Date start;
            Date end;
            if (startTime.length() == 6) {
                start = DateUtil.parse(startTime, "yyyyMM");
                end = DateUtil.endOfMonth(DateUtil.parse(endTime, "yyyyMM"));
            } else {
                start = DateUtil.parse(startTime);
                end = DateUtil.parse(endTime);
            }
            Integer startMonth = Integer.valueOf(DateUtil.format(start, "yyyyMM"));
            int endMonth = Integer.parseInt(DateUtil.format(end, "yyyyMM"));
            if (startMonth.equals(endMonth)) {
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                long diffDay = DateUtil.between(start, end, DateUnit.DAY);
                cycleNum = (int) diffDay + 1;
            } else {
                sqlDateFormat = "%Y%m";
                dateFormat = "yyyyMM";
                int diffYear = Integer.parseInt(Integer.toString(endMonth).substring(0, 4)) - Integer.parseInt(startMonth.toString().substring(0, 4));
                int diffMonth = endMonth % 100 - startMonth % 100 + 1;
                cycleNum = diffYear * 12 + diffMonth;
            }
            beginDate = start;
            endDate = end;
        }
        Integer beginTime = Integer.valueOf(DateUtil.format(beginDate, dateFormat));
        Integer finalTime = Integer.valueOf(DateUtil.format(endDate, dateFormat));
        return new BiTimeEntity(sqlDateFormat, dateFormat, beginDate, endDate, cycleNum, beginTime, finalTime, new ArrayList<>());
    }

    @Data
    @Accessors(chain = true)
    public static class BiTimeEntity {
        /**
         * sql日期格式化
         */
        private String sqlDateFormat;

        /**
         * 日期格式化
         */
        private String dateFormat;

        /**
         * 开始时间
         */
        private Date beginDate;

        /**
         * 结束时间
         */
        private Date endDate;

        /**
         * 周期
         */
        private Integer cycleNum;

        /**
         * 开始时间 字符串格式 如20200101
         */
        private Integer beginTime;

        /**
         * 结束时间 字符串格式 如20200101
         */
        private Integer finalTime;

        /**
         * user列表
         */
        private List<Long> userIds = new ArrayList<>();
        private Integer page;
        private Integer limit;

        public BiTimeEntity(String sqlDateFormat, String dateFormat, Date beginDate, Date endDate, Integer cycleNum, Integer beginTime, Integer finalTime, List<Long> userIds) {
            this.sqlDateFormat = sqlDateFormat;
            this.dateFormat = dateFormat;
            this.beginDate = beginDate;
            this.endDate = endDate;
            this.cycleNum = cycleNum;
            this.beginTime = beginTime;
            this.finalTime = finalTime;
            this.userIds = userIds;
        }

        public BiTimeEntity() {
        }

    }
}
