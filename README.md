# Readme

Tatooine is the bitcoin testnet faucet for [Padawan Wallet](https://github.com/thunderbiscuit/padawan-wallet).  
<br/>

## Deploying the faucet
Tatooine is a Ktor server that builds into a [Fat JAR](https://ktor.io/docs/fatjar.html) file which can be deployed and run anywhere with a JVM runtime. A `TATOOINE_MNEMONIC` environment variable is required with your 12 or 24 word mnemonic backup.

You start the faucet using the jar file like so:
```shell
java -jar tatooine-0.0.1-all.jar
```

This will sync your wallet from scratch (note that the config file contains a `wallet.alreadySynced` field set to `false` by default). To tell Tatooine that a valid `.wallet` file exists in the directory and that the wallet does not need to be recovered from scratch (this is not necessary but saves time on startup), you can sett this config field to `true` like so:
```shell
java -jar tatooine-0.0.1-all.jar -P:wallet.alreadySynced=true
```

## Using the faucet
To get Tatooine to send `1200` satoshis to a given address, use the `/sendcoins` POST route like so:
```shell
curl -X POST -d "<testnet address>" http://127.0.0.1:8080/sendcoins
```
