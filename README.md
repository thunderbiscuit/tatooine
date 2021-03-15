# Readme

Tatooine is the bitcoin testnet faucet for [Padawan Wallet](https://github.com/thunderbiscuit/padawan-wallet).

## Using the faucet
To get the wallet to send `1200` satoshis to a given address, use the `/sendcoins` POST route like so:
```shell
curl -X POST -d "<testnet address>" http://0.0.0.0:8082/sendcoins
```
