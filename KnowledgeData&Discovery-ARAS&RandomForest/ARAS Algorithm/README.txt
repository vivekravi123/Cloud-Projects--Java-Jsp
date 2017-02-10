In the UI

Support - Desired threshold value of support.
Confidence - Desired threshold value of confidence in percentage.
Decision Value From - Value of attribute (eg: vhigh in car example)
Decision Value to - Value of attribute (eg: high in car example)
Enter Indices: A comma seperated string of indices of the attributes which should be made stable.
Eg: 1,2,3

Steps to run the code - 

-> run KDDProj.java
-> In the UI - Enter Attribute file, Data File, support, confidence and click Load Data.
-> This will load the data and populate the remaining fields.
-> Select the desicion attribute and enter the from and to values for it.
-> Enter the comma seperated indices for stable attributes and hit enter.

Output

-> The output file with name aras_output.txt will be generated at the default folder location.
 

The attribute file should have attribute names in a tab delimited format.
Number of columns of the data should match the number of attributes.
The Data file should have comma seperated data.

The final results will be displayed in the console as well as in a file.
The code opens the output file in notepad

Use sample data set in ppt - attribute.txt and data.txt (attached in the folder)