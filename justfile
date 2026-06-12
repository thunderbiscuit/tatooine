default:
  just --list

build:
  ./kotlin build

run:
  ./kotlin run

package:
  ./kotlin package --format executable-jar
