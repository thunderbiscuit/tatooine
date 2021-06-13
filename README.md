# Readme

Tatooine is a small bitcoin testnet faucet built with Ktor, a Kotlin asynchronous framework for creating microservices and web applications. The faucet was initially built for and is currently in production as part of the [Padawan Wallet](https://github.com/thunderbiscuit/padawan-wallet) project.

Tatooine can run anywhere with a JVM runtime, and is most easily deployed using containers. The `podman/` directory has the `Containerfile` necessary to build an image using Podman or Docker.  
<br/>

## Building the faucet
You can build the project by running the `distTar` task like so
```shell
./gradlew distTar
```
This will output a tarball in `/build/distributions/tatooine-0.2.0.tar` which you can extract anywhere you'd like using something like
```shell
tar --extract --verbose --file ./build/distributions/tatooine-0.2.0.tar -C /target/path/for/faucet/
```
The tarball opens up into a directory
```shell
❯ tree tattoine-0.2.0
.
├── bin
│   ├── tatooine
│   └── tatooine.bat
└── lib
    ├── bcprov-jdk15to18-1.68.jar
    ├── bitcoinj-core-0.15.10.jar
    ├── ...
```
<br/>

## Running the faucet
On Linux/MacOS, simply run the `tatooine` binary to start up the service:
```shell
./tatooine-0.2.0/bin/tatooine
```
This will sync your wallet from scratch (note that the config file in `resources/` contains a `wallet.alreadySynced` field set to `false` by default). To tell Tatooine that a valid `.wallet` file exists and that the wallet does not need to be recovered from scratch (this is not necessary but saves time on startup), you can set this config field to `true` like so:
```shell
./tatooine-0.2.0/bin/tatooine -P:wallet.alreadySynced=true
```
<br/>

## Using a custom config file
By default, the faucet will run using the configurations set in the `resources/application.conf` file at the time the project was compiled. Modifying that file and recompiling every time you want a configuration change is not ideal. 

A better way is to write different configuration files and simply provide their location when launching the service in different settings (development, production, etc.).

You achieve this by adding a `-config` argument to the call when launching the service. For example, if you add a file called `production.conf` to the `bin/` directory with the binaries, you'll then be able to launch the service using:
```shell
./tatooine-0.2.0/bin/tatooine -config=production.conf
```
<br/>

## Launch and keep the process running in the background
If the faucet is started as in the examples above, quitting the terminal that is running the application or dropping the ssh connection (if the faucet is running in the cloud) will cause the process to stop and the faucet to wind down. 

This is usually not desired. To launch and keep the faucet live even after logging off, note that the `&` argument allows a process to run in the background, and using nohup allows you to log off and keep the process running. Because the resulting `nohup.out` file can get quite big, you can start the process and ensure that file is not created by adding `>/dev/null 2>&1` to your command.
```shell
nohup ./tatooine-0.2.0/bin/tatooine -config=production.conf >/dev/null 2>&1 &
```
<br/>

## Podman/Docker
The easiest way to deploy a Tatooine faucet on the cloud is through a [Podman](https://podman.io/) or [Docker](https://www.docker.com/) container.

To do that, first build the application by running the `distTar` task and copy the resulting tarball to the `podman` directory. Add your `production.conf` file in there as well, and you're ready to build the image.
```shell
./gradlew :distTar
cp ./build/distributions/tatooine-0.2.0.tar ./podman/

# podman/ directory content
tree podman/
podman/
├── Containerfile
├── production.conf
└── tatooine-0.2.0/
```

Then from the `podman` directory, simply build the image, create the container, and start it. If you are deploying on the cloud, you'll need to copy the contents of the `podman/` directory on the host machine first using something like `scp -P 22 -r ./podman/ remoteuser@<ip>:/home/user/`.
```shell
cd path/to/podman/
podman build --tag tatooinefaucet:v0.2.0 .
podman create --name tatooinefaucet --publish 0.0.0.0:8080:8080 localhost/tatooinefaucet:v0.2.0
podman start tatooinefaucet
# podman stop tatooinefaucet
```
<br/>

## Using the faucet
To get Tatooine to send `21000` satoshis to a given address, use the `/sendcoins` POST route like so:
```shell
curl -X POST -d "<testnet address>" http://127.0.0.1:8080/sendcoins
```
