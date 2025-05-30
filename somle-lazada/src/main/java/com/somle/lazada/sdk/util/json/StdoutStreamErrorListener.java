//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.util.json;

public class StdoutStreamErrorListener extends BufferErrorListener {
    public void end() {
        System.out.print(this.buffer.toString());
    }
}
