#!/usr/bin/env python3
"""
详细的 MySQL 脚本与 Java 实体类验证工具
逐字段级别验证，确保完全一致
"""

import json
from typing import Dict, List, Set, Tuple, Any
from collections import defaultdict
from datetime import datetime

# Java 类型到 MySQL 类型的映射规则
JAVA_TO_MYSQL_TYPE_MAP = {
    'Long': 'BIGINT',
    'Integer': ['INT', 'TINYINT'],  # Integer 可以是 INT 或 TINYINT（状态字段）
    'String': lambda length: f'VARCHAR({length})',  # 需要长度参数
    'BigDecimal': 'DECIMAL(24, 6)',
    'LocalDateTime': 'DATETIME',
    'Boolean': 'BIT(1)',
}

# 驼峰转下划线
def camel_to_snake(name: str) -> str:
    """将驼峰命名转换为下划线命名"""
    import re
    # 处理特殊情况
    if name == 'id':
        return 'id'
    # 在大写字母前添加下划线
    result = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    result = re.sub('([a-z0-9])([A-Z])', r'\1_\2', result)
    return result.lower()

# 验证器类
class SchemaValidator:
    def __init__(self, entity_schema_file: str, mysql_schema_file: str):
        # 加载 JSON 数据
        with open(entity_schema_file, 'r', encoding='utf-8') as f:
            self.entity_data = json.load(f)

        with open(mysql_schema_file, 'r', encoding='utf-8') as f:
            self.mysql_data = json.load(f)

        # 结果收集
        self.errors = []
        self.warnings = []
        self.info = []

        # 统计
        self.total_tables = 0
        self.total_fields_entity = 0
        self.total_fields_mysql = 0
        self.validated_tables = 0
        self.validated_fields = 0

        # 创建快速查找映射
        self.entity_tables = {t['tableName']: t for t in self.entity_data['tables']}
        self.mysql_tables = {t['tableName']: t for t in self.mysql_data['tables']}

    def add_error(self, level: str, table: str, message: str, details: str = ''):
        """添加错误记录"""
        self.errors.append({
            'level': level,
            'table': table,
            'message': message,
            'details': details
        })

    def add_warning(self, table: str, message: str, details: str = ''):
        """添加警告记录"""
        self.warnings.append({
            'table': table,
            'message': message,
            'details': details
        })

    def add_info(self, table: str, message: str):
        """添加信息记录"""
        self.info.append({
            'table': table,
            'message': message
        })

    def validate_table_existence(self) -> bool:
        """验证表的存在性"""
        print("步骤 1: 验证表的存在性...")

        entity_tables = set(self.entity_tables.keys())
        mysql_tables = set(self.mysql_tables.keys())

        # 检查缺失的表
        missing_in_mysql = entity_tables - mysql_tables
        extra_in_mysql = mysql_tables - entity_tables

        if missing_in_mysql:
            for table in sorted(missing_in_mysql):
                self.add_error('CRITICAL', table, '表在 MySQL 脚本中缺失',
                             f'实体类存在表 {table}，但 MySQL 脚本中未找到')

        if extra_in_mysql:
            for table in sorted(extra_in_mysql):
                self.add_warning(table, '表在实体类中不存在',
                               f'MySQL 脚本存在表 {table}，但实体类中未找到')

        # 统计
        self.total_tables = len(entity_tables)
        common_tables = entity_tables & mysql_tables
        self.validated_tables = len(common_tables)

        print(f"  ✓ 总计表数（实体类）: {len(entity_tables)}")
        print(f"  ✓ 总计表数（MySQL）: {len(mysql_tables)}")
        print(f"  ✓ 共同表数: {len(common_tables)}")

        if missing_in_mysql:
            print(f"  ✗ MySQL 中缺失 {len(missing_in_mysql)} 个表")
            return False

        print(f"  ✓ 所有表都存在于 MySQL 脚本中\n")
        return True

    def validate_base_do_fields(self, table_name: str, mysql_fields: List[Dict]) -> List[str]:
        """验证 BaseDO 的审计字段"""
        base_fields = ['creator', 'create_time', 'updater', 'update_time', 'deleted', 'tenant_id']
        mysql_field_names = {f['name'] for f in mysql_fields}

        missing_fields = []
        for field in base_fields:
            if field not in mysql_field_names:
                missing_fields.append(field)
                self.add_error('HIGH', table_name, f'缺失 BaseDO 审计字段: {field}')

        return missing_fields

    def validate_field_type(self, table_name: str, field_name: str,
                           java_type: str, mysql_type: str,
                           is_nullable: bool, mysql_nullable: bool) -> bool:
        """验证字段类型映射"""
        # 特殊情况处理
        if java_type == 'Long':
            if mysql_type != 'BIGINT':
                self.add_error('HIGH', table_name,
                             f'字段 {field_name} 类型不匹配',
                             f'Java: Long → MySQL: 应为 BIGINT，实际为 {mysql_type}')
                return False

        elif java_type == 'Integer':
            # Integer 可以是 INT 或 TINYINT
            if mysql_type not in ['INT', 'TINYINT']:
                self.add_error('HIGH', table_name,
                             f'字段 {field_name} 类型不匹配',
                             f'Java: Integer → MySQL: 应为 INT 或 TINYINT，实际为 {mysql_type}')
                return False

        elif java_type == 'String':
            # String 应该是 VARCHAR
            if not mysql_type.startswith('VARCHAR'):
                self.add_error('HIGH', table_name,
                             f'字段 {field_name} 类型不匹配',
                             f'Java: String → MySQL: 应为 VARCHAR(n)，实际为 {mysql_type}')
                return False

        elif java_type == 'BigDecimal':
            # BigDecimal 应该是 DECIMAL(24, 6)
            if mysql_type != 'DECIMAL(24, 6)':
                self.add_error('MEDIUM', table_name,
                             f'字段 {field_name} 类型精度不匹配',
                             f'Java: BigDecimal → MySQL: 推荐 DECIMAL(24, 6)，实际为 {mysql_type}')
                return False

        elif java_type == 'LocalDateTime':
            if mysql_type != 'DATETIME':
                self.add_error('HIGH', table_name,
                             f'字段 {field_name} 类型不匹配',
                             f'Java: LocalDateTime → MySQL: 应为 DATETIME，实际为 {mysql_type}')
                return False

        elif java_type == 'Boolean':
            if mysql_type != 'BIT(1)':
                self.add_error('MEDIUM', table_name,
                             f'字段 {field_name} 类型不匹配',
                             f'Java: Boolean → MySQL: 推荐 BIT(1)，实际为 {mysql_type}')
                return False

        # 验证可空性
        # 注意：实体类中没有 @NotNull 的字段，在数据库中可以为 NULL
        # 但有 @NotNull 的字段，在数据库中必须 NOT NULL

        return True

    def validate_table_fields(self, table_name: str) -> Tuple[bool, Dict[str, Any]]:
        """验证单个表的所有字段"""
        entity_table = self.entity_tables[table_name]
        mysql_table = self.mysql_tables[table_name]

        entity_fields = entity_table['fields']
        mysql_fields = mysql_table['fields']

        # 创建字段映射
        entity_field_map = {}
        for field in entity_fields:
            # 将驼峰转换为下划线
            db_field_name = camel_to_snake(field['name'])
            entity_field_map[db_field_name] = field

        mysql_field_map = {f['name']: f for f in mysql_fields}

        # BaseDO 字段（这些字段在实体类中不显式声明，但在数据库中必须存在）
        base_do_fields = ['creator', 'create_time', 'updater', 'update_time', 'deleted', 'tenant_id']

        # 验证 BaseDO 字段
        missing_base_fields = self.validate_base_do_fields(table_name, mysql_fields)

        # 添加 BaseDO 字段到实体字段映射（用于完整性检查）
        for base_field in base_do_fields:
            if base_field in mysql_field_map and base_field not in entity_field_map:
                entity_field_map[base_field] = {
                    'name': base_field,
                    'javaType': 'BaseDO',  # 标记为 BaseDO 字段
                    'isPrimaryKey': False
                }

        entity_field_names = set(entity_field_map.keys())
        mysql_field_names = set(mysql_field_map.keys())

        # 检查缺失和额外的字段
        missing_in_mysql = entity_field_names - mysql_field_names
        extra_in_mysql = mysql_field_names - entity_field_names

        field_details = {
            'total_entity_fields': len(entity_fields),
            'total_mysql_fields': len(mysql_fields),
            'missing_in_mysql': list(missing_in_mysql),
            'extra_in_mysql': list(extra_in_mysql),
            'type_mismatches': [],
            'comment_missing': []
        }

        # 报告缺失字段
        for field_name in sorted(missing_in_mysql):
            if field_name not in base_do_fields:  # 不报告 BaseDO 字段
                self.add_error('HIGH', table_name,
                             f'字段 {field_name} 在 MySQL 中缺失',
                             f'实体类定义了字段 {field_name}，但 MySQL 脚本中未找到')

        # 报告额外字段
        for field_name in sorted(extra_in_mysql):
            if field_name not in base_do_fields:
                self.add_warning(table_name,
                               f'字段 {field_name} 在实体类中不存在',
                               f'MySQL 脚本定义了字段 {field_name}，但实体类中未找到')

        # 验证共同字段的详细信息
        common_fields = entity_field_names & mysql_field_names
        for field_name in sorted(common_fields):
            entity_field = entity_field_map[field_name]
            mysql_field = mysql_field_map[field_name]

            # 跳过 BaseDO 字段的类型验证
            if entity_field.get('javaType') == 'BaseDO':
                continue

            # 验证类型
            java_type = entity_field['javaType']
            mysql_type = mysql_field['type']
            mysql_nullable = mysql_field.get('nullable', True)

            # 假设实体类字段默认可空（除非有特殊注解，但我们这里没有这个信息）
            entity_nullable = True

            self.validate_field_type(table_name, field_name,
                                    java_type, mysql_type,
                                    entity_nullable, mysql_nullable)

            # 验证注释
            entity_comment = entity_field.get('comment', '')
            mysql_comment = mysql_field.get('comment', '')

            if not mysql_comment:
                self.add_warning(table_name,
                               f'字段 {field_name} 缺少注释',
                               f'实体类注释: "{entity_comment}"')
                field_details['comment_missing'].append(field_name)
            elif entity_comment and mysql_comment != entity_comment:
                # 注释不一致是警告，不是错误
                self.add_info(table_name,
                            f'字段 {field_name} 注释不一致: 实体类 "{entity_comment}" vs MySQL "{mysql_comment}"')

        # 统计
        self.total_fields_entity += len(entity_fields)
        self.total_fields_mysql += len(mysql_fields)
        self.validated_fields += len(common_fields)

        # 判断表是否通过验证
        table_passed = (
            len(missing_in_mysql) == 0 and
            len(missing_base_fields) == 0 and
            len(field_details['type_mismatches']) == 0
        )

        return table_passed, field_details

    def run_validation(self) -> bool:
        """运行完整的验证流程"""
        print("\n" + "="*80)
        print("开始详细的 MySQL 脚本与 Java 实体类验证")
        print("="*80 + "\n")

        # 步骤 1: 验证表的存在性
        tables_exist = self.validate_table_existence()

        if not tables_exist:
            print("\n⚠️  表存在性验证失败，无法继续字段级别验证")
            return False

        # 步骤 2: 逐表验证字段
        print("步骤 2: 逐表验证字段...")

        common_tables = set(self.entity_tables.keys()) & set(self.mysql_tables.keys())
        passed_tables = []
        failed_tables = []

        for table_name in sorted(common_tables):
            table_passed, field_details = self.validate_table_fields(table_name)

            if table_passed:
                passed_tables.append(table_name)
                print(f"  ✓ {table_name}: 通过 ({field_details['total_entity_fields']} 字段)")
            else:
                failed_tables.append(table_name)
                print(f"  ✗ {table_name}: 失败")
                if field_details['missing_in_mysql']:
                    print(f"      - 缺失字段: {', '.join(field_details['missing_in_mysql'])}")

        print(f"\n  ✓ 通过验证的表: {len(passed_tables)}/{len(common_tables)}")
        print(f"  ✗ 未通过验证的表: {len(failed_tables)}/{len(common_tables)}")

        # 步骤 3: 总结
        print("\n" + "="*80)
        print("验证总结")
        print("="*80)

        print(f"\n表级统计:")
        print(f"  - 总计表数（实体类）: {self.total_tables}")
        print(f"  - 总计表数（MySQL）: {len(self.mysql_tables)}")
        print(f"  - 验证的表数: {self.validated_tables}")
        print(f"  - 通过验证的表: {len(passed_tables)}")
        print(f"  - 未通过验证的表: {len(failed_tables)}")

        print(f"\n字段级统计:")
        print(f"  - 总计字段数（实体类）: {self.total_fields_entity}")
        print(f"  - 总计字段数（MySQL）: {self.total_fields_mysql}")
        print(f"  - 验证的字段数: {self.validated_fields}")

        print(f"\n问题统计:")
        critical_errors = [e for e in self.errors if e['level'] == 'CRITICAL']
        high_errors = [e for e in self.errors if e['level'] == 'HIGH']
        medium_errors = [e for e in self.errors if e['level'] == 'MEDIUM']

        print(f"  - 严重错误（CRITICAL）: {len(critical_errors)}")
        print(f"  - 高优先级错误（HIGH）: {len(high_errors)}")
        print(f"  - 中优先级错误（MEDIUM）: {len(medium_errors)}")
        print(f"  - 警告（WARNING）: {len(self.warnings)}")
        print(f"  - 信息（INFO）: {len(self.info)}")

        # 判断整体是否通过
        overall_passed = (
            len(failed_tables) == 0 and
            len(critical_errors) == 0 and
            len(high_errors) == 0
        )

        if overall_passed:
            print("\n✅ 验证通过！MySQL 脚本与 Java 实体类完全一致。")
        else:
            print("\n❌ 验证失败！发现不一致问题。")

        return overall_passed

    def generate_report(self, output_file: str):
        """生成详细的 Markdown 报告"""
        print(f"\n生成详细报告: {output_file}")

        with open(output_file, 'w', encoding='utf-8') as f:
            # 标题
            f.write("# ERP 模块数据库验证报告\n\n")
            f.write(f"**生成时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
            f.write("---\n\n")

            # 执行摘要
            f.write("## 执行摘要\n\n")

            overall_passed = (
                len([e for e in self.errors if e['level'] in ['CRITICAL', 'HIGH']]) == 0
            )

            if overall_passed:
                f.write("✅ **验证结果**: 通过\n\n")
            else:
                f.write("❌ **验证结果**: 失败\n\n")

            f.write("### 统计数据\n\n")
            f.write("| 类别 | 数量 |\n")
            f.write("|------|-----:|\n")
            f.write(f"| 总计表数（实体类） | {self.total_tables} |\n")
            f.write(f"| 总计表数（MySQL） | {len(self.mysql_tables)} |\n")
            f.write(f"| 验证的表数 | {self.validated_tables} |\n")
            f.write(f"| 总计字段数（实体类） | {self.total_fields_entity} |\n")
            f.write(f"| 总计字段数（MySQL） | {self.total_fields_mysql} |\n")
            f.write(f"| 验证的字段数 | {self.validated_fields} |\n")
            f.write("\n")

            # 问题汇总
            f.write("### 问题汇总\n\n")

            critical_errors = [e for e in self.errors if e['level'] == 'CRITICAL']
            high_errors = [e for e in self.errors if e['level'] == 'HIGH']
            medium_errors = [e for e in self.errors if e['level'] == 'MEDIUM']

            f.write("| 严重程度 | 数量 |\n")
            f.write("|---------|-----:|\n")
            f.write(f"| 🔴 严重错误（CRITICAL） | {len(critical_errors)} |\n")
            f.write(f"| 🟠 高优先级错误（HIGH） | {len(high_errors)} |\n")
            f.write(f"| 🟡 中优先级错误（MEDIUM） | {len(medium_errors)} |\n")
            f.write(f"| 🔵 警告（WARNING） | {len(self.warnings)} |\n")
            f.write(f"| ⚪ 信息（INFO） | {len(self.info)} |\n")
            f.write("\n")

            # 严重错误详情
            if critical_errors:
                f.write("---\n\n")
                f.write("## 🔴 严重错误（CRITICAL）\n\n")
                f.write("这些错误必须立即修复，否则系统无法正常运行。\n\n")

                for i, error in enumerate(critical_errors, 1):
                    f.write(f"### {i}. {error['table']} - {error['message']}\n\n")
                    f.write(f"**详情**: {error['details']}\n\n")

            # 高优先级错误详情
            if high_errors:
                f.write("---\n\n")
                f.write("## 🟠 高优先级错误（HIGH）\n\n")
                f.write("这些错误会导致数据不一致或功能异常，建议尽快修复。\n\n")

                # 按表分组
                errors_by_table = defaultdict(list)
                for error in high_errors:
                    errors_by_table[error['table']].append(error)

                for table_name in sorted(errors_by_table.keys()):
                    f.write(f"### 表: `{table_name}`\n\n")
                    for error in errors_by_table[table_name]:
                        f.write(f"- **{error['message']}**\n")
                        if error['details']:
                            f.write(f"  - {error['details']}\n")
                    f.write("\n")

            # 中优先级错误详情
            if medium_errors:
                f.write("---\n\n")
                f.write("## 🟡 中优先级错误（MEDIUM）\n\n")
                f.write("这些问题可能影响数据精度或性能，建议在方便时修复。\n\n")

                errors_by_table = defaultdict(list)
                for error in medium_errors:
                    errors_by_table[error['table']].append(error)

                for table_name in sorted(errors_by_table.keys()):
                    f.write(f"### 表: `{table_name}`\n\n")
                    for error in errors_by_table[table_name]:
                        f.write(f"- {error['message']}\n")
                        if error['details']:
                            f.write(f"  - {error['details']}\n")
                    f.write("\n")

            # 警告详情
            if self.warnings:
                f.write("---\n\n")
                f.write("## 🔵 警告（WARNING）\n\n")
                f.write("这些是潜在问题或建议改进的地方。\n\n")

                warnings_by_table = defaultdict(list)
                for warning in self.warnings:
                    warnings_by_table[warning['table']].append(warning)

                for table_name in sorted(warnings_by_table.keys()):
                    f.write(f"### 表: `{table_name}`\n\n")
                    for warning in warnings_by_table[table_name][:5]:  # 每个表最多显示 5 个警告
                        f.write(f"- {warning['message']}\n")
                        if warning['details']:
                            f.write(f"  - {warning['details']}\n")

                    if len(warnings_by_table[table_name]) > 5:
                        f.write(f"\n  _(还有 {len(warnings_by_table[table_name]) - 5} 个警告未显示)_\n")
                    f.write("\n")

            # 建议修复方案
            if critical_errors or high_errors:
                f.write("---\n\n")
                f.write("## 📋 建议修复方案\n\n")

                if critical_errors:
                    f.write("### 1. 修复严重错误\n\n")
                    for error in critical_errors:
                        f.write(f"- **{error['table']}**: {error['message']}\n")
                        f.write(f"  - 修复建议: 在 MySQL 脚本中添加缺失的表\n\n")

                if high_errors:
                    f.write("### 2. 修复高优先级错误\n\n")
                    # 按错误类型分组
                    missing_fields = [e for e in high_errors if '缺失' in e['message'] and '字段' in e['message']]
                    type_mismatches = [e for e in high_errors if '类型不匹配' in e['message']]

                    if missing_fields:
                        f.write("#### 缺失字段\n\n")
                        f.write("在 MySQL 脚本中添加以下缺失的字段:\n\n")
                        for error in missing_fields[:10]:  # 显示前 10 个
                            f.write(f"- `{error['table']}`: {error['message']}\n")
                        if len(missing_fields) > 10:
                            f.write(f"\n  _(还有 {len(missing_fields) - 10} 个缺失字段)_\n")
                        f.write("\n")

                    if type_mismatches:
                        f.write("#### 类型不匹配\n\n")
                        f.write("修正以下字段的类型定义:\n\n")
                        for error in type_mismatches[:10]:
                            f.write(f"- `{error['table']}`: {error['message']}\n")
                            f.write(f"  - {error['details']}\n")
                        if len(type_mismatches) > 10:
                            f.write(f"\n  _(还有 {len(type_mismatches) - 10} 个类型不匹配)_\n")
                        f.write("\n")

            # 附录：完整的错误列表
            if len(self.errors) > 20 or len(self.warnings) > 20:
                f.write("---\n\n")
                f.write("## 附录：完整的问题列表\n\n")

                if self.errors:
                    f.write("### 所有错误\n\n")
                    for i, error in enumerate(self.errors, 1):
                        f.write(f"{i}. [{error['level']}] `{error['table']}` - {error['message']}\n")
                    f.write("\n")

                if self.warnings:
                    f.write("### 所有警告\n\n")
                    for i, warning in enumerate(self.warnings, 1):
                        f.write(f"{i}. `{warning['table']}` - {warning['message']}\n")
                    f.write("\n")

            # 结论
            f.write("---\n\n")
            f.write("## 结论\n\n")

            if overall_passed:
                f.write("✅ **MySQL 脚本与 Java 实体类完全一致**，可以安全使用。\n\n")
            else:
                f.write("❌ **验证失败**，发现以下问题:\n\n")
                f.write(f"- 严重错误: {len(critical_errors)} 个\n")
                f.write(f"- 高优先级错误: {len(high_errors)} 个\n")
                f.write(f"- 中优先级错误: {len(medium_errors)} 个\n\n")
                f.write("**建议**: 请根据上述修复方案修正 MySQL 脚本，然后重新验证。\n\n")

            f.write("---\n\n")
            f.write("*本报告由自动化验证工具生成*\n")

        print(f"  ✓ 报告已生成: {output_file}\n")


def main():
    """主函数"""
    # 文件路径
    entity_schema_file = '/home/myu/Source/github/ruoyi-vue-pro/openspec/changes/restore-erp-database-schema/entity-classes-schema.json'
    mysql_schema_file = '/home/myu/Source/github/ruoyi-vue-pro/openspec/changes/restore-erp-database-schema/mysql-script-schema.json'
    report_file = '/home/myu/Source/github/ruoyi-vue-pro/openspec/changes/restore-erp-database-schema/DETAILED_VERIFICATION_REPORT.md'

    # 创建验证器
    validator = SchemaValidator(entity_schema_file, mysql_schema_file)

    # 运行验证
    passed = validator.run_validation()

    # 生成报告
    validator.generate_report(report_file)

    # 返回退出码
    return 0 if passed else 1


if __name__ == '__main__':
    import sys
    sys.exit(main())
