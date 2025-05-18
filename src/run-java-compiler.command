#!/bin/bash

# Navigate to the project directory (adjust if needed)
cd "$(dirname "$0")"

# Check for the JAR
if [ ! -f "lib/rsyntaxtextarea.jar" ]; then
  echo "❌ ERROR: lib/rsyntaxtextarea.jar not found!"
  echo "Please download it from https://github.com/bobbylight/RSyntaxTextArea/releases and place it in lib/"
  exit 1
fi

# Compile the Java file
javac -cp ".:lib/*" JavaGUICompilerApp.java
if [ $? -ne 0 ]; then
  echo "❌ Compilation failed. Please check errors above."
  exit 1
fi

# Run the compiled Java application
java -cp ".:lib/*" JavaGUICompilerApp

