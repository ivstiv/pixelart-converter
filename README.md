#Usage

You can download the compiled jar bundled with all dependencies from here.
The program can be used either in UI mode or on the command line. 

Exporting an image from the command line: ```java -jar drednot-pixelart-converter-1.0.jar -mode <RGB|HSV|whatever> -i <input file> -o <output file>```
Example: 
# Project Setup
In order to setup the project you will need:
- JDK 8+
- Maven
- IntelliJ

Compilation:
```mvn package```

Run the compiled jar:
```java -jar target/drednot-pixelart-converter-1.0.jar```

Or what I do is just run both of them in bash:
```mvn package && java -jar target/drednot-pixelart-converter-1.0.jar```


