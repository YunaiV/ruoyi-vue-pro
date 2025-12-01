import type { MpStatisticsApi } from '#/api/mp/statistics';

/** 用户增减数据图表配置项 */
export function userSummaryOption(
  data: MpStatisticsApi.StatisticsUserSummaryRespVO[],
  dates: string[],
): any {
  return {
    color: ['#67C23A', '#E5323E'],
    legend: {
      data: ['新增用户', '取消关注的用户'],
    },
    tooltip: {},
    xAxis: {
      data: dates,
    },
    yAxis: {
      minInterval: 1,
    },
    series: [
      {
        name: '新增用户',
        type: 'bar',
        label: {
          show: true,
        },
        barGap: 0,
        data: data.map((item) => item.newUser), // 新增用户的数据
      },
      {
        name: '取消关注的用户',
        type: 'bar',
        label: {
          show: true,
        },
        data: data.map((item) => item.cancelUser), // 取消关注的用户的数据
      },
    ],
  };
}

/** 累计用户数据图表配置项 */
export function userCumulateOption(
  data: MpStatisticsApi.StatisticsUserCumulateRespVO[],
  dates: string[],
): any {
  return {
    legend: {
      data: ['累计用户量'],
    },
    xAxis: {
      type: 'category',
      data: dates,
    },
    yAxis: {
      minInterval: 1,
    },
    series: [
      {
        name: '累计用户量',
        data: data.map((item) => item.cumulateUser), // 累计用户量的数据
        type: 'line',
        smooth: true,
        label: {
          show: true,
        },
      },
    ],
  };
}

/** 消息发送概况数据图表配置项 */
export function upstreamMessageOption(
  data: MpStatisticsApi.StatisticsUpstreamMessageRespVO[],
  dates: string[],
): any {
  return {
    color: ['#67C23A', '#E5323E'],
    legend: {
      data: ['用户发送人数', '用户发送条数'],
    },
    tooltip: {},
    xAxis: {
      data: dates, // X 轴的日期范围
    },
    yAxis: {
      minInterval: 1,
    },
    series: [
      {
        name: '用户发送人数',
        type: 'line',
        smooth: true,
        label: {
          show: true,
        },
        data: data.map((item) => item.msgUser), // 用户发送人数的数据
      },
      {
        name: '用户发送条数',
        type: 'line',
        smooth: true,
        label: {
          show: true,
        },
        data: data.map((item) => item.msgCount), // 用户发送条数的数据
      },
    ],
  };
}

/** 接口分析况数据图表配置项 */
export function interfaceSummaryOption(
  data: MpStatisticsApi.StatisticsInterfaceSummaryRespVO[],
  dates: string[],
): any {
  return {
    color: ['#67C23A', '#E5323E', '#E6A23C', '#409EFF'],
    legend: {
      data: ['被动回复用户消息的次数', '失败次数', '最大耗时', '总耗时'],
    },
    tooltip: {},
    xAxis: {
      data: dates, // X 轴的日期范围
    },
    yAxis: {},
    series: [
      {
        name: '被动回复用户消息的次数',
        type: 'bar',
        label: {
          show: true,
        },
        barGap: 0,
        data: data.map((item) => item.callbackCount), // 被动回复用户消息的次数的数据
      },
      {
        name: '失败次数',
        type: 'bar',
        label: {
          show: true,
        },
        data: data.map((item) => item.failCount), // 失败次数的数据
      },
      {
        name: '最大耗时',
        type: 'bar',
        label: {
          show: true,
        },
        data: data.map((item) => item.maxTimeCost), // 最大耗时的数据
      },
      {
        name: '总耗时',
        type: 'bar',
        label: {
          show: true,
        },
        data: data.map((item) => item.totalTimeCost), // 总耗时的数据
      },
    ],
  };
}
