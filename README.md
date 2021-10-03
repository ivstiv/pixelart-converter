# Information

This project aims to recreate pixel arts as close as possible to a given image but with a limited color palette. By default, it is working with the color palette of the [Drednot's](https://drednot.io) Colored Panel, so technically it should not have any issues to work with any other palette given it is in the right json format.

Features:
- Resizing of images
- Scaling of the pixel art
- Option to show the IDs of the colors
- Preview & export images in different color spaces using different color palettes
- Available color spaces and algorithms: RGB, HSV, CIEDE2000, CIELAB76, CIELAB94 
- You can import your own palette as long as it follows the expected json format. (Check the default ones in src/main/resources/palettes)
- Export the color numbers in CSV format which can then be converted into A4 pdf pages using [this script](https://gist.github.com/Ivstiv/a4b8a82e48617d82db9042a9fd740f98)


# Screenshots and videos
[![Pixelart Converter Showcase](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/thumbnail.jpg)](https://vimeo.com/438229514 "Pixelart Converter Showcase")

**Experiment showing all algorithms:**

![Experiment 1](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/doc2.png)

**Useful case for making a pixel art:**

![Case 1](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/doc3.png)

**Another one:**

![Case 2](https://github.com/Ivstiv/drednot-pixelart-converter/blob/master/images/doc4.png)
# Usage and Download

You can download the compiled jar bundled with all dependencies from [here](https://github.com/Ivstiv/pixelart-converter/releases/tag/2.1).
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
```java -jar target/pixelart-converter-2.0.jar```

Or what I do is just run both of them in bash:
```mvn package && java -jar target/pixelart-converter-2.0.jar```

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


