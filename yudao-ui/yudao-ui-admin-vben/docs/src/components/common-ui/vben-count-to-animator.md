---
outline: deep
---

# Vben CountToAnimator 数字动画

框架提供的数字动画组件，支持数字动画效果。

> 如果文档内没有参数说明，可以尝试在在线示例内寻找

::: info 写在前面

如果你觉得现有组件的封装不够理想，或者不完全符合你的需求，大可以直接使用原生组件，亦或亲手封装一个适合的组件。框架提供的组件并非束缚，使用与否，完全取决于你的需求与自由。

:::

## 基础用法

通过 `start-val` 和 `end-val`设置数字动画的开始值和结束值， 持续时间`3000`ms。

<DemoPreview dir="demos/vben-count-to-animator/basic" />

## 自定义前缀及分隔符

通过 `prefix` 和 `separator` 设置数字动画的前缀和分隔符。

<DemoPreview dir="demos/vben-count-to-animator/custom" />

### Props

| 属性名     | 描述           | 类型      | 默认值   |
| ---------- | -------------- | --------- | -------- |
| startVal   | 起始值         | `number`  | `0`      |
| endVal     | 结束值         | `number`  | `2021`   |
| duration   | 动画持续时间   | `number`  | `1500`   |
| autoplay   | 自动执行       | `boolean` | `true`   |
| prefix     | 前缀           | `string`  | -        |
| suffix     | 后缀           | `string`  | -        |
| separator  | 分隔符         | `string`  | `,`      |
| color      | 字体颜色       | `string`  | -        |
| useEasing  | 是否开启动画   | `boolean` | `true`   |
| transition | 动画效果       | `string`  | `linear` |
| decimals   | 保留小数点位数 | `number`  | `0`      |

### Events

| 事件名         | 描述           | 类型           |
| -------------- | -------------- | -------------- |
| started        | 动画已开始     | `()=>void`     |
| finished       | 动画已结束     | `()=>void`     |
| ~~onStarted~~  | ~~动画已开始~~ | ~~`()=>void`~~ |
| ~~onFinished~~ | ~~动画已结束~~ | ~~`()=>void`~~ |

### Methods

| 方法名 | 描述         | 类型       |
| ------ | ------------ | ---------- |
| start  | 开始执行动画 | `()=>void` |
| reset  | 重置         | `()=>void` |
