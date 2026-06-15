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
java -jar tatooine-jvm-executable.jar
```

## Use a custom config file

By default, the faucet will run using the configurations set in the `resources/application.yaml` file at the time the project was compiled. Modifying that file and recompiling every time you want a configuration change is not ideal. 

A better way is to write different configuration files and simply provide their location when launching the service in different settings (development, production, etc.).

You achieve this by adding a `-config` argument to the call when launching the service. For example, if you create a file called `production.yaml`, you can then launch the service using:

```shell
java -jar tatooine-jvm-executable.jar -config=production.yaml
```

## Usage

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
