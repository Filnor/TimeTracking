# TimeTracking
When I started programming, I can to the point where I got to Java & object-oriented programming and for whatever reason I coded a time-tracking software, of course better than any existing solutions.

Some years have past and I rediscovered my code and laughed how shitty these 560 lines of code are. Here some facts:

- All code is in one file. Because Classes are to complicated.
- GUI Library is Swing, not JavaFX, a 15-year old framework sure be better than a very new one.
- It got database handling, but the database had only one table with a bunch of `VARCHAR(255)` columns.
- I broke the code conventions of Java and applied my own, inconsistent ones (see code below)
- I didn't know that something like String formatting even existed back then, so building an `INSERT` statement looked like this:

```java
String sUser = String.valueOf(TimeRecords.getValueAt(iRow, 1)) + "', '";
String sCategory = String.valueOf(TimeRecords.getValueAt(iRow, 2)) + "', '";
String sTask = String.valueOf(TimeRecords.getValueAt(iRow, 3)) + "', '";
String sStartTime = dStartTime + "', '";
String sEndTime = dEndTime + "', '";
String sDuration = String.valueOf(TimeRecords.getValueAt(iRow, 6)) + "'";
String sOutputLine = sDateDB + sUser + sCategory + sTask + sStartTime + sEndTime + sDuration;

String sSaveData = "INSERT INTO timetracking(`date`, `user`, `activity_group`, `activity`, `start_time`, `end_time`, `duration`) VALUES (" + sOutputLine + ")";
stmt.executeUpdate(sSaveData);
```

And last but not least:
- I wasted 45 hours of my life for this :unamused:

## But hey, at least it runs! Or not?
Well, if you import the SQL dump on a MySQL database server, change the connection settings and add the MySQL JDBC Connector to your Project.

### Credits
Thanks to @raxod502, who inspired me to upload my own bad application with his Repository [TerraFrame](https://github.com/raxod502/TerraFrame).
