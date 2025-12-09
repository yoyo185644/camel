# Camel: Efficient Compression of Floating-Point Time Series

## Project Structure

This project mainly includes the following various compression algorithms:

- The main code for the ***Camel*** algorithm is in the *yyy/ts/compress/camel* package.

Camel includes *compressor* and *decompressor* packages as well as *xorcompressor* and *xordecompressor*.

#### compressor package

This package includes 5 different XOR-based compression algorithms and provides a standard **ICompressor** interface. 

- CamelCompressor: This class is the complete Camel compression algorithm.


## TEST Camel

We recommend IntelliJ IDEA for developing this project. In our experiment, the default data block size is 1000. That is, 1000
pieces of data are read in each time for compression testing. If the size of the data set is less than 1000, we will not read it. The final experimental result is an average calculation of the compression of all data blocks.

- Camel compressor test: org/urbcomp/startdb/compress/elf/doubleprecision/TestCamel.java
- Camel Index building test: org/urbcomp/startdb/compress/elf/doubleprecision/TestCamelTree.java

### Prerequisites for testing

The following resources need to be downloaded and installed:

- Java 8 download: https://www.oracle.com/java/technologies/downloads/#java8
- IntelliJ IDEA download: https://www.jetbrains.com/idea/
- git download:https://git-scm.com/download
- maven download: https://archive.apache.org/dist/maven/maven-3/

Download and install jdk-8, IntelliJ IDEA and git. IntelliJ IDEA's maven project comes with maven, you can also use your
own maven environment, just change it in the settings.


### Set JDK

File -> Project Structure -> Project -> Project SDK -> *add SDK*

Click *JDK* to select the address where you want to download jdk-8



