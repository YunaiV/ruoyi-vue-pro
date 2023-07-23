<template>
  <div class="app-container">
    <doc-alert title="支付功能开启" url="https://doc.iocoder.cn/pay/build/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="应用编号" prop="appId">
        <el-select clearable v-model="queryParams.appId" filterable placeholder="请选择应用信息">
          <el-option v-for="item in appList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="通知类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择通知类型" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_NOTIFY_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="关联编号" prop="dataId">
        <el-input v-model="queryParams.dataId" placeholder="请输入关联编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="通知状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择通知状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_NOTIFY_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="商户订单编号" prop="merchantOrderId">
        <el-input v-model="queryParams.merchantOrderId" placeholder="请输入商户订单编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="任务编号" align="center" prop="id" />
      <el-table-column label="应用编号" align="center" prop="appName" />
      <el-table-column label="商户订单编号" align="center" prop="merchantOrderId" />
      <el-table-column label="通知类型" align="center" prop="type">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PAY_NOTIFY_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="关联编号" align="center" prop="dataId" />
      <el-table-column label="通知状态" align="center" prop="status">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PAY_NOTIFY_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="最后通知时间" align="center" prop="lastExecuteTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.lastExecuteTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下次通知时间" align="center" prop="nextNotifyTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.nextNotifyTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="通知次数" align="center" prop="notifyTimes">
        <template v-slot="scope">
          <el-tag size="mini" type="success">
            {{ scope.row.notifyTimes }} / {{ scope.row.maxNotifyTimes }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-search" @click="handleDetail(scope.row)"
                     v-hasPermi="['pay:notify:query']">查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(详情) -->
    <el-dialog title="通知详情" :visible.sync="open" width="700px" v-dialogDrag append-to-body>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="商户订单编号">
          <el-tag size="small">{{ notifyDetail.merchantOrderId }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="通知状态">
          <dict-tag :type="DICT_TYPE.PAY_NOTIFY_STATUS" :value="notifyDetail.status" size="small" />
        </el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="应用编号">{{ notifyDetail.appId }}</el-descriptions-item>
        <el-descriptions-item label="应用名称">{{ notifyDetail.appName }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="关联编号">{{ notifyDetail.dataId }}</el-descriptions-item>
        <el-descriptions-item label="通知类型">
          <dict-tag :type="DICT_TYPE.PAY_NOTIFY_TYPE" :value="notifyDetail.type" />
        </el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="通知次数">{{ notifyDetail.notifyTimes }}</el-descriptions-item>
        <el-descriptions-item label="最大通知次数">{{ notifyDetail.maxNotifyTimes }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="最后通知时间">{{ parseTime(notifyDetail.lastExecuteTime) }}</el-descriptions-item>
        <el-descriptions-item label="下次通知时间">{{ parseTime(notifyDetail.nextNotifyTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="创建时间">{{ parseTime(notifyDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ parseTime(notifyDetail.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <!-- 分割线 -->
      <el-divider />
      <el-descriptions :column="1" label-class-name="desc-label" direction="vertical" border>
        <el-descriptions-item label="回调日志">
          <el-table :data="notifyDetail.logs">
            <el-table-column label="日志编号" align="center" prop="id" />
            <el-table-column label="通知状态" align="center" prop="status">
              <template v-slot="scope">
                <dict-tag :type="DICT_TYPE.PAY_NOTIFY_STATUS" :value="scope.row.status" />
              </template>
            </el-table-column>
            <el-table-column label="通知次数" align="center" prop="notifyTimes" />
            <el-table-column label="通知时间" align="center" prop="lastExecuteTime" width="180">
              <template v-slot="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="响应结果" align="center" prop="response" />
          </el-table>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getNotifyTaskPage, getNotifyTaskDetail } from "@/api/pay/notify";
import { getAppList } from "@/api/pay/app";

export default {
  name: "PayNotify",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 支付通知列表
      list: [],
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        appId: null,
        type: null,
        dataId: null,
        status: null,
        merchantOrderId: null,
        createTime: [],
      },

      // 支付应用列表集合
      appList: [],
      // 通知详情
      notifyDetail: {
        logs: []
      },
    };
  },
  created() {
    this.getList();
    // 获得筛选项
    getAppList().then(response => {
      this.appList = response.data;
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getNotifyTaskPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
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
    /** 详情按钮操作 */
    handleDetail(row) {
      this.notifyDetail = {};
      getNotifyTaskDetail(row.id).then(response => {
        // 设置值
        this.notifyDetail = response.data;
        // 弹窗打开
        this.open = true;
      });
    },
  }
};
</script>
