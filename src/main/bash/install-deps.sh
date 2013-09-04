#!/bin/sh
readonly EXTERNAL_LIB_REPO="${EXTERNAL_LIB_REPO:-'lib'}"


if [ -z "${EXTERNAL_LIB_REPO}" ]; then
    echo "No EXTERNAL_LIB_REPO variable defined."
    exit 1
fi

mvn_install_file() {
    local artifactId="${1}"
    local version="${2}"
    local file="${3}"
    local groupId=${4}

    mvn  install:install-file -DgroupId="${groupId}" -DartifactId="${artifactId}" \
        -Dversion="${version}" -Dpackaging=jar -Dfile="${file}"
}

set -e
if [ -z "${NO_RUN}" ]; then
  vijava_version='5120121125'
  echo "Installing libs from: ${EXTERNAL_LIB_REPO}."
  mvn_install_file 'vijava' "${vijava_version}" "${EXTERNAL_LIB_REPO}/vijava${vijava_version}.jar"  'com.vmware'
  cobbler4j_version='0.1'
  mvn_install_file 'cobbler4j' "${cobbler4j_version}" "${EXTERNAL_LIB_REPO}/cobbler4j-${cobbler4j_version}.jar" 'org.fedorahosted.cobbler'
  xmlrpc_version='1.0'
  mvn_install_file 'redstone-xmlrpc-client' "${xmlrpc_version}" "${EXTERNAL_LIB_REPO}/xmlrpc-${xmlrpc_version}.jar" 'net.sf.xmlrpc'
  echo 'Done'
fi
