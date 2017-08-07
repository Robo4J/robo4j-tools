# Robo4J Tools and Plug-ins

This project contains tools and plug-ins to make it easier to design robots with Robo4J.

###### list:
1. Magnetometer calibration visualiser
2. MissionControl: Flight Recorder plugin
3. RoboCenter for compiling projects

#Magnetometer Calibration visualiser
Project is Gradle/JavaFX application with start main class: MagViz


#Mission Control Integration
To get started with the Mission Control integration, do the following:

1. Start the latest version of Eclipse.
2. Go into preferences Plug-in Development / Target platform.
3. Add a new target platform (starting from empty), and Add an installation, simply pointing to the JDK_HOME/lib/missioncontrol folder.
4. Import the io.robo4j.jmc.visualization project into Eclipse

That's it! You should see "JMC with Robo4J" as a launcher in your run/debug menus. You can also export it as a binary plug-in from within Eclipse. 
