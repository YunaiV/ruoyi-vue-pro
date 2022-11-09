<script setup lang="ts">
import { ElTable, ElTableColumn } from 'element-plus'
import { onMounted, reactive } from 'vue'
interface sociaType {
  title: string
  type: string
  source: string
  img: string
}
interface socialUserType {
  socialUser: {
    socia: sociaType[]
  }
}
const state = reactive<socialUserType>({
  socialUser: {
    socia: []
  }
})
const initSocial = () => {
  console.info(1)
}
const bind = () => {
  console.info(1)
}
const unbind = () => {
  console.info(1)
}
onMounted(async () => {
  await initSocial()
})
</script>
<template>
  <el-table :data="state.socialUser.socia" :show-header="false">
    <el-table-column label="社交平台" align="left" width="120" prop="socia">
      <template #socia="{ row }">
        <img style="height: 20px; vertical-align: middle" :src="row.img" alt="" />
        {{ row.title }}
      </template>
    </el-table-column>
    <el-table-column label="操作" align="left" prop="action">
      <template #action="{ row }">
        <div v-if="row.openid">
          已绑定
          <el-button link type="primary" @click="unbind()">(解绑)</el-button>
        </div>
        <div v-else>
          未绑定
          <el-button link type="primary" @click="bind()">(绑定)</el-button>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>
