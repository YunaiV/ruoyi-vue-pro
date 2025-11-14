import type {
  // 系列类型的定义后缀都为 SeriesOption
  BarSeriesOption,
  GaugeSeriesOption,
  LineSeriesOption,
  MapSeriesOption,
} from 'echarts/charts';
import type {
  DatasetComponentOption,
  GeoComponentOption,
  GridComponentOption,
  // 组件类型的定义后缀都为 ComponentOption
  TitleComponentOption,
  TooltipComponentOption,
  VisualMapComponentOption,
} from 'echarts/components';
import type { ComposeOption } from 'echarts/core';

import {
  BarChart,
  GaugeChart,
  LineChart,
  MapChart,
  PieChart,
  RadarChart,
} from 'echarts/charts';
import {
  // 数据集组件
  DatasetComponent,
  GeoComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  // 内置数据转换器组件 (filter, sort)
  TransformComponent,
  VisualMapComponent,
} from 'echarts/components';
import * as echarts from 'echarts/core';
import { LabelLayout, UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

// 通过 ComposeOption 来组合出一个只有必须组件和图表的 Option 类型
export type ECOption = ComposeOption<
  | BarSeriesOption
  | DatasetComponentOption
  | GaugeSeriesOption
  | GeoComponentOption
  | GridComponentOption
  | LineSeriesOption
  | MapSeriesOption
  | TitleComponentOption
  | TooltipComponentOption
  | VisualMapComponentOption
>;

// 注册必须的组件
echarts.use([
  TitleComponent,
  PieChart,
  RadarChart,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  BarChart,
  LineChart,
  GaugeChart,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer,
  LegendComponent,
  ToolboxComponent,
  VisualMapComponent,
  MapChart,
  GeoComponent,
]);

export default echarts;
