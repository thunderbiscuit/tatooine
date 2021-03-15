/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.bitcoinj.wallet.UnreadableWalletException;

import java.io.File;

public class TatooineWallet {

    WalletAppKit kit;
    NetworkParameters params = TestNet3Params.get();
    String filePrefix = "tatooine-faucet-testnet";

    public void initializeWallet() throws UnreadableWalletException {

        BriefLogFormatter.init();

        String mnemonic = System.getenv("TATOOINE_MNEMONIC");
        System.out.println(mnemonic);
        String passphrase = "";
        Long creationTime = 1_615_000_000L;
               
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, passphrase, creationTime);
        
        // trying to implement a BIP84 compatible wallet but not working for now
        // problem is with the `structure` parameter being of the wrong type
        // this.kit = new WalletAppKit(params, Script.ScriptType.P2WPKH, structure, new File("."), filePrefix).restoreWalletFromSeed(seed);
        // this.kit = new WalletAppKit(params, Script.ScriptType.P2WPKH, new KeyChainGroupStructureTatooine(), new File("."), filePrefix).restoreWalletFromSeed(seed);

        // what is the derivation path? Doesn't match anything I can find
        this.kit = new WalletAppKit(params, Script.ScriptType.P2WPKH, null, new File("."), filePrefix).restoreWalletFromSeed(seed);
        // this.kit = new WalletAppKit(params, Script.ScriptType.P2WPKH, KeyChainGroupStructure.DEFAULT, new File("."), filePrefix).restoreWalletFromSeed(seed);
        // this.kit = new WalletAppKit(params, new File("."), filePrefix).restoreWalletFromSeed(seed);
        kit.startAsync();
        kit.awaitRunning();

        // it's not easy to find print statements in the sea of bitcoin logs
        System.out.println("\n\n\n---\n\n\nWallet is initialized\n\n\n---\n\n\n");
    }

    public void helloWallet() {
        System.out.println("This wallet is alive and well");
        String mnemonic = System.getenv("TATOOINE_MNEMONIC");
        System.out.println("mnemonic is :" + mnemonic);
    }

    public String generateNewAddress() {
        String newAddress = kit.wallet().freshReceiveAddress().toString();
        System.out.println("new address: " + newAddress);
        return newAddress;
    }

    public Long getBalance() {
        Coin balance = kit.wallet().getBalance();
        return balance.value;
    }
}
