# Dexecutor Sample Application

A sample application demonstrating how [Dexecutor](https://github.com/dexecutor/dexecutor-core) can be used to simplify and improve the performance of Data Migration process in an application.


Lets take an use case where in an application update form version **x** to **x+1** requires tons of data base operations. Normally such operations are performed using migration scripts, executing a series of instructions, to bring the application to desired state.

Normally each script is executed sequentially, to improve the performance we can parallelize the execution of such scripts. However that would put us in trouble if parallel execution is not done properly. For example lets say, script1 creates a table X, and script2 uses that table, in this case these two tasks has to be executed sequentially.

[Dexecutor](https://github.com/dexecutor/dexecutor-core) can be used in these cases easily by adding an Algorithmic logic which builds the graph based on table names. Lets assume the following

    Script 1 ==> operates on Tables t1 and t2 and takes 5 minute
    Script 2 ==> operates on Tables t1 and t3 and takes 5 minute
    Script 3 ==> operates on Tables t2 and t4 and takes 5 minute
    Script 4 ==> operates on Tables t5 and t6 and takes 5 minute
    Script 5 ==> operates on Tables t5 and t7 and takes 5 minute
    Script 6 ==> operates on Tables t6 and t8 and takes 5 minute

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
     
In this case total execution time would be **30 minutes**, However if we could parallelize the script execution, make sure scripts are executed in right sequence and order, then we could save time, decreasing the total execution time to just **10 minutes**.



           +----------+                       +----------+
           | Script 1 |                       | Script 4 |
      +----+----------+--+               +----+----------+-----+
      |                  |               |                     |
      |                  |               |                     |
    +-----v----+   +-----v----+     +----v-----+        +------v---+
    | Script 2 |   | Script 3 |     | Script 5 |        | Script 6 |
    +----------+   +----------+     +----------+        +----------+


For simplicity lets assume that tasks are represented as follows
  
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

As can be seen here _task base1_, _task base3_ and _task base 4_ runs in parallel and once, one of them finishes its children are executed, for example if _task base1_ is finished then its children _task base2_ and _task app3-1_ are executed and so on.

Notice that for _task app2-4_ to start, _task app1-4_ and _task app2-1_ must finish, similarly for _task app3-2_ to start, _task app3-1_ and _task app2-4_ must finish.

This means we need an algorithm to build such kind of graph and that is what Class [MigrationTasksExecutor](https://github.com/dexecutor/dexecutor-examples/blob/master/src/main/java/com/github/dexecutor/executor/MigrationTasksExecutor.java) does.

Further we need an Ultra light, Ultra fast library to extract table names out of SQLs, for this purpose we will use [sql-table-name-parser](https://github.com/mnadeem/sql-table-name-parser)

 ```
   <dependency>
	<groupId>com.github.mnadeem</groupId>
	<artifactId>sql-table-name-parser</artifactId>
	<version>0.0.2</version>
  </dependency>
	  
 ```
 
 If you print the graph using [Dexecutor](https://dexecutor.github.io/current/apidocs/com/github/dexecutor/core/Dexecutor.html#print(java.io.Writer)) you would notice the following level order traversal.
 
 
 ```
base-4[] base-1[] base-3[] 
app1-3[base-4] app1-4[base-4] base-2[base-1] app3-1[base-1] app2-1[base-3] app2-3[base-3] 
app2-2[app1-4] app1-1[base-2] app2-4[app1-4, app2-1] 
app1-2[app1-1] app3-2[app2-4, app3-1] 
app3-3[app1-3, app3-2] 
app3-4[app3-3]  
 
 ```
 base-4, base-1 and base-3 are run in parallel and then don't have any dependency.
 
 at the next level app2-2, app1-4, base-2, app3-1, app2-1 and app2-3 are run, and their dependencies are mentioned in square brackets [  ],
 
 Here is the console output executing [MigrationApp](https://github.com/dexecutor/dexecutor-examples/blob/master/src/main/java/com/github/dexecutor/main/MigrationApp.java)
 
 ```
21:44:48.911 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule base-4 node
21:44:48.911 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task base-4 
21:44:48.912 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule base-1 node
21:44:48.913 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task base-1 
21:44:48.913 [pool-1-thread-1] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # base-4
21:44:48.913 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule base-3 node
21:44:48.913 [pool-1-thread-1] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task base-4
21:44:48.913 [pool-1-thread-2] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # base-1
21:44:48.914 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task base-3 
21:44:48.914 [pool-1-thread-1] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # base-4, Execution Done!
21:44:48.914 [pool-1-thread-2] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task base-1
21:44:48.915 [pool-1-thread-2] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # base-1, Execution Done!
21:44:48.915 [pool-1-thread-3] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # base-3
21:44:48.915 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node base-4 done, with status SUCCESS
21:44:48.916 [pool-1-thread-3] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task base-3
21:44:48.916 [pool-1-thread-3] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # base-3, Execution Done!
21:44:48.916 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app1-3 node
21:44:48.917 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app1-3 
21:44:48.917 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app1-4 node
21:44:48.917 [pool-1-thread-4] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app1-3
21:44:48.917 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app1-4 
21:44:48.917 [pool-1-thread-4] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app1-3
21:44:48.917 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node base-1 done, with status SUCCESS
21:44:48.917 [pool-1-thread-4] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app1-3, Execution Done!
21:44:48.918 [pool-1-thread-5] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app1-4
21:44:48.918 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule base-2 node
21:44:48.918 [pool-1-thread-5] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app1-4
21:44:48.918 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task base-2 
21:44:48.918 [pool-1-thread-5] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app1-4, Execution Done!
21:44:48.919 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app3-1 node
21:44:48.919 [pool-1-thread-6] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # base-2
21:44:48.919 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app3-1 
21:44:48.919 [pool-1-thread-6] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task base-2
21:44:48.919 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node base-3 done, with status SUCCESS
21:44:48.919 [pool-1-thread-6] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # base-2, Execution Done!
21:44:48.919 [pool-1-thread-7] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app3-1
21:44:48.919 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app2-1 node
21:44:48.920 [pool-1-thread-7] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app3-1
21:44:48.920 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app2-1 
21:44:48.920 [pool-1-thread-7] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app3-1, Execution Done!
21:44:48.920 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app2-3 node
21:44:48.920 [pool-1-thread-8] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app2-1
21:44:48.920 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app2-3 
21:44:48.920 [pool-1-thread-8] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app2-1
21:44:48.921 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app1-3 done, with status SUCCESS
21:44:48.921 [pool-1-thread-8] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app2-1, Execution Done!
21:44:48.921 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 188 - node app3-3 depends on [app1-3, app3-2]
21:44:48.921 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app1-4 done, with status SUCCESS
21:44:48.922 [pool-1-thread-9] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app2-3
21:44:48.922 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app2-2 node
21:44:48.922 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app2-2 
21:44:48.922 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 188 - node app2-4 depends on [app1-4, app2-1]
21:44:48.922 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node base-2 done, with status SUCCESS
21:44:48.922 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app1-1 node
21:44:48.922 [pool-1-thread-9] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app2-3
21:44:48.923 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app1-1 
21:44:48.923 [pool-1-thread-10] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app2-2
21:44:48.923 [pool-1-thread-10] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app2-2
21:44:48.923 [pool-1-thread-10] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app2-2, Execution Done!
21:44:48.924 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app3-1 done, with status SUCCESS
21:44:48.924 [pool-1-thread-9] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app2-3, Execution Done!
21:44:48.924 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 188 - node app3-2 depends on [app2-4, app3-1]
21:44:48.924 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app2-1 done, with status SUCCESS
21:44:48.924 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app2-4 node
21:44:48.924 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app2-4 
21:44:48.925 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app2-2 done, with status SUCCESS
21:44:48.925 [pool-1-thread-12] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app2-4
21:44:48.925 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app2-3 done, with status SUCCESS
21:44:48.925 [pool-1-thread-12] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app2-4
21:44:48.925 [pool-1-thread-12] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app2-4, Execution Done!
21:44:48.925 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app2-4 done, with status SUCCESS
21:44:48.925 [pool-1-thread-11] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app1-1
21:44:48.925 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app3-2 node
21:44:48.926 [pool-1-thread-11] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app1-1
21:44:48.926 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app3-2 
21:44:48.926 [pool-1-thread-11] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app1-1, Execution Done!
21:44:48.926 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app1-1 done, with status SUCCESS
21:44:48.926 [pool-1-thread-13] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app3-2
21:44:48.926 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app1-2 node
21:44:48.926 [pool-1-thread-13] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app3-2
21:44:48.926 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app1-2 
21:44:48.927 [pool-1-thread-13] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app3-2, Execution Done!
21:44:48.927 [pool-1-thread-14] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app1-2
21:44:48.927 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app3-2 done, with status SUCCESS
21:44:48.927 [pool-1-thread-14] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app1-2
21:44:48.928 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app3-3 node
21:44:48.928 [pool-1-thread-14] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app1-2, Execution Done!
21:44:48.928 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app3-3 
21:44:48.928 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app1-2 done, with status SUCCESS
21:44:48.929 [pool-1-thread-15] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app3-3
21:44:48.930 [pool-1-thread-15] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app3-3
21:44:48.930 [pool-1-thread-15] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app3-3, Execution Done!
21:44:48.930 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app3-3 done, with status SUCCESS
21:44:48.930 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doExecute 179 - Going to schedule app3-4 node
21:44:48.930 [main] DEBUG c.g.d.core.DefaultExecutionEngine.submit 93 - Received Task app3-4 
21:44:48.931 [pool-1-thread-16] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 44 - Executing Node # app3-4
21:44:48.931 [pool-1-thread-16] INFO  c.g.d.e.MigrationTasksExecutor$DummyTask.execute 148 - Executing Task app3-4
21:44:48.931 [pool-1-thread-16] DEBUG c.g.dexecutor.core.task.LoggerTask.execute 46 - Node # app3-4, Execution Done!
21:44:48.932 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.doWaitForExecution 223 - Processing of node app3-4 done, with status SUCCESS
21:44:48.932 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.execute 130 - Total Time taken to process 16 jobs is 23 ms.
21:44:48.932 [main] DEBUG c.g.dexecutor.core.DefaultDexecutor.execute 131 - Processed Nodes Ordering [base-4, base-1, base-3, app1-3, app1-4, base-2, app3-1, app2-1, app2-2, app2-3, app2-4, app1-1, app3-2, app1-2, app3-3, app3-4]
 
 
 ```
 
 Refer [this blog](https://reachmnadeem.wordpress.com/2016/10/16/take-migration-process-to-next-level-using-dexecutor/) for more detail.
 
 Note that exeuction logic does not executes the SQLs as it is out of scope for this sample application.
 
 
