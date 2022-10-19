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
  table: {
    // 自动监听父元素的变化去重新计算表格
    autoResize: true,
    emptyText: '暂无数据',
    // 鼠标移到行是否要高亮显示
    highlightHoverRow: true
  },
  pager: {
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
  i18n: (key, args) => {
    return unref(i18n.global.locale) === 'zh'
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
