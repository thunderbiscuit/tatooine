# Readme

Tatooine is the bitcoin testnet faucet for [Padawan Wallet](https://github.com/thunderbiscuit/padawan-wallet).

## Using/testing the faucet
Tatooine builds into a Fat JAR file which can be deployed and run anywhere with a JVM runtime. A `TATOOINE_MNEMONIC` environment variable needs to exists with your 12 or 24 word mnemonic.

You start the faucet using the jar file and providing the `NEW_WALLET` command line parameter (`true/false`) to tell Tatooine whether the wallet needs to be recovered from scratch or if there is already an existing and valid `.wallet` file in the directory (this is not necessary but saves time on startup).
```shell
java -jar tatooine-0.0.1-all.jar -P:NEW_WALLET=false
```

To get the wallet to send `1200` satoshis to a given address, use the `/sendcoins` POST route like so:
```shell
curl -X POST -d "<testnet address>" http://0.0.0.0:8082/sendcoins
```
