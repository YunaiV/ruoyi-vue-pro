import { required as requiredRule } from '@/utils/formRules'
import dayjs from 'dayjs'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n() // 国际化

export class FormSchemaBuilder {
  static input(label: string, field: string, required: Boolean = false): FormSchema {
    return {
      label,
      field,
      component: 'Input',
      formItemProps: {
        rules: required ? [requiredRule] : []
      }
    }
  }
  static inputNumber(
    label: string,
    field: string,
    value: number,
    required: Boolean = false
  ): FormSchema {
    return {
      label,
      field,
      value,
      component: 'InputNumber',
      formItemProps: {
        rules: required ? [requiredRule] : []
      }
    }
  }
  static radioButton(
    label: string,
    field: string,
    value: number,
    options: ComponentOptions[],
    required: Boolean = false
  ): FormSchema {
    return {
      label,
      field,
      component: 'RadioButton',
      value,
      formItemProps: {
        rules: required ? [requiredRule] : []
      },
      componentProps: {
        options
      }
    }
  }
  static select(
    label: string,
    field: string,
    value: number | null,
    options: ComponentOptions[],
    required: Boolean = false
  ): FormSchema {
    return {
      label,
      field,
      component: 'Select',
      value,
      formItemProps: {
        rules: required ? [requiredRule] : []
      },
      componentProps: {
        options
      }
    }
  }
  static textarea(
    label: string,
    field: string,
    rows: number,
    span: number,
    required: Boolean = false
  ): FormSchema {
    return {
      label,
      field,
      component: 'Input',
      componentProps: {
        type: 'textarea',
        rows: rows
      },
      formItemProps: {
        rules: required ? [requiredRule] : []
      },
      colProps: {
        span: span
      }
    }
  }
}

export class TableColumnBuilder {
  static column(label: string, field: string): TableColumn {
    return {
      label,
      field
    }
  }
  static date(label: string, field: string, template?: string): TableColumn {
    return {
      label,
      field,
      formatter: (_: Recordable, __: TableColumn, cellValue: string) => {
        return dayjs(cellValue).format(template || 'YYYY-MM-DD HH:mm:ss')
      }
    }
  }
  static action(width: number): TableColumn {
    return {
      label: t('table.action'),
      field: 'action',
      width: width + 'px'
    }
  }
}

export class ComponentOptionsBuilder {
  static option(label: string, value: number): ComponentOptions {
    return {
      label,
      value
    }
  }
}
