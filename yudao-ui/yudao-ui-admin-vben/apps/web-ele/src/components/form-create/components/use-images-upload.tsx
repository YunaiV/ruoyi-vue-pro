import { defineComponent } from 'vue';

import ImageUpload from '#/components/upload/image-upload.vue';

export function useImagesUpload() {
  return defineComponent({
    name: 'ImagesUpload',
    props: {
      multiple: {
        type: Boolean,
        default: true,
      },
      maxNumber: {
        type: Number,
        default: 5,
      },
    },
    setup() {
      // TODO: @puhui999：@dhb52 其实还是靠 props 默认参数起作用，没能从 formCreate 传递
      return (props: { maxNumber?: number; multiple?: boolean }) => (
        <ImageUpload maxNumber={props.maxNumber} multiple={props.multiple} />
      );
    },
  });
}
