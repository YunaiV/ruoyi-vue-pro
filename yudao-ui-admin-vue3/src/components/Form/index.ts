import Form from './src/Form.vue'
import { ElForm } from 'element-plus'
import { FormSchema, FormSetPropsType } from '@/types/form'

export interface FormExpose {
  setValues: (data: Recordable) => void
  setProps: (props: Recordable) => void
  delSchema: (field: string) => void
  addSchema: (formSchema: FormSchema, index?: number) => void
  setSchema: (schemaProps: FormSetPropsType[]) => void
  formModel: Recordable
  getElFormRef: () => ComponentRef<typeof ElForm>
}

export { Form }
