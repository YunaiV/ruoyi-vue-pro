import { createApp } from 'vue'

import Cookies from 'js-cookie'

import ElementPlus from 'element-plus'
import locale from 'element-plus/lib/locale/lang/zh-cn' // 中文语言

import '@/assets/styles/index.scss' // global css

import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive


// 注册指令
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

// svg图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import elementIcons from '@/components/SvgIcon/svgicon'

import './permission' // permission control

import { useDict } from '@/utils/dict'  // TODO 芋艿，需要删除
import { parseTime, resetForm, addDateRange, handleTree, selectDictLabel,addBeginAndEndTime} from '@/utils/ruoyi'
import {DICT_TYPE, getDictDataLabel, getDictDatas, getDictDatas2} from "@/utils/dict";


// 分页组件
import Pagination from '@/components/Pagination'
// 自定义表格工具组件
import RightToolbar from '@/components/RightToolbar'
// 文件上传组件
import FileUpload from "@/components/FileUpload"
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"
// 图片预览组件
import ImagePreview from "@/components/ImagePreview"
// 自定义树选择组件
import TreeSelect from '@/components/TreeSelect'
// 字典标签组件
import DictTag from '@/components/DictTag'
// 文档提示
import DocAlert from '@/components/DocAlert'

const app = createApp(App)

// 全局方法挂载
app.config.globalProperties.useDict = useDict // TODO 芋艿，需要删除
app.config.globalProperties.download = download
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.addBeginAndEndTime=addBeginAndEndTime
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.getDictDatas = getDictDatas
app.config.globalProperties.getDictDatas2 = getDictDatas2
app.config.globalProperties.getDictDataLabel = getDictDataLabel
app.config.globalProperties.DICT_TYPE = DICT_TYPE
app.config.globalProperties.handleTree = handleTree

// 全局组件挂载
app.component('DictTag', DictTag)
app.component('DocAlert', DocAlert)
app.component('Pagination', Pagination)
app.component('TreeSelect', TreeSelect)
app.component('FileUpload', FileUpload)
app.component('ImageUpload', ImageUpload)
app.component('ImagePreview', ImagePreview)
app.component('RightToolbar', RightToolbar)

app.use(router)
app.use(store)
app.use(plugins)
app.use(elementIcons)
app.component('svg-icon', SvgIcon)

// 引入 VForm 组件，参考 https://www.vform666.com/document3.html 文档
import axios from 'axios'
import VForm3 from 'vform3-builds' // 引入 VForm3 库
// import 'element-plus/dist/index.css'  // 引入 element-plus 样式
import 'vform3-builds/dist/designer.style.css'  //引入 VForm3 样式
app.use(VForm3)  // 全局注册 VForm(同时注册了 v-form-designer、v-form-render 等组件)
window.axios = axios

directive(app)

// 使用element-plus 并且设置全局的大小
app.use(ElementPlus, {
  locale: locale,
  // 支持 large、default、small
  size: Cookies.get('size') || 'default'
})

app.mount('#app')
