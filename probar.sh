#!/bin/bash
# Compila y ejecuta las pruebas de la lógica de negocio.
cd "$(dirname "$0")"
./compilar.sh || exit 1
mkdir -p out-test datos
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -cp out -d out-test test/PruebasLogica.java || exit 1
java -Dstdout.encoding=UTF-8 -cp out:out-test PruebasLogica
