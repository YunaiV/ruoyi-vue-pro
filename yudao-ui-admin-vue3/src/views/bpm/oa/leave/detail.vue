<template>
  <div class="app-container">
    <!-- 对话框(添加 / 修改) -->
    <el-form ref="form" :model="form" label-width="100px">
      <el-form-item label="开始时间：" prop="startTime">
        {{ formatDate(form.startTime, '{y}-{m}-{d}') }}
      </el-form-item>
      <el-form-item label="结束时间：" prop="endTime">
        {{ formatDate(form.endTime, '{y}-{m}-{d}') }}
      </el-form-item>
      <el-form-item label="请假类型：" prop="type">
        <dict-tag :type="DICT_TYPE.BPM_OA_LEAVE_TYPE" :value="form.type" />
      </el-form-item>
      <el-form-item label="原因：" prop="reason"> {{ form.reason }}</el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getLeaveApi } from '@/api/bpm/leave'
import { DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { useRouter } from 'vue-router'
const router = useRouter()
// 请假编号
const id = ref()
// 表单参数
const form = ref({
  startTime: undefined,
  endTime: undefined,
  type: undefined,
  reason: undefined
})
/** 获得请假信息 */
const getDetail = () => {
  getLeaveApi(id.value).then((response) => {
    form.value = response.data
  })
}
onMounted(() => {
  id.value = router.currentRoute.value.query.id
  if (!id.value) {
    ElMessage({
      type: 'error',
      message: '未传递 id 参数，无法查看 OA 请假信息'
    })
    return
  }
  getDetail()
})
</script>
