## Robo4J RaspberryPi Camera client 
Simple Robo4J server for handling and displaying Image Messages. Example allows to store image in PNG format.

## Requirements
* [Git][] 
* [Java JDK 8][]

## How to run demo
run compiled jar file 
```bash
$ java -jar robo4j-camera-center.jar
```
it opens main JavaFX application. 
* press button Active : center started to accept image messages and displaying received ones
* press button SAVE : current images is saved under the desired name, each press will override the already existing file
* text field name NAME : write a specific file name 

## Building from Source
The Robo4j framework uses [Gradle][] for building
> **Note:* If you are not using Robo4J as the standard user (pi) on a Raspberry Pi, you will have to specify the path to the local maven repository in the file _**libraries.gradle**_, variable: _mavenRepository_
> **Note:** Ensure that you have JDK 8 configured properly in your IDE.

## Staying in Touch
Follow [@robo4j][] or authors: [@miragemiko][] , [@hirt][]
on Twitter. In-depth articles can be found at [Robo4j.io][], [miragemiko blog][] or [marcus blog][]

## License
The Robo4j.io Framework is released under version 3.0 of the [General Public License][].

[Robo4j.io]: http://www.robo4j.io
[miragemiko blog]: http://www.miroslavkopecky.com
[marcus blog]: http://hirt.se/blog/
[General Public License]: http://www.gnu.org/licenses/gpl-3.0-standalone.html0
[@robo4j]: https://twitter.com/robo4j
[@miragemiko]: https://twitter.com/miragemiko
[@hirt]: https://twitter.com/hirt
[Gradle]: http://gradle.org
[Java JDK 8]: http://www.oracle.com/technetwork/java/javase/downloads
[Git]: http://help.github.com/set-up-git-redirect
[Robo4j documentation]: http://www.robo4j.io/p/documentation.html
