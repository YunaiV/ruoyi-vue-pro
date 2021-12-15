<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
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
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>



    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="任务Id" align="center" prop="id" />
      <el-table-column label="流程名称" align="center" prop="processName" />
      <el-table-column label="任务状态" align="center"  :formatter="statusFormat" prop="status" />
<!--      <el-table-column label="申请人id" align="center" prop="userId" />-->
<!--      <el-table-column label="开始时间" align="center" prop="startTime" width="180">-->
<!--        <template slot-scope="scope">-->
<!--          <span>{{ parseTime(scope.row.startTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="结束时间" align="center" prop="endTime" width="180">-->
<!--        <template slot-scope="scope">-->
<!--          <span>{{ parseTime(scope.row.endTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="请假类型" align="center" prop="leaveType" />-->
<!--      <el-table-column label="原因" align="center" prop="reason" />-->
<!--      <el-table-column label="申请时间" align="center" prop="applyTime" width="180">-->
<!--        <template slot-scope="scope">-->
<!--          <span>{{ parseTime(scope.row.applyTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" v-if="scope.row.status == 1"  @click="handleClaim(scope.row)">签收</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" v-if="scope.row.status == 2"  @click="getTaskFormKey(scope.row)">办理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-tabs tab-position="left" style="height: 500px;">
        <el-tab-pane label="详情">
          <el-form ref="form" :model="handleTask.formObject"  label-width="80px">
            <el-form-item label="状态" >
              {{ getDictDataLabel(DICT_TYPE.OA_LEAVE_STATUS, handleTask.formObject.status) }}
            </el-form-item>
            <el-form-item label="申请人id" >{{handleTask.formObject.userId}}</el-form-item>
            <el-form-item label="开始时间" >{{ parseTime(handleTask.formObject.startTime) }}</el-form-item>
            <el-form-item label="结束时间" prop="endTime">{{ parseTime(handleTask.formObject.endTime) }}</el-form-item>
            <el-form-item label="请假类型" prop="leaveType">
              {{ getDictDataLabel(DICT_TYPE.OA_LEAVE_TYPE, handleTask.formObject.leaveType) }}
            </el-form-item>
            <el-form-item label="原因" prop="reason">{{handleTask.formObject.reason}}</el-form-item>
            <el-form-item label="申请时间" prop="applyTime">{{ parseTime(handleTask.formObject.applyTime) }}</el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="任务处理">
          <el-steps :active="handleTask.historyTask.length-1" simple finish-status="success">
            <el-step :title="item.stepName" icon="el-icon-edit" v-for="(item) in handleTask.historyTask" ></el-step>
          </el-steps>
          <br/>
          <el-steps direction="vertical" :active="handleTask.historyTask.length-1" finish-status="success" space="60px">
            <el-step :title="item.stepName" :description="item.comment" v-for="(item) in handleTask.historyTask" ></el-step>
          </el-steps>
          <br/>
          <el-form ref="taskForm" :model="task"  label-width="80px" v-show="handleTask.taskVariable !=''">
              <el-form-item label="处理意见" prop="approved">
                <el-select v-model="task.approved" placeholder="处理意见">
                  <el-option
                    v-for="dict in approvedData"
                    :key="parseInt(dict.value)"
                    :label="dict.label"
                    :value="parseInt(dict.value)"
                  />
                </el-select>
              </el-form-item>
              <el-input
                type="textarea"
                :rows="2"
                v-model="task.comment">
              </el-input>
          </el-form>
          <br/>
          <el-button type="primary" @click="submitTask">提交</el-button>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { completeTask, taskSteps, getTaskFormKey,deleteLeave, getLeave, getTodoTaskPage, claimTask } from "@/api/oa/todo";
import { getDictDataLabel, getDictDatas, DICT_TYPE } from '@/utils/dict'
export default {
  name: "Todo",
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
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10
      },
      // 表单参数
      form: {},
      handleTask: {
        historyTask:[{
            stepName:"步骤一"
          }
        ],
        taskVariable: "",
        formObject: {}
      },
      steps:[{
        stepName:"步骤一"
      }],
      task: {
        approved : 1,
        variables: {},
        taskId: undefined,
        comment: ""
      },
      rules: {
      },
      leaveTypeDictData: getDictDatas(DICT_TYPE.OA_LEAVE_TYPE),
      leaveStatusData: getDictDatas(DICT_TYPE.OA_LEAVE_STATUS),
      approvedData: [
        {
          value: 1,
          label: '同意'
        },
        {
          value: 0,
          label: '不同意'
        }
      ]
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
      // 执行查询
      getTodoTaskPage(params).then(response => {
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

    statusFormat(row, column) {
      return row.status == 1 ? "未签收" : "已签收";
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
    getTaskFormKey(row) {
      const taskId = row.id;
      const data = {
        taskId : taskId
      }
      getTaskFormKey(data).then(response => {
        const resp = response.data;
        const path = resp.formKey;
        const taskId = resp.id;
        const businessKey =  resp.businessKey;
        const route = {
          path: path,
          query: {
            businessKey: businessKey,
            taskId:taskId
          }
        }
        this.$router.replace(route);
      });
    },
    handleLeaveApprove(row) {
      this.reset();
      const businessKey = row.businessKey;
      const taskId = row.id;
      const processKey = row.processKey;
      const data = {
        taskId : taskId,
        businessKey: businessKey,
        processKey: processKey
      }
      taskSteps(data).then(response => {
        this.form = {};
        this.handleTask = response.data;
        this.task.taskId = taskId;
        this.open = true;
        this.title = "任务办理";
      });
    },
    /** 任务签收操作 */
    handleClaim(row) {
      this.reset();
      const id = row.id;
      claimTask(id).then(() => {
        this.getList();
        this.msgSuccess("签收成功");
      });
    },
    /** 提交任务 */
    submitTask() {
      const taskVariableName = this.handleTask.taskVariable;
      if (taskVariableName != "") {
        if (this.task.approved == 1) {
          this.task.variables[taskVariableName] = true;
        }
        if (this.task.approved == 0) {
          this.task.variables[taskVariableName] = false;
        }
      }
      completeTask(this.task).then(response => {
        this.msgSuccess("执行任务成功");
        this.open = false;
        this.getList();
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$confirm('是否确认删除请假申请编号为"' + id + '"的数据项?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return deleteLeave(id);
        }).then(() => {
          this.getList();
          this.msgSuccess("删除成功");
        })
    }
  }
};
</script>
