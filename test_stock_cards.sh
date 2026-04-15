#!/bin/bash
cat << 'END' > DummyTest.kt
package com.rivavafi.universal
fun main() {
    println("Test passed")
}
END
kotlinc DummyTest.kt -include-runtime -d DummyTest.jar
java -jar DummyTest.jar
