interface VerificationProps {
  arith?: number;
  barSize?: {
    height: string;
    width: string;
  };
  blockSize?: {
    height: string;
    width: string;
  };
  captchaType?: 'blockPuzzle' | 'clickWord';
  explain?: string;
  figure?: number;
  imgSize?: {
    height: string;
    width: string;
  };
  mode?: 'fixed' | 'pop';
  space?: number;
  type?: '1' | '2';
  checkCaptchaApi?: (data: any) => Promise<any>;
  getCaptchaApi?: (data: any) => Promise<any>;
}

export type { VerificationProps };
