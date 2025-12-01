import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { fenToYuan } from '@vben/utils';

/** 会员地域分布图表配置 */
export function getAreaChartOptions(
  data: MallMemberStatisticsApi.AreaStatisticsRespVO[],
): any {
  if (!data || data.length === 0) {
    return {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#999',
          fontSize: 14,
        },
      },
    };
  }

  // 计算 min 和 max 值
  let min = Number.POSITIVE_INFINITY;
  let max = Number.NEGATIVE_INFINITY;
  const mapData = data.map((item) => {
    const payUserCount = item.orderPayUserCount || 0;
    min = Math.min(min, payUserCount);
    max = Math.max(max, payUserCount);
    return {
      ...item,
      name: item.areaName,
      value: payUserCount,
    };
  });
  // 如果所有值都为 0，设置合理的 min 和 max 值
  if (min === max && min === 0) {
    min = 0;
    max = 10;
  }

  // 返回图表配置
  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const itemData = params?.data;
        if (!itemData) {
          return `${params?.name || ''}<br/>暂无数据`;
        }
        return `${itemData.areaName || params.name}<br/>
会员数量：${itemData.userCount || 0}<br/>
订单创建数量：${itemData.orderCreateUserCount || 0}<br/>
订单支付数量：${itemData.orderPayUserCount || 0}<br/>
订单支付金额：￥${Number(fenToYuan(itemData.orderPayPrice || 0)).toFixed(2)}`;
      },
    },
    visualMap: {
      text: ['高', '低'],
      realtime: false,
      calculable: true,
      top: 'middle',
      left: 10,
      min,
      max,
      inRange: {
        color: ['#e6f3ff', '#1890ff', '#0050b3'],
      },
    },
    series: [
      {
        name: '会员地域分布',
        type: 'map',
        map: 'china',
        roam: false,
        selectedMode: false,
        itemStyle: {
          borderColor: '#389e0d',
          borderWidth: 0.5,
        },
        emphasis: {
          itemStyle: {
            areaColor: '#ffec3d',
            borderWidth: 1,
          },
        },
        data: mapData,
      },
    ],
  };
}

/** VXE Grid 表格列配置 */
export function getAreaTableColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'areaName',
      title: '省份',
      minWidth: 80,
      sortable: true,
      showOverflow: 'tooltip',
    },
    {
      field: 'userCount',
      title: '会员数量',
      minWidth: 100,
      sortable: true,
    },
    {
      field: 'orderCreateUserCount',
      title: '订单创建数量',
      minWidth: 120,
      sortable: true,
    },
    {
      field: 'orderPayUserCount',
      title: '订单支付数量',
      minWidth: 120,
      sortable: true,
    },
    {
      field: 'orderPayPrice',
      title: '订单支付金额',
      minWidth: 120,
      sortable: true,
      formatter: 'formatFenToYuanAmount',
    },
  ];
}
