import loadScript from './loadScript'
import ELEMENT from 'element-ui'
import pluginsConfig from './pluginsConfig'

let tinymceObj

export default function loadTinymce(cb) {
  const { tinymceUrl } = pluginsConfig

  if (tinymceObj) {
    cb(tinymceObj)
    return
  }

  const loading = ELEMENT.Loading.service({
    fullscreen: true,
    lock: true,
    text: '富文本资源加载中...',
    spinner: 'el-icon-loading',
    background: 'rgba(255, 255, 255, 0.5)'
  })

  loadScript(tinymceUrl, () => {
    loading.close()
    // eslint-disable-next-line no-undef
    tinymceObj = tinymce
    cb(tinymceObj)
  })
}
