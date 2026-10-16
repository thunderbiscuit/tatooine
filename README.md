<div align="center">
    <h1>Tatooine Faucet</h1>
    <br/>
    <img src="./artwork.svg" width="370px"  alt="Tatooine logo"/>
    <br/>
    <br/>
    <br/>
</div>

Tatooine is a small bitcoin faucet application built with Ktor, a Kotlin asynchronous framework for creating microservices and web applications. The faucet was initially built for and is currently in production as part of the [Padawan Wallet](https://padawanwallet.com) project.

Tatooine can run anywhere with a JVM runtime and is most easily deployed using containers. The `podman/` directory has the `Containerfile` necessary to build an image using Podman or Docker.  

<br/>

## Build the faucet

You can build the project by running the `package` task like so

```shell
just package
# The above runs: ./kotlin package --format executable-jar
```

This will output a jar in `./build/tasks/_tatooine_executableJarJvm/tatooine-jvm-executable.jar` which you can start anywhere you like using

```shell
java -jar tatooine-jvm-executable.jar -config=production.yaml
```

## Config file

The faucet requires a config file, passed with the `-config` argument; no config is bundled in the jar, and the faucet will refuse to start without one. Keep a separate config file for each setting you run in (development, production, etc.).

The `dev.yaml` file at the root of the project is a config for a local regtest setup with an Electrum client, and is the one used by `just run`. Use it as a starting point for your own:

```shell
cp dev.yaml production.yaml
# edit production.yaml, then
java -jar tatooine-jvm-executable.jar -config=production.yaml
```

## Data directory

The faucet keeps all of its state in a single data directory, independent of where it is launched from. This directory is `~/.tatooine/`.

```
~/.tatooine/
├── bdk_persistence.sqlite3   # wallet database
├── cbfdata/                  # compact block filter data (CBF sync only)
└── logs/
    └── tatooinefaucet.log
```

## Usage

To run the faucet in the background on a server and capture console output and crashes alongside the application log, use:

```shell
mkdir ~/.tatooine/logs
nohup java -jar tatooine-jvm-executable.jar -config=production.yaml 1>~/.tatooine/logs/app.log 2>~/.tatooine/logs/error.log &
```

Most of the routes for the server require authentication, and the `/sendcoins` route requires a POST request. Here are examples for all 5 routes:

```shell
# /
curl http://127.0.0.1:8080/

# /getbalance
curl --header "Authorization: Bearer token" http://127.0.0.1:8080/getbalance

# /report
curl --header "Authorization: Bearer token" http://127.0.0.1:8080/report

# /sendcoins
curl -X POST --data "<bitcoin address>" --header "Authorization: Bearer token" http://127.0.0.1:8080/sendcoins

# /shutdown
curl --header "Authorization: Bearer token" http://127.0.0.1:8080/shutdown
```
