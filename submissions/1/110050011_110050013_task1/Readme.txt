Lab 1, Distributed Systems Lab

Team: Shubham Mehta (110050013), Rohan Prinja (110050011)

This Readme.txt file provides instructions for this part (Part 1) as well as the next part (Part 2) of this assignment.

How to run the code: On both the server side and the client side, we assume the following directory structure:

./acc.txt
./Bank.java
./getAccTypeInterface.java
./getAccType.x
./remotetea

First we generate jrpcgen.jar by entering remotetea/ and running 'ant', then we enter the root directory, compile the .x file via
	java -jar remotetea/classes/jrpcgen.jar getAccType.x
then we compile the generated java files via
	javac -classpath .:remotetea/classes/oncrpc.jar *.java
then we run the server via
	java -classpath .:remotetea/classes/oncrpc.jar getAccTypeInterface

On the client side, we run queries like this:
	java -classpath .:remotetea/classes/oncrpc.jar Bank <SERVER_IP_ADDRESS> <COMMAND_NAME> <COMMAND_ARGS>

The query results are printed to the client terminal and the running time is logged in a file named 'log-client.txt' which is located in the project root directory. On the server side, the time at which the RPC function was called will be logged in a file named 'log-server.txt'. The output of the function in the format specified is written to a server-side file named 'output.txt'. If the RPC functions make any change to the server data then the changes will be reflected in the server-side file 'acc.txt'.