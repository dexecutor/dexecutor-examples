# Dexecutor Sample Application

A sample application demonstrating how Dexecutor can be used to simplify and improve the performance of Data Migration process in an application.


Lets take an use case where in an application update form version x to x+1 requires tons of data base operations. Normally such operations are performed using migration scripts, executing a series of instructions, to bring the application to desired state.

Normally each script is executed sequentially, to improve the performance we can parallelize the execution of such scripts. However that would put us in trouble if parallel execution is not done properly. For example lets say, script1 creates a table X, and script2 uses that table, in this case these two tasks has to be executed sequentially.

Dexecutore can be used in these cases easily by adding an Algorithmic logic which builds the graph based on table names. 

   Script 1 ==> operates on Tables t1 and t2 and takes 5 min
   Script 2 ==> operates on Tables t1 and t3 and takes 5 min
   Script 3 ==> operates on Tables t2 and t4 and takes 5 min
   Script 4 ==> operates on Tables t5 and t6 and takes 5 min
   Script 5 ==> operates on Tables t5 and t7 and takes 5 min
   Script 6 ==> operates on Tables t6 and t8 and takes 5 min

Normally These scripts are executed sequentially

    Script 1
      |
      V
    Script 2
      |
      V
    Script 3
      |
      V
    Script 4
      |
      V
    Script 5
      |
      V
    Script 6
     
In this case total execution time would be 30 minutes, However if we could parallelize the script execution make sure scripts are executed in right sequence and order, then we could save time, decreasing the total execution time just 10 minutes.



           +----------+                       +----------+
           | Script 1 |                       | Script 4 |
      +----+----------+--+               +----+----------+-----+
      |                  |               |                     |
      |                  |               |                     |
    +-----v----+   +-----v----+     +----v-----+        +------v---+
    | Script 2 |   | Script 3 |     | Script 5 |        | Script 6 |
    +----------+   +----------+     +----------+        +----------+

 
 
