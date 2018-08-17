# Robo4J Tools and Plug-ins

This project contains tools and plug-ins to make it easier to design robots with Robo4J.

The tools are for now:

1. A magnetometer calibration tool named MagViz
2. A Java Mission Control plug-in for visualizing Robo4J Flight Recorder events
3. RoboCenter for compiling projects

# Magnetometer calibration tool
This is a JavaFX application which visualizes magnetometer data, and which will calculate the bias vector and matrix necessary to compensate for hard and soft iron effects. Simply run the com.robo4j.tools.magviz.MagViz class with the file containing calibration data as argument. See http://hirt.se/blog/?p=796 for more information.


# Java Mission Control integration
To get started with the Mission Control integration, do the following:

1. Start the latest version of Eclipse.
2. Go into preferences Plug-in Development / Target platform.
3. Add a new target platform (starting from empty), and Add an installation, simply pointing to the JDK_HOME/lib/missioncontrol folder.
4. Import the io.robo4j.jmc.visualization project into Eclipse

That's it! You should see "JMC with Robo4J" as a launcher in your run/debug menus. You can also export it as a binary plug-in from within Eclipse. 
