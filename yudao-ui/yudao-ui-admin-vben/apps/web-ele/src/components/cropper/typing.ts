import type Cropper from 'cropperjs';
import type { ButtonProps } from 'element-plus';

import type { CSSProperties } from 'vue';

export interface apiFunParams {
  file: Blob;
  filename: string;
  name: string;
}

export interface CropendResult {
  imgBase64: string;
  imgInfo: Cropper.Data;
}

export interface CropperProps {
  src?: string;
  alt?: string;
  circled?: boolean;
  realTimePreview?: boolean;
  height?: number | string;
  crossorigin?: '' | 'anonymous' | 'use-credentials' | undefined;
  imageStyle?: CSSProperties;
  options?: Cropper.Options;
}

export interface CropperAvatarProps {
  width?: number | string;
  value?: string;
  showBtn?: boolean;
  btnProps?: ButtonProps;
  btnText?: string;
  uploadApi?: (params: apiFunParams) => Promise<any>;
  size?: number;
}

export interface CropperModalProps {
  circled?: boolean;
  uploadApi?: (params: apiFunParams) => Promise<any>;
  src?: string;
  size?: number;
}

export const defaultOptions: Cropper.Options = {
  aspectRatio: 1,
  zoomable: true,
  zoomOnTouch: true,
  zoomOnWheel: true,
  cropBoxMovable: true,
  cropBoxResizable: true,
  toggleDragModeOnDblclick: true,
  autoCrop: true,
  background: true,
  highlight: true,
  center: true,
  responsive: true,
  restore: true,
  checkCrossOrigin: true,
  checkOrientation: true,
  scalable: true,
  modal: true,
  guides: true,
  movable: true,
  rotatable: true,
};

export type { Cropper as CropperType };
