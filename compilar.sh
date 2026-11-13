#!/bin/bash
# Compila el proyecto (usa el módulo jdk.compiler, equivale a javac).
cd "$(dirname "$0")"
mkdir -p out
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d out $(find src -name '*.java') "$@"
