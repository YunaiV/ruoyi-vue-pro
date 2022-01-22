import loadScript from './loadScript'
import ELEMENT from 'element-ui'
import pluginsConfig from './pluginsConfig'

let beautifierObj

export default function loadBeautifier(cb) {
  const { beautifierUrl } = pluginsConfig
  if (beautifierObj) {
    cb(beautifierObj)
    return
  }

  const loading = ELEMENT.Loading.service({
    fullscreen: true,
    lock: true,
    text: '格式化资源加载中...',
    spinner: 'el-icon-loading',
    background: 'rgba(255, 255, 255, 0.5)'
  })

  loadScript(beautifierUrl, () => {
    loading.close()
    // eslint-disable-next-line no-undef
    beautifierObj = beautifier
    cb(beautifierObj)
  })
}
