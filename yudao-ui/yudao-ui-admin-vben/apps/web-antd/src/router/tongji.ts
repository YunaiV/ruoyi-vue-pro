import type { Router } from 'vue-router';

declare global {
  interface Window {
    _hmt: any[];
  }
}

const HM_ID = import.meta.env.VITE_APP_BAIDU_CODE;

/**
 * 设置百度统计
 * @param router
 */
function setupBaiduTongJi(router: Router) {
  // 如果没有配置百度统计的 ID，则不进行设置
  if (!HM_ID) {
    return;
  }

  // _hmt：用于 router push
  window._hmt = window._hmt || [];

  router.afterEach((to) => {
    // 添加到 _hmt 中
    window._hmt.push(['_trackPageview', to.fullPath]);
  });
}

export { setupBaiduTongJi };
