#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
LOG_FILE=${SCRIPT_DIR}/build-local-docker-image.log
ERROR=""
IMAGE_NAME="vben-admin-local"

function stop_and_remove_container() {
    # Stop and remove the existing container
    docker stop ${IMAGE_NAME} >/dev/null 2>&1
    docker rm ${IMAGE_NAME} >/dev/null 2>&1
}

function remove_image() {
    # Remove the existing image
    docker rmi vben-admin-pro >/dev/null 2>&1
}

function install_dependencies() {
    # Install all dependencies
    cd ${SCRIPT_DIR}
    pnpm install || ERROR="install_dependencies failed"
}

function build_image() {
    # build docker
    docker build ../../ -f Dockerfile -t ${IMAGE_NAME} || ERROR="build_image failed"
}

function log_message() {
    if [[ ${ERROR} != "" ]];
    then
        >&2 echo "build failed, Please check build-local-docker-image.log for more details"
        >&2 echo "ERROR: ${ERROR}"
        exit 1
    else
        echo "docker image with tag '${IMAGE_NAME}' built sussessfully. Use below sample command to run the container"
        echo ""
        echo "docker run -d -p 8010:8080 --name ${IMAGE_NAME} ${IMAGE_NAME}"
    fi
}

echo "Info: Stopping and removing existing container and image" | tee ${LOG_FILE}
stop_and_remove_container
remove_image

echo "Info: Installing dependencies" | tee -a ${LOG_FILE}
install_dependencies 1>> ${LOG_FILE} 2>> ${LOG_FILE}

if [[ ${ERROR} == "" ]]; then
    echo "Info: Building docker image" | tee -a ${LOG_FILE}
    build_image 1>> ${LOG_FILE} 2>> ${LOG_FILE}
fi

log_message | tee -a ${LOG_FILE}
