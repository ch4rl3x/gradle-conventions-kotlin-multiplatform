#!/usr/bin/env bash
# Checks that the test projects produced complete, publishable artifacts.
#
# Without this a publication that silently produces nothing would pass as green --
# which is exactly what the publishing convention did for JVM modules before it
# learned to register their publication.
set -uo pipefail

repo="${HOME}/.m2/repository/de/charlex/testproject"
version="0.0.0-test"
status=0

# artifact directory -> extension of its main file
artifacts=(
  "jvm-library:jar"
  "test-android-library:aar"
  "kmp-library:jar"
  "kmp-library-android:aar"
  "kmp-library-iosarm64:klib"
  "kmp-library-iossimulatorarm64:klib"
)

for entry in "${artifacts[@]}"; do
  artifact="${entry%%:*}"
  extension="${entry##*:}"
  dir="${repo}/${artifact}/${version}"

  # Maven Central rejects a publication without sources, javadoc or a POM.
  for file in \
    "${artifact}-${version}.${extension}" \
    "${artifact}-${version}-sources.jar" \
    "${artifact}-${version}-javadoc.jar" \
    "${artifact}-${version}.pom"
  do
    if [ -f "${dir}/${file}" ]; then
      echo "ok    ${artifact}/${file}"
    else
      echo "MISS  ${artifact}/${file}"
      status=1
    fi
  done
done

exit "${status}"
