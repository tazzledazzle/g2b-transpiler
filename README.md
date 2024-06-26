# g2b-transpiler

g2b-transpiler is a tool designed to transpile code from one programming language to another using ANTLR for parsing and Kotlin for implementation. This project aims to simplify the process of converting source code from a given language (e.g., Go) to another language (e.g., Bazel).

## Features

- **Language Parsing**: Utilizes ANTLR for parsing the source language.
- **Transpilation**: Converts parsed code into the target language.
- **Modular Design**: Easy to extend for additional languages.

## Requirements

- Java 8 or higher
- Gradle 6.0 or higher

## Installation

Clone the repository and build the project using Gradle:

```bash
git clone https://github.com/tazzledazzle/g2b-transpiler.git
cd g2b-transpiler
./gradlew build
```

## Usage

To transpile a file, run the following command:

```bash
./gradlew run --args="path/to/input/file"
```

## Project Structure

- **src/main/kotlin**: Contains the main source code for the transpiler.
- **src/main/antlr**: ANTLR grammar files for parsing the source language.
- **build.gradle**: Build configuration file for Gradle.
- **settings.gradle**: Gradle settings file.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the MIT License.
