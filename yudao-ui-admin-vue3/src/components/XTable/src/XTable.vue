<template>
  <VxeGrid v-bind="getProps" ref="xGrid" :class="`${prefixCls}`" class="xtable-scrollbar">
    <template #[item]="data" v-for="item in Object.keys($slots)" :key="item">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
  </VxeGrid>
</template>
<script lang="ts" setup name="XTable">
import { computed, PropType, ref, unref, useAttrs, watch } from 'vue'
import { SizeType, VxeGridInstance } from 'vxe-table'
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'
import { XTableProps } from './type'
import { isBoolean, isFunction } from '@/utils/is'

const appStore = useAppStore()

const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('x-vxe-table')

const attrs = useAttrs()
const emit = defineEmits(['register'])

const props = defineProps({
  options: {
    type: Object as PropType<XTableProps>,
    default: () => {}
  }
})
const innerProps = ref<Partial<XTableProps>>()

const getProps = computed(() => {
  const options = innerProps.value || props.options
  options.size = currentSize as any
  options.height = 700
  getColumnsConfig(options)
  getProxyConfig(options)
  getPageConfig(options)
  getToolBarConfig(options)
  // console.log(options);
  return {
    ...options,
    ...attrs
  }
})

const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
watch(
  () => appStore.getIsDark,
  () => {
    if (appStore.getIsDark == true) {
      import('./style/dark.scss')
    }
    if (appStore.getIsDark == false) {
      import('./style/light.scss')
    }
  },
  { immediate: true }
)
const currentSize = computed(() => {
  let resSize: SizeType = 'small'
  const appsize = appStore.getCurrentSize
  switch (appsize) {
    case 'large':
      resSize = 'medium'
      break
    case 'default':
      resSize = 'small'
      break
    case 'small':
      resSize = 'mini'
      break
  }
  return resSize
})

const reload = () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  g.commitProxy('query')
}

const getSearchData = () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  const queryParams = Object.assign({}, JSON.parse(JSON.stringify(g.getProxyInfo()?.form)))
  return queryParams
}

let proxyForm = false

// columns
const getColumnsConfig = (options: XTableProps) => {
  const { allSchemas } = options
  if (!allSchemas) return
  if (allSchemas.printSchema) {
    options.printConfig = {
      columns: allSchemas.printSchema
    }
  }
  if (allSchemas.formSchema) {
    proxyForm = true
    options.formConfig = {
      enabled: true,
      titleWidth: 100,
      titleAlign: 'right',
      items: allSchemas.searchSchema
    }
  }
  if (allSchemas.tableSchema) {
    options.columns = allSchemas.tableSchema
  }
}

// 动态请求
const getProxyConfig = (options: XTableProps) => {
  const { getListApi, proxyConfig, data } = options
  if (proxyConfig || data) return
  if (getListApi && isFunction(getListApi)) {
    options.proxyConfig = {
      seq: true, // 启用动态序号代理（分页之后索引自动计算为当前页的起始序号）
      form: proxyForm, // 启用表单代理，当点击表单提交按钮时会自动触发 reload 行为
      props: { result: 'list', total: 'total' },
      ajax: {
        query: async ({ page, form }) => {
          let queryParams: any = Object.assign({}, JSON.parse(JSON.stringify(form)))
          if (options.params) {
            queryParams = Object.assign(queryParams, options.params)
          }
          queryParams.pageSize = page.currentPage
          queryParams.page = page.pageSize

          return new Promise(async (resolve) => {
            resolve(await getListApi(queryParams))
          })
        }
      }
    }
  }
}

// 分页
const getPageConfig = (options: XTableProps) => {
  const { pagination, pagerConfig } = options
  if (pagerConfig) return
  if (pagination) {
    if (isBoolean(pagination)) {
      options.pagerConfig = {
        border: false, // 带边框
        background: true, // 带背景颜色
        perfect: false, // 配套的样式
        pageSize: 10, // 每页大小
        pagerCount: 7, // 显示页码按钮的数量
        autoHidden: false, // 当只有一页时自动隐藏
        pageSizes: [5, 10, 20, 30, 50, 100], // 每页大小选项列表
        layouts: [
          'PrevJump',
          'PrevPage',
          'JumpNumber',
          'NextPage',
          'NextJump',
          'Sizes',
          'FullJump',
          'Total'
        ]
      }
      return
    }
    options.pagerConfig = pagination
  }
}

// tool bar
const getToolBarConfig = (options: XTableProps) => {
  const { toolBar, toolbarConfig } = options
  if (toolbarConfig) return
  if (toolBar) {
    if (!isBoolean(toolBar)) {
      options.toolbarConfig = toolBar
      return
    }
  } else {
    options.toolbarConfig = {
      slots: { buttons: 'toolbar_buttons' }
    }
  }
}

const setProps = (prop: Partial<XTableProps>) => {
  innerProps.value = { ...unref(innerProps), ...prop }
}

defineExpose({ reload, Ref: xGrid, getSearchData })
emit('register', { reload, getSearchData, setProps })
</script>
<style lang="scss">
@import './style/index.scss';
</style>
