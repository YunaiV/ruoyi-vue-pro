import { GlobPartial } from '../types';
interface FileDetails {
    file: string;
    hash: string;
    size: number;
}
export declare function getFileDetails({ globDirectory, globFollow, globIgnores, globPattern, }: Omit<GlobPartial, 'globDirectory' | 'globPatterns' | 'templatedURLs'> & {
    globDirectory: string;
    globPattern: string;
}): {
    globbedFileDetails: Array<FileDetails>;
    warning: string;
};
export {};
