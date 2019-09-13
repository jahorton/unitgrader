# UnitGrader

The UnitGrader is a Java-based program designed to facilitate unit-test-based grading for programming courses.  

At present the system is relatively small-scale and is based upon the use of JUnit, although this can be used to wrap testing code for other programming languages.  It also provides multiple Reflection-API-based services designed to facilitate the development of simple interface-oriented testing and evaluation and allows points to be awarded upon the success of tests on function-by-function basis for each test case and uses light multithreading to streamline the grading process.

## Project Setup

If using Eclipse, use File > Import, then choose the Gradle > Existing Gradle Project option.  Then, select the folder where you
cloned this repository to import the project for development and Eclipse's wizard should automatically set things up correctly from there.

You may need to go to Window > Preferences, then Java > Installed JREs to ensure that Eclipse is using a JDK installation instead of
a JRE when running the project, as a JDK is necessary for the UnitGrader to compile student submissions.

## Project State

I'm no longer actively maintaining this, as I'm no longer in a position where I'm in need of the tool.  I may use it as a practical sample project when teaching Git use, but won't be actively maintaining this otherwise.
