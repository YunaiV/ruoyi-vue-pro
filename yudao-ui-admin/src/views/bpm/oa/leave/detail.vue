<template>
  <div class="app-container">
    <!-- 对话框(添加 / 修改) -->
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="开始时间：" prop="startTime"> {{parseTime(form.startTime, '{y}-{m}-{d}')}} </el-form-item>
        <el-form-item label="结束时间：" prop="endTime"> {{parseTime(form.endTime, '{y}-{m}-{d}')}} </el-form-item>
        <el-form-item label="请假类型：" prop="type">
          <dict-tag :type="DICT_TYPE.BPM_OA_LEAVE_TYPE" :value="form.type"/>
        </el-form-item>
        <el-form-item label="原因：" prop="reason"> {{ form.reason }}</el-form-item>
      </el-form>
  </div>
</template>

<script>
import { getLeave}  from "@/api/bpm/leave"
import {getDictDatas, DICT_TYPE} from '@/utils/dict'
export default {
  name: "LeaveDetail",
  components: {
  },
  data() {
    return {
      id: undefined, // 请假编号
      // 表单参数
      form: {
        startTime: undefined,
        endTime: undefined,
        type: undefined,
        reason: undefined,
      },

      typeDictData: getDictDatas(DICT_TYPE.BPM_OA_LEAVE_TYPE),
    };
  },
  created() {
    this.id = this.$route.query.id;
    if (!this.id) {
      this.$message.error('未传递 id 参数，无法查看 OA 请假信息');
      return;
    }
    this.getDetail();
  },
  methods: {
    /** 获得请假信息 */
    getDetail() {
      getLeave(this.id).then(response => {
        this.form = response.data;
      });
    },
  }
};
</script>
