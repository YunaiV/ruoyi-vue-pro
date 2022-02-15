<template>
  <div>
    <template v-for="(dict, index) in this.getDictDatas2(type, value)">
      <!-- 默认样式 -->
      <span v-if="dict.colorType === 'default' || dict.colorType === '' || dict.color === undefined" :key="dict.value" :index="index"
            :class="dict.cssClass">{{ dict.label }}</span>
      <!-- Tag 样式 -->
      <el-tag v-else :key="dict.value" :index="index" :type="dict.colorType" :class="dict.cssClass">
        {{ dict.label }}
      </el-tag>
    </template>
  </div>
</template>

<script>
export default {
  name: "DictTag",
  props: {
    type: String,
    value: [Number, String, Array],
  },
};
</script>
<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
