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

[doc("Format all Kotlin source and test files with ktfmt.")]
format: check-ktfmt-version
  ktfmt --kotlinlang-style --enable-editorconfig src/ test/

[group("Format")]
[doc("Check that all Kotlin source files are formatted, without changing them.")]
format-check: check-ktfmt-version
  #!/usr/bin/env bash
  set -euo pipefail
  if ! ktfmt --kotlinlang-style --enable-editorconfig --dry-run --set-exit-if-changed src/ test/; then
    echo "Error: the files above need formatting. Run 'just format' to fix them." >&2
    exit 1
  fi

# The version of ktfmt must be 0.65
[private]
check-ktfmt-version:
  #!/usr/bin/env bash
  set -euo pipefail
  version=$(ktfmt --version | grep -oE '[0-9]+\.[0-9]+')
  if [ "$version" != "0.65" ]; then
    echo "Error: ktfmt 0.65 is required, but found $version" >&2
    exit 1
  fi
