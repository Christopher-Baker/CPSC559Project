worker:
	javac src/CPSC559/WorkerThread.java src/CPSC559/Book.java src/CPSC559/User.java src/CPSC559/UserDB.java src/CPSC559/BookDB.java src/CPSC559/WorkerClass.java
balancer:
	javac src/CPSC559/LoadBalancerMain.java src/CPSC559/LoadBalancer.java src/CPSC559/BalancerWorker.java src/CPSC559/UsageChecker.java src/CPSC559/SocketUsagePair.java
client:
	javac src/CPSC559/Client.java
all: worker balancer client
