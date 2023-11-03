<template>
  <span>
    <template v-for="(dict, index) in this.getDictDatas2(type, value)">
      <!-- 默认样式 -->
      <span v-if="dict.colorType === 'default' || dict.colorType === '' || dict.colorType === undefined" :key="dict.value" :index="index"
            :class="dict.cssClass">{{ dict.label }}</span>
      <!-- Tag 样式 -->
      <el-tag v-else :disable-transitions="true" :key="dict.value" :index="index" :type="dict.colorType" :class="dict.cssClass">
        {{ dict.label }}
      </el-tag>
    </template>
  </span>
</template>

<script>
export default {
  name: "DictTag",
  props: {
    type: String,
    value: [Number, String, Boolean, Array],
  },
};
</script>
<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
