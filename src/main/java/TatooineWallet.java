/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

public class TatooineWallet {
    public void helloWallet() {
        System.out.println("This wallet is alive and well");
        String mnemonic = System.getenv("TATOOINE_MNEMONIC");
        System.out.println("mnemonic is :" + mnemonic);
    }
}
