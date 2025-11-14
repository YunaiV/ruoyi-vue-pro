'use strict';
Object.defineProperty(exports, '__esModule', {
  value: true,
});
const fs = require('fs');
import stripJsonComments from './strip-json-comments';
import { isArray, isEmpty } from 'lodash';

class TransformPages {
  constructor({ includes, pagesJsonDir }) {
    this.includes = includes;
    this.uniPagesJSON = JSON.parse(stripJsonComments(fs.readFileSync(pagesJsonDir, 'utf-8')));
    this.routes = this.getPagesRoutes().concat(this.getSubPackagesRoutes());
    this.tabbar = this.getTabbarRoutes();
    this.routesMap = this.transformPathToKey(this.routes);
  }
  /**
   * 通过读取pages.json文件 生成直接可用的routes
   */
  getPagesRoutes(pages = this.uniPagesJSON.pages, rootPath = null) {
    let routes = [];
    for (let i = 0; i < pages.length; i++) {
      const item = pages[i];
      let route = {};
      for (let j = 0; j < this.includes.length; j++) {
        const key = this.includes[j];
        let value = item[key];
        if (key === 'path') {
          value = rootPath ? `/${rootPath}/${value}` : `/${value}`;
        }
        if (key === 'aliasPath' && i == 0 && rootPath == null) {
          route[key] = route[key] || '/';
        } else if (value !== undefined) {
          route[key] = value;
        }
      }
      routes.push(route);
    }
    return routes;
  }
  /**
   * 解析小程序分包路径
   */
  getSubPackagesRoutes() {
    if (!(this.uniPagesJSON && this.uniPagesJSON.subPackages)) {
      return [];
    }
    const subPackages = this.uniPagesJSON.subPackages;
    let routes = [];
    for (let i = 0; i < subPackages.length; i++) {
      const subPages = subPackages[i].pages;
      const root = subPackages[i].root;
      const subRoutes = this.getPagesRoutes(subPages, root);
      routes = routes.concat(subRoutes);
    }
    return routes;
  }

  getTabbarRoutes() {
    if (!(this.uniPagesJSON && this.uniPagesJSON.tabBar && this.uniPagesJSON.tabBar.list)) {
      return [];
    }
    const tabbar = this.uniPagesJSON.tabBar.list;
    let tabbarMap = [];
    tabbar.forEach((bar) => {
      tabbarMap.push('/' + bar.pagePath);
    });
    return tabbarMap;
  }

  transformPathToKey(list) {
    if (!isArray(list) || isEmpty(list)) {
      return [];
    }
    let map = {};
    list.forEach((i) => {
      map[i.path] = i;
    });
    return map;
  }
}

function uniReadPagesV3Plugin({ pagesJsonDir, includes }) {
  let defaultIncludes = ['path', 'aliasPath', 'name'];
  includes = [...defaultIncludes, ...includes];
  let pages = new TransformPages({
    pagesJsonDir,
    includes,
  });
  return {
    name: 'uni-read-pages-v3',
    config(config) {
      return {
        define: {
          ROUTES: pages.routes,
          ROUTES_MAP: pages.routesMap,
          TABBAR: pages.tabbar,
        },
      };
    },
  };
}
exports.default = uniReadPagesV3Plugin;
