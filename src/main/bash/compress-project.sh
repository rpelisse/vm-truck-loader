#!/bin/bash

readonly ROOT="${ROOT:-.}"
readonly FILENAME=${FILENAME:-$(basename $(pwd))}
readonly TARGET_DIR=$(mktemp -d)

readonly TARGET="${TARGET_DIR}/${FILENAME}.zip"
zip -r "${TARGET}" "${ROOT}" -x bin/\* -x ./target/\* -x .project -x .settings/\* -x .classpath > /dev/null

if [ "${?}" -eq 0 ]; then
  echo "Project compressed:"
  du -hs "${TARGET}"
else
  echo "Compress failed."
fi
