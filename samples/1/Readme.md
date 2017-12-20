# Sample 1

This sample serves to demonstrate an example setup for utilizing the UnitGrader for student assignments.

# /bin

The /bin folder contains the JUnit test cases needed to assess student assignments.  If this folder does not
include the *.test specification file, it should lie directly within the folder containing said file.

If this folder includes the *.test specification file, the folder may safely be renamed as desired.

# *.test  (here, AssignmentGrader.test)

This file contains a pre-made UnitGrader test specification usable to grade assignments.  It should be created
either directly outside of a /bin folder containing the relevant Junit tests or within the folder, and should
not be moved relative to said folder after creation.

# submissions.zip

This zip is a sample batch download from an online learning management software solution, containing a set of 
student submission files.  This should generally contain one *.zip per student as extracted directly from the 
batch download folder.

For testing to occur, it should be decompressed to a new folder; if extracting within the repo, we suggest 
the .gitignore'd /submissions folder.  In general, the decompression results need not be named /submissions 
and may be stored anywhere on the system.