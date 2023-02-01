import type { Form, FormExpose } from '@/components/Form'
import type { ElForm } from 'element-plus'
import type { FormProps } from '@/components/Form/src/types'
import { FormSchema, FormSetPropsType } from '@/types/form'

export const useForm = (props?: FormProps) => {
  // From实例
  const formRef = ref<typeof Form & FormExpose>()

  // ElForm实例
  const elFormRef = ref<ComponentRef<typeof ElForm>>()

  /**
   * @param ref Form实例
   * @param elRef ElForm实例
   */
  const register = (ref: typeof Form & FormExpose, elRef: ComponentRef<typeof ElForm>) => {
    formRef.value = ref
    elFormRef.value = elRef
  }

  const getForm = async () => {
    await nextTick()
    const form = unref(formRef)
    if (!form) {
      console.error('The form is not registered. Please use the register method to register')
    }
    return form
  }

  // 一些内置的方法
  const methods: {
    setProps: (props: Recordable) => void
    setValues: (data: Recordable) => void
    getFormData: <T = Recordable | undefined>() => Promise<T>
    setSchema: (schemaProps: FormSetPropsType[]) => void
    addSchema: (formSchema: FormSchema, index?: number) => void
    delSchema: (field: string) => void
  } = {
    setProps: async (props: FormProps = {}) => {
      const form = await getForm()
      form?.setProps(props)
      if (props.model) {
        form?.setValues(props.model)
      }
    },

    setValues: async (data: Recordable) => {
      const form = await getForm()
      form?.setValues(data)
    },

    /**
     * @param schemaProps 需要设置的schemaProps
     */
    setSchema: async (schemaProps: FormSetPropsType[]) => {
      const form = await getForm()
      form?.setSchema(schemaProps)
    },

    /**
     * @param formSchema 需要新增数据
     * @param index 在哪里新增
     */
    addSchema: async (formSchema: FormSchema, index?: number) => {
      const form = await getForm()
      form?.addSchema(formSchema, index)
    },

    /**
     * @param field 删除哪个数据
     */
    delSchema: async (field: string) => {
      const form = await getForm()
      form?.delSchema(field)
    },

    /**
     * @returns form data
     */
    getFormData: async <T = Recordable>(): Promise<T> => {
      const form = await getForm()
      return form?.formModel as T
    }
  }

  props && methods.setProps(props)

  return {
    register,
    elFormRef,
    methods
  }
}
