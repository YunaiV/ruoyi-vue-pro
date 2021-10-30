<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="流程id" prop="processInstanceId">
        <el-input v-model="queryParams.processInstanceId" placeholder="请输入流程id" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态">
          <el-option
            v-for="dict in leaveStatusData"
            :key="parseInt(dict.value)"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker v-model="dateRangeStartTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker v-model="dateRangeEndTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="请假类型" prop="leaveType">
        <el-select v-model="queryParams.leaveType" placeholder="请选择请假类型">
          <el-option
            v-for="dict in leaveTypeDictData"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="queryParams.reason" placeholder="请输入原因" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="申请时间">
        <el-date-picker v-model="dateRangeApplyTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="请假表单主键" align="center" prop="id" />
      <el-table-column label="状态" align="center" prop="status" :formatter="statusFormat" />
      <el-table-column label="申请人id" align="center" prop="userId" />
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
      <el-table-column label="请假类型" align="center" prop="leaveType" :formatter="leaveTypeFormat" />
      <el-table-column label="原因" align="center" prop="reason" />
      <el-table-column label="申请时间" align="center" prop="applyTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.applyTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleStep(scope.row)">审批进度</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleDetail(scope.row)">详情</el-button>
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
        {{ getDictDataLabel(DICT_TYPE.OA_LEAVE_TYPE, form.leaveType) }}
      </el-form-item>
      <el-form-item label="原因" prop="reason">{{form.reason}}</el-form-item>
      <el-form-item label="申请时间" prop="applyTime">{{ parseTime(form.applyTime) }}</el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogDetailVisible = false">确 定</el-button>
        <el-button @click="dialogDetailVisible = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="title" :visible.sync="dialogStepsVisible" width="600px" append-to-body>
      <el-steps :active="stepActive" finish-status="success" >
        <el-step :title="stepTitle(item)" :description="' 办理人：' + item.assignee " icon="el-icon-edit"  v-for="(item) in handleTask.historyTask"></el-step>
      </el-steps>
      <br/>
      <el-steps direction="vertical" :active="stepActive">
        <el-step :title="stepTitle(item)" :description="stepDes(item)" v-for="(item) in handleTask.historyTask"></el-step>
      </el-steps>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogStepsVisible = false">确 定</el-button>
        <el-button @click="dialogStepsVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createLeave, updateLeave, deleteLeave, getLeave, getLeavePage, exportLeaveExcel } from "@/api/oa/leave"
import { getDictDataLabel, getDictDatas, DICT_TYPE } from '@/utils/dict'
import { processHistorySteps } from '@/api/oa/todo'
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
      //进度弹出层
      dialogDetailVisible: false,
      //审批进度弹出层
      dialogStepsVisible: false,
      dateRangeStartTime: [],
      dateRangeEndTime: [],
      dateRangeApplyTime: [],
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
      handleTask: {
        historyTask:[],
        taskVariable: "",
        formObject: {}
      },
      steps:[{
        stepName:"步骤一"
      }],
      // 表单校验
      rules: {
        startTime: [{ required: true, message: "开始时间不能为空", trigger: "blur" }],
        endTime: [{ required: true, message: "结束时间不能为空", trigger: "blur" }],
        applyTime: [{ required: true, message: "申请时间不能为空", trigger: "blur" }],
      },
      statusFormat(row, column) {
        return getDictDataLabel(DICT_TYPE.OA_LEAVE_STATUS, row.status)
      },
      leaveTypeFormat(row, column) {
        return getDictDataLabel(DICT_TYPE.OA_LEAVE_TYPE, row.leaveType)
      },
      leaveTypeDictData: getDictDatas(DICT_TYPE.OA_LEAVE_TYPE),
      leaveStatusData: getDictDatas(DICT_TYPE.OA_LEAVE_STATUS)
    };
  },
  created() {
    this.getList();
  },
  computed: {
    stepActive: function () {
      let idx = 0;
      for (let i = 0; i < this.handleTask.historyTask.length; i++) {
        if (this.handleTask.historyTask[i].status === 1) {
          idx = idx + 1;
        } else {
          break;
        }
      }
      return idx;
    },
    stepTitle() {
      return function (item) {
        let name = item.stepName;
        if (item.status === 1) {
          name += '(已完成)'
        }
        if (item.status === 0) {
          name += '(进行中)'
        }
        return name;
      }
    },
    stepDes() {
      return function (item) {
        let desc = "";
        if (item.status === 1) {
          desc += "审批人：[" + item.assignee + "]    审批意见: [" + item.comment + "]   审批时间: " + this.parseTime(item.endTime);
        }
        return desc;
      }
    }
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeStartTime, 'startTime');
      this.addBeginAndEndTime(params, this.dateRangeEndTime, 'endTime');
      this.addBeginAndEndTime(params, this.dateRangeApplyTime, 'applyTime');
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
        processInstanceId: undefined,
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
      this.dateRangeApplyTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加请假申请";
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
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateLeave(this.form).then(response => {
            this.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
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
    /** 审批进度 */
    handleStep(row) {
      const id = row.processInstanceId;
      processHistorySteps(id).then(response => {
        this.handleTask.historyTask = response.data;
        this.dialogStepsVisible = true
        this.title = "审批进度";
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.addBeginAndEndTime(params, this.dateRangeStartTime, 'startTime');
      this.addBeginAndEndTime(params, this.dateRangeEndTime, 'endTime');
      this.addBeginAndEndTime(params, this.dateRangeApplyTime, 'applyTime');
      // 执行导出
      this.$confirm('是否确认导出所有请假申请数据项?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return exportLeaveExcel(params);
        }).then(response => {
          this.downloadExcel(response, '请假申请.xls');
        })
    }
  }
};
</script>
