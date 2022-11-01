import { App, unref } from 'vue'
import 'xe-utils'
import XEUtils from 'xe-utils'
import { i18n } from '@/plugins/vueI18n'
import zhCN from 'vxe-table/es/locale/lang/zh-CN'
import enUS from 'vxe-table/lib/locale/lang/en-US'
import {
  // 全局对象
  VXETable,

  // 表格功能
  Filter,
  Edit,
  Menu,
  Export,
  Keyboard,
  Validator,

  // 可选组件
  Icon,
  Column,
  Colgroup,
  Grid,
  Tooltip,
  Toolbar,
  Pager,
  Form,
  FormItem,
  FormGather,
  Checkbox,
  CheckboxGroup,
  Radio,
  RadioGroup,
  RadioButton,
  Switch,
  Input,
  Select,
  Optgroup,
  Option,
  Textarea,
  Button,
  Modal,
  List,
  Pulldown,

  // 表格
  Table
} from 'vxe-table'

// 全局默认参数
VXETable.setup({
  size: 'medium', // 全局尺寸
  version: 0, // 版本号，对于某些带数据缓存的功能有用到，上升版本号可以用于重置数据
  zIndex: 1008, // 全局 zIndex 起始值，如果项目的的 z-index 样式值过大时就需要跟随设置更大，避免被遮挡
  loadingText: '加载中...', // 全局loading提示内容，如果为null则不显示文本
  height: 600,
  table: {
    border: 'inner', // default（默认）, full（完整边框）, outer（外边框）, inner（内边框）, none（无边框）
    align: 'center', // eft（左对齐）, center（居中对齐）, right（右对齐）
    autoResize: true, // 自动监听父元素的变化去重新计算表格
    resizable: true, // 列是否允许拖动列宽调整大小
    emptyText: '暂无数据', // 空表单
    highlightHoverRow: true // 自动监听父元素的变化去重新计算表格
  },
  grid: {
    toolbarConfig: {
      refresh: true,
      import: true,
      export: true,
      print: true,
      zoom: true,
      custom: true
    },
    pagerConfig: {
      border: false,
      background: true,
      autoHidden: true,
      perfect: true,
      pageSize: 10,
      pagerCount: 7,
      pageSizes: [5, 10, 15, 20, 50, 100, 200, 500],
      layouts: [
        'PrevJump',
        'PrevPage',
        'Jump',
        'PageCount',
        'NextPage',
        'NextJump',
        'Sizes',
        'Total'
      ]
    }
  },
  pager: {
    background: true,
    autoHidden: false,
    perfect: true,
    pageSize: 10,
    pagerCount: 7,
    pageSizes: [10, 15, 20, 50, 100],
    layouts: ['PrevJump', 'PrevPage', 'Jump', 'PageCount', 'NextPage', 'NextJump', 'Sizes', 'Total']
  },
  input: {
    clearable: true
  },
  modal: {
    width: 600, // 窗口的宽度
    height: 400, // 窗口的高度
    showZoom: true, // 标题是否标显示最大化与还原按钮
    resize: true, // 是否允许窗口边缘拖动调整窗口大小
    marginSize: 0, // 只对 resize 启用后有效，用于设置可拖动界限范围，如果为负数则允许拖动超出屏幕边界
    remember: false, // 记忆功能，会记住最后操作状态，再次打开窗口时还原窗口状态
    destroyOnClose: true, // 在窗口关闭时销毁内容
    storage: false, // 是否启用 localStorage 本地保存，会将窗口拖动的状态保存到本地
    transfer: true, // 是否将弹框容器插入于 body 内
    showFooter: true, // 是否显示底部
    mask: true, // 是否显示遮罩层
    maskClosable: true, // 是否允许点击遮罩层关闭窗口
    escClosable: true // 是否允许按 Esc 键关闭窗口
  },
  i18n: (key, args) => {
    return unref(i18n.global.locale) === 'zh-CN'
      ? XEUtils.toFormatString(XEUtils.get(zhCN, key), args)
      : XEUtils.toFormatString(XEUtils.get(enUS, key), args)
  }
})
export const setupVxeTable = (app: App<Element>) => {
  // 表格功能
  app.use(Filter).use(Edit).use(Menu).use(Export).use(Keyboard).use(Validator)

  // 可选组件
  app
    .use(Icon)
    .use(Column)
    .use(Colgroup)
    .use(Grid)
    .use(Tooltip)
    .use(Toolbar)
    .use(Pager)
    .use(Form)
    .use(FormItem)
    .use(FormGather)
    .use(Checkbox)
    .use(CheckboxGroup)
    .use(Radio)
    .use(RadioGroup)
    .use(RadioButton)
    .use(Switch)
    .use(Input)
    .use(Select)
    .use(Optgroup)
    .use(Option)
    .use(Textarea)
    .use(Button)
    .use(Modal)
    .use(List)
    .use(Pulldown)

    // 安装表格
    .use(Table)

  // 给 vue 实例挂载内部对象，例如：
  // app.config.globalProperties.$XModal = VXETable.modal
  // app.config.globalProperties.$XPrint = VXETable.print
  // app.config.globalProperties.$XSaveFile = VXETable.saveFile
  // app.config.globalProperties.$XReadFile = VXETable.readFile
}
