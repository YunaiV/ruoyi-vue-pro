<script setup lang="ts">
import { PropType, reactive, ref } from 'vue'
import { VxeGrid, VxeGridProps, VxeTableInstance } from 'vxe-table'

const props = defineProps({
  options: {
    type: Object as PropType<VxeGridProps>,
    default: () => ({})
  }
})
const xGrid = ref<VxeTableInstance>()

const gridOptions = reactive<VxeGridProps>({
  height: 300,
  align: null,
  columnConfig: {
    resizable: true
  },
  columns: [],
  toolbarConfig: {},
  data: []
})
const init = () => {
  console.log(props.options)
}
init()
</script>

<template>
  <ContentWrap>
    <vxe-grid ref="xGrid" v-bind="gridOptions" show-footer class="pro-table-scrollbar" />
  </ContentWrap>
</template>

<style scoped>
/*滚动条整体部分*/
.pro-table-scrollbar ::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
/*滚动条的轨道*/
.pro-table-scrollbar ::-webkit-scrollbar-track {
  background-color: #ffffff;
}
/*滚动条里面的小方块，能向上向下移动*/
.pro-table-scrollbar ::-webkit-scrollbar-thumb {
  background-color: #bfbfbf;
  border-radius: 5px;
  border: 1px solid #f1f1f1;
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
}
.pro-table-scrollbar ::-webkit-scrollbar-thumb:hover {
  background-color: #a8a8a8;
}
.pro-table-scrollbar ::-webkit-scrollbar-thumb:active {
  background-color: #787878;
}
/*边角，即两个滚动条的交汇处*/
.pro-table-scrollbar ::-webkit-scrollbar-corner {
  background-color: #ffffff;
}
</style>
