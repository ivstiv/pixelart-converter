# Information

This project aims to recreate pixel arts as close as possible to a given image but with a limited color palette. In this case it is working with the color palette of the [Drednot's](https://drednot.io) Colored Panel but technically it should not have any issues to work with any other palette given it is in the right json format.

Features:
- Scaling of the pixel art
- Option to show the IDs of the colors (specific to Drednot)
- Preview images in different color spaces
- Export images in different color spaces
- Available color spaces and algorithms: RGB, HSV, CIEDE2000, CIELAB76, CIELAB94 

**Note that this has been made over a weekend without any testing or best practices and can have a lot of bugs. Please report them to me on discord SKDown#4341!**
# Screenshots
UI of the program:
![UI](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/doc1.png)

Experiment showing all algorithms:
![Experiment 1](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/doc2.png)

Useful case for making a pixel art:
![Case 1](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/doc3.png)

Another one:
![Case 2](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/doc4.png)
# Usage

You can download the compiled jar bundled with all dependencies from here.
The UI is pretty self-explanatory,
 just don't feed the program large images. It is supposed to work with pixel arts with 1 to 1 ratio e.g sprites.  
 
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

# Planned features
If there is interest I can continue working on the project and add more features to it such as:
- Better color matching algorithms
- Exposing more offsets to the UI
- Adding support for wider variety of images
- Add documentation
- Command line support for exporting (will make the program easy to integrate with a web stack)
- Open to ideas

# Links
Related links to the project:
- Most of the stuff was implemented from here: https://en.wikipedia.org/wiki/Color_difference
- Using this library for CIEDE200 comparison: https://github.com/dajudge/color-diff
- Drednot: https://drednot.io/
- My discord tag: SKDown#4341


