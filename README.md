# Java CFG Analyzer

This project is a simple Java Control Flow Graph (CFG) Analyzer using JavaParser.

## Overview

The Java CFG Analyzer parses a Java source file, extracts control flow information, and generates a CFG in JSON format. The project demonstrates how to use JavaParser to work with Abstract Syntax Trees (AST) and analyze the structure of if-else blocks.

## Usage

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Chayan199916/java-cfg-analyzer.git

   ```

2. **Navigate to the project directory:**

   ```bash
   cd java-cfg-analyzer

   ```

3. **Place your Java source files in the src/main/resources directory.**

   For example, if your file is named "Sample2.java":

   - src
     - main
       - resources
         - Sample2.java

4. **Build the project using Maven:**

   ```bash
   mvn clean package

   ```

5. **Run the Java CFG Analyzer:**

   ```bash
   mvn exec:java

   ```

6. **This will generate the control flow graph (CFG) in JSON format in the target directory and display the number of distinct paths.**

## Dependencies

JavaParser: A Java library for parsing and analyzing Java source code.

Gson: A Java library for JSON serialization and deserialization.

# Contributors

Contributions are welcome! Feel free to open issues, suggest improvements, or contribute additional features to enhance the analyzer example.
