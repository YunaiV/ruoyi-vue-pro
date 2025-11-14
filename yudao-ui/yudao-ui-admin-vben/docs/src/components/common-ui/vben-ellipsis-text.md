---
outline: deep
---

# Vben EllipsisText 省略文本

框架提供的文本展示组件，可配置超长省略、tooltip提示、展开收起等功能。

> 如果文档内没有参数说明，可以尝试在在线示例内寻找

## 基础用法

通过默认插槽设置文本内容，`maxWidth`属性设置最大宽度。

<DemoPreview dir="demos/vben-ellipsis-text/line" />

## 可折叠的文本块

通过`line`设置折叠后的行数，`expand`属性设置是否支持展开收起。

<DemoPreview dir="demos/vben-ellipsis-text/expand" />

## 自定义提示浮层

通过名为`tooltip`的插槽定制提示信息。

<DemoPreview dir="demos/vben-ellipsis-text/tooltip" />

## 自动显示 tooltip

通过`tooltip-when-ellipsis`设置，仅在文本长度超出导致省略号出现时才触发 tooltip。

<DemoPreview dir="demos/vben-ellipsis-text/auto-display" />

## API

### Props

| 属性名 | 描述 | 类型 | 默认值 |
| --- | --- | --- | --- |
| expand | 支持点击展开或收起 | `boolean` | `false` |
| line | 文本最大行数 | `number` | `1` |
| maxWidth | 文本区域最大宽度 | `number \| string` | `'100%'` |
| placement | 提示浮层的位置 | `'bottom'\|'left'\|'right'\|'top'` | `'top'` |
| tooltip | 启用文本提示 | `boolean` | `true` |
| tooltipWhenEllipsis | 内容超出，自动启用文本提示 | `boolean` | `false` |
| ellipsisThreshold | 设置 tooltipWhenEllipsis 后才生效，文本截断检测的像素差异阈值，越大则判断越严格，如果碰见异常情况可以自己设置阈值 | `number` | `3` |
| tooltipBackgroundColor | 提示文本的背景颜色 | `string` | - |
| tooltipColor | 提示文本的颜色 | `string` | - |
| tooltipFontSize | 提示文本的大小 | `string` | - |
| tooltipMaxWidth | 提示浮层的最大宽度。如不设置则保持与文本宽度一致 | `number` | - |
| tooltipOverlayStyle | 提示框内容区域样式 | `CSSProperties` | `{ textAlign: 'justify' }` |

### Events

| 事件名       | 描述         | 类型                       |
| ------------ | ------------ | -------------------------- |
| expandChange | 展开状态改变 | `(isExpand:boolean)=>void` |

### Slots

| 插槽名  | 描述                             |
| ------- | -------------------------------- |
| tooltip | 启用文本提示时，用来定制提示内容 |
