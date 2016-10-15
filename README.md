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
     
In this case total execution time would be **30 minutes**, However if we could parallelize the script execution make sure scripts are executed in right sequence and order, then we could save time, decreasing the total execution time just **10 minutes**.



           +----------+                       +----------+
           | Script 1 |                       | Script 4 |
      +----+----------+--+               +----+----------+-----+
      |                  |               |                     |
      |                  |               |                     |
    +-----v----+   +-----v----+     +----v-----+        +------v---+
    | Script 2 |   | Script 3 |     | Script 5 |        | Script 6 |
    +----------+   +----------+     +----------+        +----------+


For simplicity lets assume that tasks represented as follows
  
  ```
  
    <migration-tasks>
	<task module="baseApp" taskId="base-1" version="1.0.2" release="1">
		<task>select * from table1, 
		table2, table3 where xyx= 1;
		delete  from table1
	</task>
	</task>
	<task module="baseApp" taskId="base-2" version="1.0.2" release="1">
		<task>select * from table1, table3, table4 where xyx= 1;</task>
	</task>

	<task module="baseApp" taskId="base-3" version="1.0.2" release="1">
		<task>select * from table5, table6 where xyx= 1;</task>
	</task>
	<task module="baseApp" taskId="base-4" version="1.0.2" release="1">
		<task>select * from table10, table11 where xyx= 1;</task>
	</task>

	<task module="app1" taskId="app1-1" version="1.0.2" release="2">
		<task>select * from table4, table8 where xyx= 1;</task>
	</task>
	<task module="app1" taskId="app1-2" version="1.0.2" release="2">
		<task>select * from table8, table9 where xyx= 1;</task>
	</task>

	<task module="app1" taskId="app1-3" version="1.0.2" release="2">
		<task>select * from table10, table19 where xyx= 1;</task>
	</task>
	<task module="app1" taskId="app1-4" version="1.0.2" release="2">
		<task>select * from table11, table17 where xyx= 1;</task>
	</task>

	<task module="app2" taskId="app2-1" version="1.0.2" release="3">
		<task>select * from table6, table7 where xyx= 1</task>
	</task>
	<task module="app2" taskId="app2-2" version="1.0.2" release="3">
		<task>select * from table17, table18  where xyx= 1;</task>
	</task>

	<task module="app2" taskId="app2-3" version="1.0.2" release="3">
		<task>select * from table5, table20 where xyx= 1</task>
	</task>
	<task module="app2" taskId="app2-4" version="1.0.2" release="3">
		<task>select * from table7, table11 where xyx= 1</task>
	</task>

	<task module="app3" taskId="app3-1" version="1.0.2" release="3">
		<task>select * from table2, table16 where xyx= 1;</task>
	</task>
	<task module="app3" taskId="app3-2" version="1.0.2" release="3">
		<task>select * from table2, table11 where xyx= 1;</task>
	</task>

	<task module="app3" taskId="app3-3" version="1.0.2" release="3">
		<task>select * from table10, table11 where xyx= 1;</task>
	</task>
	<task module="app3" taskId="app3-4" version="1.0.2" release="3">
		<task>select * from table10, table11 where xyx= 1;</task>
	</task>
</migration-tasks>
  
  
  ```  

Based on the Table names in tasks, the built graph would be

[![dexecutor-graph.png](https://s6.postimg.org/5vytnb28h/dexecutor_graph.png)](https://postimg.org/image/g618mjs3x/)

As can be seen here _task base1_, _task base3_ and _task base 4_ runs in parallel and once, one of them finishes its children are executed for example if _task base1_ is finished its children _task base2_ and _task app3-1_ are scheduled and so on.

Notice that for _task app2-4_ to start _task app1-4_ and _task app2-1_ must finish, similarly for _task app3-2_ to start _task app3-1_ and _task app2-4_ must finish.

This mean we need an algorithm to build such kind of graph and that is what Class `MigrationTasksExecutor` does.

Further we need an Ultra light, Ultra fast library to extract table names out of SQLs, for this purpose we will use [sql-table-name-parser](https://github.com/mnadeem/sql-table-name-parser)

 ```
       <dependency>
		<groupId>com.github.mnadeem</groupId>
		<artifactId>sql-table-name-parser</artifactId>
		<version>0.0.2</version>
	  </dependency>
	  
 ```
 
 
