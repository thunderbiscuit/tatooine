[doc("List all available commands.")]
@list:
  just --list --unsorted

[doc("Remove build output and caches.")]
clean:
  ./kotlin clean

[doc("Compile the project.")]
build:
  ./kotlin build

[doc("Run all tests.")]
test:
  ./kotlin test

[doc("Run the app.")]
run:
  ./kotlin run

[doc("Package the app and place it at the root.")]
package:
  ./kotlin package --format executable-jar
  @cp build/tasks/_tatooine_executableJarJvm/tatooine-jvm-executable.jar ./

[doc("Format source and test files using ktfmt")]
format:
  ktfmt --kotlinlang-style src/ test/
