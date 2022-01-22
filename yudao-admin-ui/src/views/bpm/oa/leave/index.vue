<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="请假类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择请假类型" clearable>
          <el-option v-for="dict in leaveTypeDictData" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="申请时间">
        <el-date-picker v-model="dateRangeCreateTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="结果" prop="result">
        <el-select v-model="queryParams.result" placeholder="请选择流结果" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="queryParams.reason" placeholder="请输入原因" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="申请编号" align="center" prop="id" />
      <el-table-column label="状态" align="center" prop="result" :formatter="resultFormat" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="请假类型" align="center" prop="type" :formatter="typeFormat" />
      <el-table-column label="原因" align="center" prop="reason" />
      <el-table-column label="申请时间" align="center" prop="applyTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleProcessDetail(scope.row)">审批进度</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker clearable size="small" v-model="form.startTime" type="date" value-format="timestamp" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker clearable size="small" v-model="form.endTime" type="date" value-format="timestamp" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择">
            <el-option
              v-for="dict in leaveTypeDictData"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="form.reason" placeholder="请输入原因" />
        </el-form-item>
        <el-form-item label="申请时间" prop="applyTime">
          <el-date-picker clearable size="small" v-model="form.applyTime" type="date" value-format="timestamp" placeholder="选择申请时间" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="title" :visible.sync="dialogDetailVisible" width="500px" append-to-body>
      <el-form ref="form" :model="form"  label-width="80px">
      <el-form-item label="状态" >
        {{ getDictDataLabel(DICT_TYPE.OA_LEAVE_STATUS, form.status) }}
      </el-form-item>
      <el-form-item label="申请人id" >{{form.userId}}</el-form-item>
      <el-form-item label="开始时间" >{{ parseTime(form.startTime) }}</el-form-item>
      <el-form-item label="结束时间" prop="endTime">{{ parseTime(form.endTime) }}</el-form-item>
      <el-form-item label="请假类型" prop="leaveType">
        {{ getDictDataLabel(DICT_TYPE.BPM_OA_LEAVE_TYPE, form.leaveType) }}
      </el-form-item>
      <el-form-item label="原因" prop="reason">{{form.reason}}</el-form-item>
      <el-form-item label="申请时间" prop="applyTime">{{ parseTime(form.applyTime) }}</el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogDetailVisible = false">确 定</el-button>
        <el-button @click="dialogDetailVisible = false">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { createLeave, getLeaveApplyMembers, getLeave, getLeavePage} from "@/api/oa/leave"
import { getDictDataLabel, getDictDatas, DICT_TYPE } from '@/utils/dict'

export default {
  name: "Leave",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 请假申请列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      //审批进度弹出层
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        processInstanceId: null,
        status: null,
        userId: null,
        leaveType: null,
        reason: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        startTime: [{ required: true, message: "开始时间不能为空", trigger: "blur" }],
        endTime: [{ required: true, message: "结束时间不能为空", trigger: "blur" }],
      },
      leaveTypeDictData: getDictDatas(DICT_TYPE.BPM_OA_LEAVE_TYPE),
      leaveResultData: getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT),

      dialogDetailVisible: false, // TODO 芋艿：后面挪到详情页
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getLeavePage(params).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        status: undefined,
        userId: undefined,
        startTime: undefined,
        endTime: undefined,
        leaveType: undefined,
        reason: undefined,
        applyTime: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeStartTime = [];
      this.dateRangeEndTime = [];
      this.dateRangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      getLeaveApplyMembers().then(response => {
        const route = {
          path: '/flow/leave/apply',
          query: {
            hr:  response.data.hr,
            pm:  response.data.pm,
            bm : response.data.bm
          }
        }
        this.$router.replace(route);
      });
    },
    /** 详情按钮操作 */
    handleDetail(row) {
      this.reset();
      const id = row.id;
      getLeave(id).then(response => {
        this.form = response.data;
        this.dialogDetailVisible = true
        this.title = "请假详情";
      });
    },
    /** 查看审批进度的操作 */
    handleProcessDetail(row) {
      this.$router.push({ path: "/bpm/process-instance/detail", query: { id: row.processInstanceId}});
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 添加的提交
        createLeave(this.form).then(response => {
          this.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    resultFormat(row, column) {
      return getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, row.result)
    },
    typeFormat(row, column) {
      return getDictDataLabel(DICT_TYPE.BPM_OA_LEAVE_TYPE, row.type)
    },
  }
};
</script>
