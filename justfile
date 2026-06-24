@list:
  just --list --unsorted

clean:
  ./kotlin clean

build:
  ./kotlin build

run:
  ./kotlin run

package:
  ./kotlin package --format executable-jar

format:
  ktfmt --kotlinlang-style src/ test/
