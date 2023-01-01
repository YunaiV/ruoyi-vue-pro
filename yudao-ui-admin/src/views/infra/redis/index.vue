<template>
  <div class="app-container">
    <doc-alert title="Redis 缓存" url="https://doc.iocoder.cn/redis-cache/" />
    <doc-alert title="本地缓存" url="https://doc.iocoder.cn/local-cache/" />
    <el-row>
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header"><span>基本信息</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%">
              <tbody>
                <tr>
                  <td><div class="cell">Redis版本</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.redis_version }}</div></td>
                  <td><div class="cell">运行模式</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.redis_mode === "standalone" ? "单机" : "集群" }}</div></td>
                  <td><div class="cell">端口</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.tcp_port }}</div></td>
                  <td><div class="cell">客户端数</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.connected_clients }}</div></td>
                </tr>
                <tr>
                  <td><div class="cell">运行时间(天)</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.uptime_in_days }}</div></td>
                  <td><div class="cell">使用内存</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.used_memory_human }}</div></td>
                  <td><div class="cell">使用CPU</div></td>
                  <td><div class="cell" v-if="cache.info">{{ parseFloat(cache.info.used_cpu_user_children).toFixed(2) }}</div></td>
                  <td><div class="cell">内存配置</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.maxmemory_human }}</div></td>
                </tr>
                <tr>
                  <td><div class="cell">AOF是否开启</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.aof_enabled === "0" ? "否" : "是" }}</div></td>
                  <td><div class="cell">RDB是否成功</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.rdb_last_bgsave_status }}</div></td>
                  <td><div class="cell">Key数量</div></td>
                  <td><div class="cell" v-if="cache.dbSize">{{ cache.dbSize }} </div></td>
                  <td><div class="cell">网络入口/出口</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.instantaneous_input_kbps }}kps/{{cache.info.instantaneous_output_kbps}}kps</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header"><span>命令统计</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="commandstats" style="height: 420px" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header">
            <span>内存信息</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="usedmemory" style="height: 420px" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="keyDefineListLoad" :data="keyDefineList" row-key="id" @row-click="openKeyTemplate">
      <el-table-column prop="keyTemplate" label="Key 模板" width="200" />
      <el-table-column prop="keyType" label="Key 类型" width="100" />
      <el-table-column prop="valueType" label="Value 类型" />
      <el-table-column prop="timeoutType" label="超时时间" width="200">
        <template v-slot="scope">
          <dict-tag
            :type="DICT_TYPE.INFRA_REDIS_TIMEOUT_TYPE"
            :value="scope.row.timeoutType"
          />
          <span v-if="scope.row.timeout > 0"
            >({{ scope.row.timeout / 1000 }} 秒)</span
          >
        </template>
      </el-table-column>
      <el-table-column prop="memo" label="备注" />
    </el-table>

    <!-- 缓存模块信息框 -->
    <el-dialog :title="keyTemplate + ' 模板'" :visible.sync="open" width="70vw" append-to-body>
      <el-row :gutter="10">
        <el-col :span="14" class="card-box">
          <el-card style="height: 70vh; overflow: scroll">
            <div slot="header">
              <span>键名列表</span>
              <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-refresh-right" @click="refreshKeys" />
            </div>
            <el-table :data="cacheKeys" style="width: 100%" @row-click="handleKeyValue">
              <el-table-column label="缓存键名" align="center" :show-overflow-tooltip="true">
                <template v-slot="scope">{{ scope.row }}</template>
              </el-table-column>
              <el-table-column label="操作" width="60" align="center" class-name="small-padding fixed-width">
                <template v-slot="scope">
                  <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteKey(scope.row)" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="10">
          <el-card :bordered="false" style="height: 70vh">
            <div slot="header">
              <span>缓存内容</span>
              <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-refresh-right"
                         @click="handleDeleteKeys(keyTemplate)">清理全部</el-button>
            </div>
          <el-form :model="cacheForm">
            <el-row :gutter="32">
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存键名:" prop="key">
                  <el-input v-model="cacheForm.key" :readOnly="true" />
                </el-form-item>
                </el-col>
                <el-col :offset="1" :span="22">
                  <el-form-item label="缓存内容:" prop="value">
                    <el-input v-model="cacheForm.value" type="textarea" :rows="12" :readOnly="true"/>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script>
import {getCache, getKeyDefineList, getKeyList, getKeyValue, deleteKey, deleteKeys} from "@/api/infra/redis";
import echarts from "echarts";

export default {
  name: "Server",
  data () {
    return {
      // 统计命令信息
      commandstats: null,
      // 使用内存
      usedmemory: null,
      // cache 信息
      cache: [],
      // key 列表
      keyDefineListLoad: true,
      keyDefineList: [],
      // 模块弹出框
      open: false,
      keyTemplate: "",
      cacheKeys: [],
      cacheForm: {}
    };
  },
  created () {
    this.getList();
    this.openLoading();
  },
  methods: {
    /** 查缓存询信息 */
    getList () {
      // 查询 Redis 监控信息
      getCache().then((response) => {
        this.cache = response.data;
        this.$modal.closeLoading();

        this.commandstats = echarts.init(this.$refs.commandstats, "macarons");
        const commandStats = [];
        response.data.commandStats.forEach(row => {
          commandStats.push({
            name: row.command,
            value: row.calls
          });
        })
        this.commandstats.setOption({
          tooltip: {
            trigger: "item",
            formatter: "{a} <br/>{b} : {c} ({d}%)",
          },
          series: [
            {
              name: "命令",
              type: "pie",
              roseType: "radius",
              radius: [15, 95],
              center: ["50%", "38%"],
              data: commandStats,
              animationEasing: "cubicInOut",
              animationDuration: 1000,
            },
          ],
        });
        this.usedmemory = echarts.init(this.$refs.usedmemory, "macarons");
        this.usedmemory.setOption({
          tooltip: {
            formatter: "{b} <br/>{a} : " + this.cache.info.used_memory_human,
          },
          series: [
            {
              name: "峰值",
              type: "gauge",
              min: 0,
              max: 1000,
              detail: {
                formatter: this.cache.info.used_memory_human,
              },
              data: [
                {
                  value: parseFloat(this.cache.info.used_memory_human),
                  name: "内存消耗",
                },
              ],
            },
          ],
        });
      });

      // 查询 Redis Key 列表
      getKeyDefineList().then(response => {
        this.keyDefineList = response.data;
        this.keyDefineListLoad = false;
      });
    },

    // 打开加载层
    openLoading () {
      this.$modal.loading("正在加载缓存监控数据，请稍后！");
    },

    // 打开缓存弹窗
    openKeyTemplate (keyDefine) {
      this.open = true;
      // 加载键名列表
      this.keyTemplate = keyDefine.keyTemplate;
      this.doGetKeyList(this.keyTemplate);
    },

    // 获取键名列表
    doGetKeyList (keyTemplate) {
      getKeyList(keyTemplate).then(response => {
        this.cacheKeys = response.data
        this.cacheForm = {}
      })
    },

    // 获取缓存值
    handleKeyValue (key) {
      getKeyValue(key).then(response => {
        this.cacheForm = response.data
      })
    },

    // 刷新键名列表
    refreshKeys() {
      this.$modal.msgSuccess("刷新键名列表成功");
      this.doGetKeyList(this.keyTemplate);
    },

    // 删除缓存
    handleDeleteKey(key){
      deleteKey(key).then(response => {
        this.$modal.msgSuccess("清理缓存键名[" + key + "]成功");
        this.doGetKeyList(this.keyTemplate);
      })
    },
    handleDeleteKeys(keyTemplate){
      deleteKeys(keyTemplate).then(response => {
        this.$modal.msgSuccess("清空[" + keyTemplate + "]成功");
        this.doGetKeyList(this.keyTemplate);
      })
    },
  },
};
</script>
