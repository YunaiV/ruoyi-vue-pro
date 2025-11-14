export enum MODE {
  HTML = 'htmlmixed',
  JS = 'javascript',
  JSON = 'application/json',
  VUE = 'vue',
}

export interface CodeEditorProps {
  mode?: string;
  value?: string;
  readonly?: boolean;
  bordered?: boolean;
  autoFormat?: boolean;
}
