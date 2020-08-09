<!-- @author Shiyn/   huangmx 20200807优化-->
<template>
  <div class="top-right-btn">
    <el-row>
      <el-tooltip class="item" effect="dark" :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
        <el-button size="mini" circle icon="el-icon-search" @click="clickSearch()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="刷新" placement="top">
        <el-button size="mini" circle icon="el-icon-refresh" @click="clickFresh()" />
      </el-tooltip>
    </el-row>
  </div>
</template>
<script>
export default {
  name: "TableToolsExt",
  data() {
    return {};
  },
  props: {
    showSearch: {
      type: Boolean,
      default: true,
    },
  },

  methods: {
    //搜索
    clickSearch() {
      this.$emit("update:showSearch", !this.showSearch);
    },
    //刷新
    clickFresh() {
      this.$emit("queryTable");
    },
  },
};
</script>