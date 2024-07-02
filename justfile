default:
  just --list

build:
  ./gradlew build

run:
  ./gradlew run

install:
  ./gradlew installDist
