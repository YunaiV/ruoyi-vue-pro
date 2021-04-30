<template>
  <div class="app-container">
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="处理器的名字" prop="handlerName">
        <el-input v-model="queryParams.handlerName" placeholder="请输入处理器的名字" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="开始执行时间" prop="beginTime">
        <el-date-picker clearable size="small" v-model="queryParams.beginTime" type="date" value-format="yyyy-MM-dd" placeholder="选择开始执行时间" />
      </el-form-item>
      <el-form-item label="结束执行时间" prop="endTime">
        <el-date-picker clearable size="small" v-model="queryParams.endTime" type="date" value-format="yyyy-MM-dd" placeholder="选择结束执行时间" />
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择任务状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.INF_JOB_LOG_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['infra:job:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="日志编号" align="center" prop="id" />
      <el-table-column label="任务编号" align="center" prop="jobId" />
      <el-table-column label="处理器的名字" align="center" prop="handlerName" />
      <el-table-column label="处理器的参数" align="center" prop="handlerParam" />
      <el-table-column label="第几次执行" align="center" prop="executeIndex" />
      <el-table-column label="执行时间" align="center" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.beginTime) + ' ~ ' + parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行时长" align="center" prop="duration">
        <template slot-scope="scope">
          <span>{{ scope.row.duration + ' 毫秒' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.INF_JOB_LOG_STATUS, scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)"
                     v-hasPermi="['infra:job:query']">详细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 调度日志详细 -->
    <el-dialog title="调度日志详细" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="120px" size="mini">
        <el-row>
          <el-col :span="12">
            <el-form-item label="日志编号：">{{ form.id }}</el-form-item>
            <el-form-item label="任务编号：">{{ form.jobId }}</el-form-item>
            <el-form-item label="处理器的名字：">{{ form.handlerName }}</el-form-item>
            <el-form-item label="处理器的参数：">{{ form.handlerParam }}</el-form-item>
            <el-form-item label="第几次执行：">{{ form.executeIndex }}</el-form-item>
            <el-form-item label="执行时间：">{{ parseTime(form.beginTime) + ' ~ ' + parseTime(form.endTime) }}</el-form-item>
            <el-form-item label="执行时长：">{{ parseTime(form.duration) + ' 毫秒' }}</el-form-item>
            <el-form-item label="任务状态：">{{ getDictDataLabel(DICT_TYPE.INF_JOB_LOG_STATUS, form.status) }}</el-form-item>
            <el-form-item label="执行结果：">{{ form.result }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getJobLogPage, exportJobLogExcel } from "@/api/infra/jobLog";

export default {
  name: "JobLog",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 调度日志表格数据
      list: [],
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        handlerName: null,
        beginTime: null,
        endTime: null,
        status: null,
      }
    };
  },
  created() {
    this.queryParams.jobId = this.$route.query && this.$route.query.jobId;
    this.getList();
  },
  methods: {
    /** 查询调度日志列表 */
    getList() {
      this.loading = true;
      getJobLogPage({
        ...this.queryParams,
        beginTime: this.queryParams.beginTime ? this.queryParams.beginTime + ' 00:00:00' : undefined,
        endTime: this.queryParams.endTime ? this.queryParams.endTime + ' 23:59:59' : undefined,
      }).then(response => {
          this.list = response.data.list;
          this.total = response.data.total;
          this.loading = false;
        }
      );
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 详细按钮操作 */
    handleView(row) {
      this.open = true;
      this.form = row;
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams,
        beginTime: this.queryParams.beginTime ? this.queryParams.beginTime + ' 00:00:00' : undefined,
        endTime: this.queryParams.endTime ? this.queryParams.endTime + ' 23:59:59' : undefined,
      };
      params.pageNo = undefined;
      params.pageSize = undefined;
      // 执行导出
      this.$confirm('是否确认导出所有定时任务日志数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return exportJobLogExcel(params);
      }).then(response => {
        this.downloadExcel(response, '定时任务日志.xls');
      })
    }
  }
};
</script>
