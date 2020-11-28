<!-- @author ruoyi 20201128 支持三级以上菜单缓存 -->
<template>
  <section class="app-main">
    <transition name="fade-transform" mode="out-in">
      <keep-alive :max="20" :exclude="notCacheName">
        <router-view :key="key" />
      </keep-alive>
    </transition>
  </section>
</template>

<script>
import Global from "@/layout/components/global.js";

export default {
  name: 'AppMain',
  computed: {
    notCacheName() {
      var visitedViews = this.$store.state.tagsView.visitedViews;
      var noCacheViews = [];
      Object.keys(visitedViews).some((index) => {
        if (visitedViews[index].meta.noCache) {
          noCacheViews.push(visitedViews[index].name);
        }
      });
      return noCacheViews;
    },
    key() {
      return this.$route.path;
    },
  },
  mounted() {
    // 关闭标签触发
    Global.$on("removeCache", (name, view) => {
      this.removeCache(name, view);
    });
  },
  methods: {
    // 获取有keep-alive子节点的Vnode
    getVnode() {
      // 判断子集非空
      if (this.$children.length == 0) return false;
      let vnode;
      for (let item of this.$children) {
        // 如果data中有key则代表找到了keep-alive下面的子集，这个key就是router-view上的key
        if (item.$vnode.data.key) {
          vnode = item.$vnode;
          break;
        }
      }
      return vnode ? vnode : false;
    },
    // 移除keep-alive缓存
    removeCache(name, view = {}) {
      let vnode = this.getVnode();
      if (!vnode) return false;
      let componentInstance = vnode.parent.componentInstance;
      // 这个key是用来获取前缀用来后面正则匹配用的
      let keyStart = vnode.key.split("/")[0];
      let thisKey = `${keyStart}${view.fullPath}`;
      let regKey = `${keyStart}${view.path}`;

      this[name]({ componentInstance, thisKey, regKey });
    },
    // 移除其他
    closeOthersTags({ componentInstance, thisKey }) {
      Object.keys(componentInstance.cache).forEach((key, index) => {
        if (key != thisKey) {
          // 销毁实例(这里存在多个key指向一个缓存的情况可能前面一个已经清除掉了所有要加判断)
          if (componentInstance.cache[key]) {
            componentInstance.cache[key].componentInstance.$destroy();
          }
          // 删除缓存
          delete componentInstance.cache[key];
          // 移除key中对应的key
          componentInstance.keys.splice(index, 1);
        }
      });
    },
    // 移除所有缓存
    closeAllTags({ componentInstance }) {
      // 销毁实例
      Object.keys(componentInstance.cache).forEach((key) => {
        if (componentInstance.cache[key]) {
          componentInstance.cache[key].componentInstance.$destroy();
        }
      });
      // 删除缓存
      componentInstance.cache = {};
      // 移除key中对应的key
      componentInstance.keys = [];
    },
    // 移除单个缓存
    closeSelectedTag({ componentInstance, regKey }) {
      let reg = new RegExp(`^${regKey}`);
      Object.keys(componentInstance.cache).forEach((key, i) => {
        if (reg.test(key)) {
          // 销毁实例
          if (componentInstance.cache[key]) {
            componentInstance.cache[key].componentInstance.$destroy();
          }
          // 删除缓存
          delete componentInstance.cache[key];
          // 移除key中对应的key
          componentInstance.keys.splice(i, 1);
        }
      });
    },
    // 刷新单个缓存
    refreshSelectedTag({ componentInstance, thisKey }) {
      Object.keys(componentInstance.cache).forEach((key, index) => {
        if (null != thisKey && key.replace("/redirect", "") == thisKey) {
          // 1 销毁实例(这里存在多个key指向一个缓存的情况可能前面一个已经清除掉了所有要加判断)
          if (componentInstance.cache[key]) {
            componentInstance.cache[key].componentInstance.$destroy();
          }
          // 2 删除缓存
          delete componentInstance.cache[key];
          // 3 移除key中对应的key
          componentInstance.keys.splice(index, 1);
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.app-main {
  /* 50= navbar  50  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

.hasTagsView {
  .app-main {
    /* 84 = navbar + tags-view = 50 + 34 */
    min-height: calc(100vh - 84px);
  }

  .fixed-header + .app-main {
    padding-top: 84px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>
