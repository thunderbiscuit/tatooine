<div align="center">
    <h1>Tatooine Faucet</h1>
    <br/>
    <img src="./artwork.svg" width="370px" />
    <br/>
    <br/>
    <br/>
</div>

Tatooine is a small bitcoin faucet application built with Ktor, a Kotlin asynchronous framework for creating microservices and web applications. The faucet was initially built for and is currently in production as part of the [Padawan Wallet](https://padawanwallet.com) project.

Tatooine can run anywhere with a JVM runtime, and is most easily deployed using containers. The `podman/` directory has the `Containerfile` necessary to build an image using Podman or Docker.  
<br/>

## Build the faucet

You can build the project by running the `distTar` task like so
```shell
./gradlew distTar
```
This will output a tarball in `/app/build/distributions/tatooine-0.8.0.tar` which you can extract anywhere you'd like using something like
```shell
tar --extract --verbose --file ./app/build/distributions/tatooine-0.8.0.tar -C /target/path/for/faucet/
```
The tarball opens up into a directory
```shell
❯ tree tattoine-0.8.0
.
├── bin
│   ├── tatooine
│   └── tatooine.bat
└── lib
    ├── bcprov-jdk15to18-1.68.jar
    ├── bitcoinj-core-0.15.10.jar
    ├── ...
```

## Run the faucet

On Linux/macOS, simply run the `tatooine` binary to start up the service:
```shell
./tatooine-0.8.0/bin/tatooine
```

## Use a custom config file

By default, the faucet will run using the configurations set in the `resources/application.conf` file at the time the project was compiled. Modifying that file and recompiling every time you want a configuration change is not ideal. 

A better way is to write different configuration files and simply provide their location when launching the service in different settings (development, production, etc.).

You achieve this by adding a `-config` argument to the call when launching the service. For example, if you add a file called `production.conf` to the `bin/` directory with the binaries, you'll then be able to launch the service using:
```shell
./tatooine-0.8.0/bin/tatooine -config=production.conf
```

## Podman/Docker

The easiest way to deploy a Tatooine faucet on the cloud is through a [Podman](https://podman.io/) or [Docker](https://www.docker.com/) container.

To do that, first build the application by running the `distTar` task and copy the resulting tarball to the `podman` directory. Add your `production.conf` file, and you're ready to build the image.
```shell
./gradlew :distTar
cp ./app/build/distributions/tatooine-0.8.0.tar ./podman/

# podman/ directory content
tree podman/
podman/
├── Containerfile
├── production.conf
└── tatooine-0.8.0/
```

Then from the `podman` directory, simply build the image, create the container, and start it.
```shell
cd path/to/podman/
podman build --tag tatooinefaucet:v0.8.0 .
podman create --name tatooinefaucet --publish 0.0.0.0:8080:8080 localhost/tatooinefaucet:v0.8.0
podman start tatooinefaucet
# podman stop tatooinefaucet
```

If you are deploying on the cloud, you'll need to copy the contents of the `podman/` directory on the host machine first using something like
```shell
scp -P 22 -r ./podman/ user@<ip>:/home/user/
```

## Usage

Most of the routes for the server require authentication, and the `/sendcoins` route requires a POST request. Here are examples for all 4 routes:
```shell
# /
curl http://127.0.0.1:8080/

# /getbalance
curl --user padawan:password http://127.0.0.1:8080/getbalance

# /sendcoins
curl -X POST --data "<bitcoin address>" --user padawan:password http://127.0.0.1:8080/sendcoins

# /shutdown
curl --user padawan:password http://127.0.0.1:8080/shutdown
```
