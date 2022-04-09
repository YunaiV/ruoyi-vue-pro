// Baidu 统计 integration
import router from './router'

window._hmt = window._hmt || []; // 用于 router push
const HM_ID = process.env.VUE_APP_BAIDU_CODE || ''; // 有值的时候，才开启
(function() {
  if (!HM_ID) {
    return;
  }
  const hm = document.createElement("script")
  hm.src = "https://hm.baidu.com/hm.js?" + HM_ID
  const s = document.getElementsByTagName("script")[0]
  s.parentNode.insertBefore(hm, s)
})()

router.afterEach(function (to) {
  if (!HM_ID) {
    return;
  }
  _hmt.push(['_trackPageview', to.fullPath])
})
