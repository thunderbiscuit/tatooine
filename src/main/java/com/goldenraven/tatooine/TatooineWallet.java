/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.tatooine;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.*;

import java.io.File;

public class TatooineWallet {

    WalletAppKit kit;
    NetworkParameters params = TestNet3Params.get();
    String filePrefix = "tatooine-faucet-testnet";
    String mnemonic;

    public void initializeWallet(Boolean alreadySynced, String mnemonicPhrase) throws UnreadableWalletException {

        this.mnemonic = mnemonicPhrase;
        // BriefLogFormatter.init();
        BriefLogFormatter.initWithSilentBitcoinJ();
        String walletdataDirectory = System.getenv("HOME") + "/.tatooinewalletdata/";

        if (alreadySynced == false) {
            System.out.println(this.mnemonic);
            String passphrase = "";
            Long creationTime = 1_615_000_000L;

            DeterministicSeed seed = new DeterministicSeed(this.mnemonic, null, passphrase, creationTime);

            // the path created by default is m/1h/0/*
            this.kit = new WalletAppKit(params, Script.ScriptType.P2WPKH, null, new File(walletdataDirectory), filePrefix).restoreWalletFromSeed(seed);
        } else {
            this.kit = new WalletAppKit(params, Script.ScriptType.P2WPKH, null, new File(walletdataDirectory), filePrefix);
        }

        kit.startAsync();
        kit.awaitRunning();

        // it's not easy to find print statements in the sea of bitcoin logs
        System.out.println("\n\n\n---\n\n\nWallet is initialized\n\n\n---\n\n\n");
    }

    public void helloWallet(Boolean alreadySynced) {
        if (alreadySynced == false) {
            System.out.println("This new wallet is alive and well");
        } else {
            System.out.println("This old wallet is alive and well");
        }
        System.out.println("mnemonic is :" + this.mnemonic);
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

    public String sendTo(String address) throws InsufficientMoneyException {
        Address recipientAddress = SegwitAddress.fromBech32(null, address);
        SendRequest sendRequest = SendRequest.to(recipientAddress, Coin.valueOf(21000));
        sendRequest.feePerKb = Coin.valueOf(200);
        Wallet.SendResult result = kit.wallet().sendCoins(sendRequest);

        return result.tx.getTxId().toString();
    }
}
