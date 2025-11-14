import $store from '@/sheep/store';
import { showAuthModal, showShareModal } from '@/sheep/hooks/useModal';
import { isNumber, isString, isEmpty, startsWith, isObject, isNil, clone } from 'lodash-es';
import throttle from '@/sheep/helper/throttle';

const _go = (
  path,
  params = {},
  options = {
    redirect: false,
  },
) => {
  let page = ''; // 跳转页面
  let query = ''; // 页面参数
  let url = ''; // 跳转页面完整路径

  if (isString(path)) {
    // 判断跳转类型是 path ｜ 还是http
    if (startsWith(path, 'http')) {
      // #ifdef H5
      window.location = path;
      return;
      // #endif
      // #ifndef H5
      page = `/pages/public/webview`;
      query = `url=${encodeURIComponent(path)}`;
      // #endif
    } else if (startsWith(path, 'action:')) {
      handleAction(path);
      return;
    } else {
      [page, query] = path.split('?');
    }
    if (!isEmpty(params)) {
      let query2 = paramsToQuery(params);
      if (isEmpty(query)) {
        query = query2;
      } else {
        query += '&' + query2;
      }
    }
  }

  if (isObject(path)) {
    page = path.url;
    if (!isNil(path.params)) {
      query = paramsToQuery(path.params);
    }
  }

  const nextRoute = ROUTES_MAP[page];

  // 未找到指定跳转页面
  // mark: 跳转404页
  if (!nextRoute) {
    console.log(`%c跳转路径参数错误<${page || 'EMPTY'}>`, 'color:red;background:yellow');
    return;
  }

  // 页面登录拦截
  if (nextRoute.meta?.auth && !$store('user').isLogin) {
    showAuthModal();
    return;
  }

  url = page;
  if (!isEmpty(query)) {
    url += `?${query}`;
  }

  // 跳转底部导航
  if (TABBAR.includes(page)) {
    uni.switchTab({
      url,
    });
    return;
  }

  // 使用redirect跳转
  if (options.redirect) {
    uni.redirectTo({
      url,
    });
    return;
  }

  uni.navigateTo({
    url,
  });
};

// 限流 防止重复点击跳转
function go(...args) {
  throttle(() => {
    _go(...args);
  });
}

function paramsToQuery(params) {
  if (isEmpty(params)) {
    return '';
  }
  // return new URLSearchParams(Object.entries(params)).toString();
  let query = [];
  for (let key in params) {
    query.push(key + '=' + params[key]);
  }

  return query.join('&');
}

function back() {
  // #ifdef H5
  history.back();
  // #endif

  // #ifndef H5
  uni.navigateBack();
  // #endif
}

function redirect(path, params = {}) {
  go(path, params, {
    redirect: true,
  });
}

// 检测是否有浏览器历史
function hasHistory() {
  // #ifndef H5
  const pages = getCurrentPages();
  if (pages.length > 1) {
    return true;
  }
  return false;
  // #endif

  // #ifdef H5
  return !!history.state.back;
  // #endif
}

function getCurrentRoute(field = '') {
  let currentPage = getCurrentPage();
  // #ifdef MP
  currentPage.$page['route'] = currentPage.route;
  currentPage.$page['options'] = currentPage.options;
  // #endif
  if (field !== '') {
    return currentPage.$page[field];
  } else {
    return currentPage.$page;
  }
}

function getCurrentPage() {
  let pages = getCurrentPages();
  return pages[pages.length - 1];
}

function handleAction(path) {
  const action = path.split(':');
  switch (action[1]) {
    case 'showShareModal':
      showShareModal();
      break;
  }
}

function error(errCode, errMsg = '') {
  redirect('/pages/public/error', {
    errCode,
    errMsg,
  });
}

export default {
  go,
  back,
  hasHistory,
  redirect,
  getCurrentPage,
  getCurrentRoute,
  error,
};
