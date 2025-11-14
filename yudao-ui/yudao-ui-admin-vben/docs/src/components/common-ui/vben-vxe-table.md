---
outline: deep
---

# Vben Vxe Table 表格

框架提供的Table 列表组件基于 [vxe-table](https://vxetable.cn/v4/#/grid/api?apiKey=grid)，结合`Vben Form 表单`进行了二次封装。

其中，表头的 **表单搜索** 部分采用了`Vben Form表单`，表格主体部分使用了`vxe-grid`组件，支持表格的分页、排序、筛选等功能。

> 如果文档内没有参数说明，可以尝试在在线示例或者在 [vxe-grid 官方API 文档](https://vxetable.cn/v4/#/grid/api?apiKey=grid) 内寻找

::: info 写在前面

如果你觉得现有组件的封装不够理想，或者不完全符合你的需求，大可以直接使用原生组件，亦或亲手封装一个适合的组件。框架提供的组件并非束缚，使用与否，完全取决于你的需求与自由。

:::

## 适配器

表格底层使用 [vxe-table](https://vxetable.cn/#/start/install) 进行实现，所以你可以使用 `vxe-table` 的所有功能。对于不同的 UI 框架，我们提供了适配器，以便更好的适配不同的 UI 框架。

### 适配器说明

每个应用都可以自己配置`vxe-table`的适配器，你可以根据自己的需求。下面是一个简单的配置示例：

::: details vxe-table 表格适配器

```ts
import { h } from 'vue';

import { setupVbenVxeTable, useVbenVxeGrid } from '@vben/plugins/vxe-table';

import { Button, Image } from 'ant-design-vue';

import { useVbenForm } from './form';

setupVbenVxeTable({
  configVxeTable: (vxeUI) => {
    vxeUI.setConfig({
      grid: {
        align: 'center',
        border: false,
        columnConfig: {
          resizable: true,
        },
        minHeight: 180,
        formConfig: {
          // 全局禁用vxe-table的表单配置，使用formOptions
          enabled: false,
        },
        proxyConfig: {
          autoLoad: true,
          response: {
            result: 'items',
            total: 'total',
            list: 'items',
          },
          showActiveMsg: true,
          showResponseMsg: false,
        },
        round: true,
        showOverflow: true,
        size: 'small',
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellImage' },
    vxeUI.renderer.add('CellImage', {
      renderTableDefault(_renderOpts, params) {
        const { column, row } = params;
        return h(Image, { src: row[column.field] });
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellLink' },
    vxeUI.renderer.add('CellLink', {
      renderTableDefault(renderOpts) {
        const { props } = renderOpts;
        return h(
          Button,
          { size: 'small', type: 'link' },
          { default: () => props?.text },
        );
      },
    });

    // 这里可以自行扩展 vxe-table 的全局配置，比如自定义格式化
    // vxeUI.formats.add
  },
  useVbenForm,
});

export { useVbenVxeGrid };

export type * from '@vben/plugins/vxe-table';
```

:::

## 基础表格

使用 `useVbenVxeGrid` 创建最基础的表格。

<DemoPreview dir="demos/vben-vxe-table/basic" />

## 远程加载

通过指定 `proxyConfig.ajax` 的 `query` 方法，可以实现远程加载数据。

<DemoPreview dir="demos/vben-vxe-table/remote" />

## 树形表格

树形表格的数据源为扁平结构，可以指定`treeConfig`配置项，实现树形表格。

```typescript
treeConfig: {
  transform: true, // 指定表格为树形表格
  parentField: 'parentId', // 父节点字段名
  rowField: 'id', // 行数据字段名
},
```

<DemoPreview dir="demos/vben-vxe-table/tree" />

## 固定表头/列

列固定可选参数： `'left' | 'right' | '' | null`

<DemoPreview dir="demos/vben-vxe-table/fixed" />

## 自定义单元格

自定义单元格有两种实现方式

- 通过 `slots` 插槽
- 通过 `customCell` 自定义单元格，但是要先添加渲染器

```typescript
// 表格配置项可以用 cellRender: { name: 'CellImage' },
vxeUI.renderer.add('CellImage', {
  renderDefault(_renderOpts, params) {
    const { column, row } = params;
    return h(Image, { src: row[column.field] } as any); // 注意此处的Image 组件，来源于Antd，需要自行引入,否则会使用js的Image类
  },
});

// 表格配置项可以用 cellRender: { name: 'CellLink' },
vxeUI.renderer.add('CellLink', {
  renderDefault(renderOpts) {
    const { props } = renderOpts;
    return h(
      Button,
      { size: 'small', type: 'link' },
      { default: () => props?.text },
    );
  },
});
```

<DemoPreview dir="demos/vben-vxe-table/custom-cell" />

## 搜索表单

**表单搜索** 部分采用了`Vben Form 表单`，参考 [Vben Form 表单文档](/components/common-ui/vben-form)。

当启用了表单搜索时，可以在toolbarConfig中配置`search`为`true`来让表格在工具栏区域显示一个搜索表单控制按钮。表格的所有以`form-`开头的命名插槽都会被传递给搜索表单。

### 定制分隔条

当你启用表单搜索时，在表单和表格之间会显示一个分隔条。这个分隔条使用了默认的组件背景色，并且横向贯穿整个Vben Vxe Table在视觉上融入了页面的默认背景中。如果你在Vben Vxe Table的外层包裹了一个不同背景色的容器（如将其放在一个Card内），默认的表单和表格之间的分隔条可能就显得格格不入了，下面的代码演示了如何定制这个分隔条。

```ts
const [Grid] = useVbenVxeGrid({
  formOptions: {},
  gridOptions: {},
  // 完全移除分隔条
  separator: false,
  // 你也可以使用下面的代码来移除分隔条
  // separator: { show: false },
  // 或者使用下面的代码来改变分隔条的颜色
  // separator: { backgroundColor: 'rgba(100,100,0,0.5)' },
});
```

<DemoPreview dir="demos/vben-vxe-table/form" />

## 单元格编辑

通过指定`editConfig.mode`为`cell`，可以实现单元格编辑。

<DemoPreview dir="demos/vben-vxe-table/edit-cell" />

## 行编辑

通过指定`editConfig.mode`为`row`，可以实现行编辑。

<DemoPreview dir="demos/vben-vxe-table/edit-row" />

## 虚拟滚动

通过 scroll-y.enabled 与 scroll-y.gt 组合开启，其中 enabled 为总开关，gt 是指当总行数大于指定行数时自动开启。

> 参考 [vxe-table 官方文档 - 虚拟滚动](https://vxetable.cn/v4/#/component/grid/scroll/vertical)。

<DemoPreview dir="demos/vben-vxe-table/virtual" />

## API

`useVbenVxeGrid` 返回一个数组，第一个元素是表格组件，第二个元素是表格的方法。

```vue
<script setup lang="ts">
import { useVbenVxeGrid } from '#/adapter/vxe-table';

// Grid 为表格组件
// gridApi 为表格的方法
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {},
  formOptions: {},
  gridEvents: {},
  // 属性
  // 事件
});
</script>

<template>
  <Grid />
</template>
```

### GridApi

useVbenVxeGrid 返回的第二个参数，是一个对象，包含了一些表单的方法。

| 方法名 | 描述 | 类型 | 说明 |
| --- | --- | --- | --- |
| setLoading | 设置loading状态 | `(loading)=>void` | - |
| setGridOptions | 设置vxe-table grid组件参数 | `(options: Partial<VxeGridProps['gridOptions'])=>void` | - |
| reload | 重载表格，会进行初始化 | `(params:any)=>void` | - |
| query | 重载表格，会保留当前分页 | `(params:any)=>void` | - |
| grid | vxe-table grid实例 | `VxeGridInstance` | - |
| formApi | vbenForm api实例 | `FormApi` | - |
| toggleSearchForm | 设置搜索表单显示状态 | `(show?: boolean)=>boolean` | 当省略参数时，则将表单在显示和隐藏两种状态之间切换 |

## Props

所有属性都可以传入 `useVbenVxeGrid` 的第一个参数中。

| 属性名 | 描述 | 类型 | 版本要求 |
| --- | --- | --- | --- |
| tableTitle | 表格标题 | `string` | - |
| tableTitleHelp | 表格标题帮助信息 | `string` | - |
| gridClass | grid组件的class | `string` | - |
| gridOptions | grid组件的参数 | `VxeTableGridProps` | - |
| gridEvents | grid组件的触发的事件 | `VxeGridListeners` | - |
| formOptions | 表单参数 | `VbenFormProps` | - |
| showSearchForm | 是否显示搜索表单 | `boolean` | - |
| separator | 搜索表单与表格主体之间的分隔条 | `boolean\|SeparatorOptions` | >5.5.4 |

## Slots

大部分插槽的说明请参考 [vxe-table 官方文档](https://vxetable.cn/v4/#/grid/api)，但工具栏部分由于做了一些定制封装，需使用以下插槽定制表格的工具栏：

| 插槽名          | 描述                                         |
| --------------- | -------------------------------------------- |
| toolbar-actions | 工具栏左侧部分（表格标题附近）               |
| toolbar-tools   | 工具栏右侧部分（vxeTable原生工具按钮的左侧） |
| table-title     | 表格标题插槽                                 |

::: info 搜索表单的插槽

对于使用了搜索表单的表格来说，所有以`form-`开头的命名插槽都会传递给表单。

:::
