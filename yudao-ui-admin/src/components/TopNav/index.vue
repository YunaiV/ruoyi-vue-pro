<template>
  <el-menu
    :default-active="activeMenu"
    mode="horizontal"
    @select="handleSelect"
  >
    <template v-for="(item, index) in topMenus">
      <el-menu-item :index="item.path" :key="index" v-if="index < visibleNumber"
      ><svg-icon :icon-class="item.meta.icon" />
        {{ item.meta.title }}</el-menu-item
      >
    </template>

    <!-- 顶部菜单超出数量折叠 -->
    <el-submenu index="more" v-if="topMenus.length > visibleNumber">
      <template slot="title">更多菜单</template>
      <template v-for="(item, index) in topMenus">
        <el-menu-item
          :index="item.path"
          :key="index"
          v-if="index >= visibleNumber"
        ><svg-icon :icon-class="item.meta.icon" />
          {{ item.meta.title }}</el-menu-item
        >
      </template>
    </el-submenu>
  </el-menu>
</template>

<script>
import { constantRoutes } from "@/router";

// 不需要激活的路由
const noactiveList = ["/user/profile", "/dict/type", "/gen/edit", "/job/log"];

export default {
  data() {
    return {
      // 顶部栏初始数
      visibleNumber: 5,
      // 是否为首次加载
      isFrist: false,
    };
  },
  computed: {
    // 顶部显示菜单
    topMenus() {
      let topMenus = [];
      this.routers.map((menu) => {
        if (menu.hidden === false) {
          topMenus.push(menu);
        }
      });
      return topMenus;
    },
    // 所有的路由信息
    routers() {
      return this.$store.state.permission.topbarRouters;
    },
    // 设置子路由
    childrenMenus() {
      var childrenMenus = [];
      this.routers.map((router) => {
        for (var item in router.children) {
          if (router.children[item].parentPath === undefined) {
            router.children[item].path = router.path + "/" + router.children[item].path;
            router.children[item].parentPath = router.path;
          }
          childrenMenus.push(router.children[item]);
        }
      });
      return constantRoutes.concat(childrenMenus);
    },
    // 默认激活的菜单
    activeMenu() {
      const path = this.$route.path;
      let activePath = this.routers[0].path;
      var noactive = noactiveList.some(function (item) {
        return path.indexOf(item) !== -1;
      });
      if (noactive) {
        return;
      }
      if (path.lastIndexOf("/") > 0) {
        const tmpPath = path.substring(1, path.length);
        activePath = "/" + tmpPath.substring(0, tmpPath.indexOf("/"));
      } else if ("/index" == path || "" == path) {
        if (!this.isFrist) {
          this.isFrist = true;
        } else {
          activePath = "index";
        }
      }
      this.activeRoutes(activePath);
      return activePath;
    },
  },
  mounted() {
    this.setVisibleNumber();
  },
  methods: {
    // 根据宽度计算设置显示栏数
    setVisibleNumber() {
      const width = document.body.getBoundingClientRect().width - 380;
      const elWidth = this.$el.getBoundingClientRect().width;
      const menuItemNodes = this.$el.children;
      const menuWidth = Array.from(menuItemNodes).map(
        (i) => i.getBoundingClientRect().width
      );
      this.visibleNumber = (
        parseInt(width - elWidth) / parseInt(menuWidth)
      ).toFixed(0);
    },
    // 菜单选择事件
    handleSelect(key, keyPath) {
      if (key.indexOf("http://") !== -1 || key.indexOf("https://") !== -1) {
        // http(s):// 路径新窗口打开
        window.open(key, "_blank");
      } else {
        this.activeRoutes(key);
      }
    },
    // 当前激活的路由
    activeRoutes(key) {
      var routes = [];
      if (this.childrenMenus && this.childrenMenus.length > 0) {
        this.childrenMenus.map((item) => {
          if (key == item.parentPath || (key == "index" && "" == item.path)) {
            routes.push(item);
          }
        });
      }
      this.$store.commit("SET_SIDEBAR_ROUTERS", routes);
    }
  },
};
</script>

<style lang="scss" scoped>
.el-menu--horizontal > .el-menu-item {
  float: left;
  height: 50px;
  line-height: 50px;
  margin: 0;
  border-bottom: 3px solid transparent;
  color: #999093;
  padding: 0 5px;
  margin: 0 10px;
}

.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom: 3px solid #409eff;
  color: #303133;
}
</style>
