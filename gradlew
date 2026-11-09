#!/bin/bash
# Wrapper Gradle per il progetto Deutsch
# Uso: ./gradlew assembleDebug

export JAVA_HOME=/opt/data/toolchain/jdk-17.0.20.1+1
export ANDROID_HOME=/opt/data/toolchain/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$JAVA_HOME/bin:/opt/data/toolchain/gradle-8.11.1/bin:$PATH
export GRADLE_USER_HOME=/opt/data/toolchain/gradle-home

cd "$(dirname "$0")"
gradle "$@"
