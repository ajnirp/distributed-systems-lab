all: *.java
	javac *.java
	java -Djava.rmi.server.hostname="192.168.0.103" DistributedChat client2 eth0

clean:
	rm *.class
