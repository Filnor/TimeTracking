# TimeTracking
When I started programming, I came to the point where I got to Java & object-oriented programming and for whatever reason I coded a time-tracking software, which of course would have been better than any existing solutions.

Some years have past and I rediscovered my code and laughed how shitty these 560 lines of code are. Here some facts:

- All code is in one file. No classes or interfaces. Nothing at all.
- GUI Library is Swing, but learning JavaFX would have been much easier (Swing is in standart Java since 1998, JFX since 2014)
- It got database handling, but the database had only one table with a bunch of columns, but I actually just wrote back strings to the database.
- I didn't update a table, I truncated it and just inserted all the rows again. (without reseeding IDs in the process, so you would end up with id `99999...` if you even scroll that far without getting mad).
- I didn't follow any code conventions, so variable naming is a bit messy, like the whole code
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
- I wasted 45 hours of my life for this... project (Not sure why I had nothing better to do with my time)

## Installation & Usage
Load the dump to your MySQL server, edit the connection properties and run it from your IDE or command line, if you want.

## Contribution
Your improvements are welcome, but please refer to the upcoming branch where I also do some improvements and rewritings.

### Credits
Thanks to @raxod502, who inspired me to upload my own bad application with his repository [TerraFrame](https://github.com/raxod502/TerraFrame).
