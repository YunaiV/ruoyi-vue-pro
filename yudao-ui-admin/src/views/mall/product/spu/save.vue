<template>
  <div class="container">
    <el-tabs v-model="activeName" class="tabs">
      <!-- 基础设置 -->
      <el-tab-pane label="基础设置" name="base">
        <el-form ref="baseForm" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="baseForm.name" placeholder="请输入商品名称"/>
          </el-form-item>
          <el-form-item label="卖点" prop="sellPoint">
            <el-input v-model="baseForm.sellPoint" placeholder="请输入卖点"/>
          </el-form-item>
          <el-form-item label="分类id" prop="categoryIds">
            <el-cascader
              v-model="baseForm.categoryIds"
              placeholder="请输入分类id"
              style="width: 100%"
              :options="categoryList"
              :props="propName"
              clearable
            ></el-cascader>
          </el-form-item>
          <el-form-item label="商品主图" prop="bannerUrl">
            <ImageUpload v-model="baseForm.bannerUrl" :limit="1"/>
          </el-form-item>
          <el-form-item label="商品轮播图" prop="picUrls">
            <ImageUpload v-model="baseForm.picUrls" :limit="10"/>
          </el-form-item>
          <el-form-item label="排序字段" prop="sort">
            <el-input v-model="baseForm.sort" placeholder="请输入排序字段"/>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 价格库存 -->
      <el-tab-pane label="价格库存" name="rates" class="rates">
        <el-form :model="ratesForm" :rules="rules.ratesForm">
          <el-form-item label="商品规格">
            <el-radio-group v-model="ratesForm.spec" @change="changeRadio">
              <el-radio :label="0">单规格</el-radio>
              <el-radio :label="1">多规格</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 动态添加规格属性 -->
          <div v-show="ratesForm.spec === 1">
            <div
              v-for="(specs, index) in dynamicSpec"
              :key="index"
              class="dynamic-spec"
            >
              <!-- 删除按钮 -->
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                class="spec-delete"
                @click="dynamicSpec.splice(index, 1)"
              ></el-button>

              <div class="spec-header">
                规格项：
                <el-select v-model="specs.specId" filterable placeholder="请选择" @change="changeSpec">
                  <el-option
                    v-for="item in propertyPageList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id">
                  </el-option>
                </el-select>
              </div>
              <div class="spec-values">
                <template v-for="(v, i) in specs.specValue">
                  <el-input v-model="v.name" class="spec-value" :key="i"/>
                </template>
                <!-- <el-button
                  type="primary"
                  icon="el-icon-plus"
                  circle
                  @click="dialogForSpec = true; currentSpec = index"
                ></el-button> -->
              </div>
            </div>
            <el-button type="primary" @click="dynamicSpec.push({specValue: []}); ratesForm.rates = []"
            >添加规格项目
            </el-button
            >
          </div>

          <!-- 规格明细 -->
          <el-form-item label="规格明细">
            <el-table :data="ratesForm.rates" border style="width: 100%" ref="rates">

              <template v-if="ratesForm.spec == 1">
                <el-table-column :key="index" v-for="(item, index) in dynamicSpec.filter(v=>v.specName != undefined)"
                                 :label="item.specName">
                  <template slot-scope="scope">
                    <el-input
                      v-if="scope.row.spec"
                      v-model="scope.row.spec[index]"
                      disabled
                    ></el-input>
                  </template>
                </el-table-column>
              </template>

              <el-table-column label="规格图片" width="120px">
                <template slot-scope="scope">
                  <ImageUpload v-model="scope.row.picUrl" :limit="1" :isShowTip="false"
                               style="width: 100px; height: 50px"/>
                </template>
              </el-table-column>
              <el-table-column label="市场价(元)">
                <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.marketPrice"
                    type="number"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column label="销售价(元)" re>
                <template slot-scope="scope">
                  <el-input v-model="scope.row.price" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="成本价">
                <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.costPrice"
                    type="number"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column label="库存">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.stock" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="体积">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.volume" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="重量">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.weight" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="条码">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.barCode"></el-input>
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 商品详情 -->
      <el-tab-pane label="商品描述" name="third">
        <editor v-model="baseForm.description" :min-height="400"/>
      </el-tab-pane>

      <!-- 销售设置 -->
      <el-tab-pane label="销售设置" name="fourth">
        <el-form ref="baseForm" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
          <el-form-item label="库存数量" prop="totalStock">
            <el-input v-model="baseForm.totalStock" placeholder="请输入库存数量"/>
          </el-form-item>
          <el-form-item label="上下架状态" prop="status">
            <el-radio-group v-model="baseForm.status">
              <el-radio :label="0">上架</el-radio>
              <el-radio :label="1">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <div class="buttons">
      <el-button type="info" round @click="cancel">取消</el-button>
      <el-button type="success" round @click="submit">确认</el-button>
    </div>

  </div>
</template>

<script>
import {getProductCategoryList} from "@/api/mall/product/category";
import {createSpu,} from "@/api/mall/product/spu";
import {getPropertyPage,} from "@/api/mall/product/property";
import Editor from "@/components/Editor";
import ImageUpload from "@/components/ImageUpload";

export default {
  components: {
    Editor,
    ImageUpload
  },
  data() {
    return {
      activeName: "base",
      propName: {
        checkStrictly: true,
        label: "name",
        value: "id",
      },
      // 基础设置
      baseForm: {
        name: null,
        sellPoint: null,
        categoryIds: null,
        sort: null,
        description: null,
        bannerUrl: null,
        picUrls: [],
        totalStock: null,
        status: 0
      },
      categoryList: [],
      // 价格库存
      ratesForm: {
        spec: 0,
        // 规格明细
        rates: [{}]
      },
      dynamicSpec: [
        // {
        //   specId: 86,
        //   specName: "颜色",
        //   specValue:[{
        //      name: "红色",
        //      id: 225,
        //   }]
        // },
      ],
      propertyPageList: [],
      specValue: null,

      // 表单校验
      rules: {
        description: [
          {required: true, message: "描述不能为空", trigger: "blur"},
        ],
        categoryIds: [
          {required: true, message: "分类id不能为空", trigger: "blur"},
        ],
        bannerUrl: [{required: true, message: "商品主图地址", trigger: "blur"}],
        sort: [
          {required: true, message: "排序字段不能为空", trigger: "blur"},
        ],
      },
    };
  },
  created() {
    this.getListCategory();
    this.getPropertyPageList();
  },
  methods: {
    changeRadio() {

      this.$refs.rates.doLayout()
      if (this.ratesForm.spec == 0) {
        this.ratesForm.rates = [{}]
      } else {
        this.ratesForm.rates = []
        if (this.dynamicSpec.length > 0) {
          this.buildRatesFormRates()
        }
      }
    },
    // 构建规格明细笛卡尔积
    buildRatesFormRates() {
      let rates = [];
      this.dynamicSpec.map(v => v.specValue.map(m => m.name))
        .reduce((last, current) => {
          const array = [];
          last.forEach(par1 => {
            current.forEach(par2 => {
              let v
              if (par1 instanceof Array) {
                v = par1.concat(par2)
              } else {
                v = [par1, par2];
              }
              array.push(v)
            });
          });
          return array;
        })
        .forEach(v => {
          rates.push({spec: v})
        });
      this.ratesForm.rates = rates
    },
    /** 查询分类 */
    getListCategory() {
      // 执行查询
      getProductCategoryList().then((response) => {
        this.categoryList = this.handleTree(response.data, "id", "parentId");
      });
    },
    cancel() {
      this.$emit("closeDialog");
    },
    submit() {
      let rates = this.ratesForm.rates;
      // 动态规格调整字段
      if (this.ratesForm.spec == 1) {
        rates.forEach(r => {
          let properties = []
          r.spec.forEach((v, i) => {
            let specValue = this.dynamicSpec[i].specValue.find(o => o.name == v);
            let propertie = {};
            propertie.propertyId = this.dynamicSpec[i].specId;
            propertie.valueId = specValue.id;
            properties.push(propertie);
          })
          r.properties = properties;
        })
      }
      this.baseForm.skus = rates;
      this.baseForm.specType = this.ratesForm.spec;
      this.baseForm.categoryId = this.baseForm.categoryIds[this.baseForm.categoryIds.length - 1];
      console.log(this.baseForm)
      createSpu(this.baseForm).then((response) => {
        this.$modal.msgSuccess("新增成功");
        this.open = false;
        this.getList();
        this.$emit("closeDialog");
      });
    },
    /** 查询规格 */
    getPropertyPageList() {
      // 执行查询
      getPropertyPage().then((response) => {
        this.propertyPageList = response.data.list;
      });
    },
    changeSpec(val) {
      let obj = this.propertyPageList.find(o => o.id == val);
      let dynamicSpec = this.dynamicSpec;
      let spec = dynamicSpec.find(o => o.specId == val)
      spec.specId = obj.id;
      spec.specName = obj.name;
      spec.specValue = obj.propertyValueList;
      this.dynamicSpec = dynamicSpec;
      this.buildRatesFormRates();
    }
  },
};
</script>

<style lang="scss">
.spec-dialog {
  width: 400px;
  height: 300px;
}

.dynamic-spec {
  background-color: #f2f2f2;
  width: 85%;
  margin: auto;
  margin-bottom: 10px;

  .spec-header {
    padding: 30px;
    padding-bottom: 20px;

    .spec-name {
      display: inline;

      input {
        width: 30%;
      }
    }
  }

  .spec-values {
    width: 84%;
    padding: 25px;
    margin: auto;
    padding-top: 5px;

    .spec-value {
      display: inline-block;
      margin-right: 10px;
      margin-bottom: 10px;
      width: 13%;

    }
  }

  .spec-delete {
    float: right;
    margin-top: 10px;
    margin-right: 10px;
  }
}

.tabs {
  height: 500px;
  border-bottom: 2px solid #f2f2f2;

  .el-tab-pane {
    height: 445px;
    overflow-y: auto;
  }
}

// 库存价格图片样式修改
.rates {
  .component-upload-image {
    margin: auto;
  }

  .el-upload--picture-card {
    width: 100px;
    height: 50px;
    line-height: 60px;
    margin: auto;
  }

  .el-upload-list__item {
    width: 100px !important;
    height: 50px !important;
  }
}

.buttons {
  margin-top: 20px;
  height: 36px;

  button {
    float: right;
    margin-left: 15px;
  }
}
</style>
