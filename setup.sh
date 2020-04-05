#!/bin/bash

# This script assumes the code is complied already you can do this by running `javac src/CPSC559/*.java`

# Copy DBs from the cwd to tmp so they arent modified in the repo itself
cp ./books.csv /tmp/books1.csv
cp ./users.csv /tmp/users1.csv

cp ./books.csv /tmp/books2.csv
cp ./users.csv /tmp/users2.csv

cp ./books.csv /tmp/books3.csv
cp ./users.csv /tmp/users3.csv

# Start the Branch replicas
cd src && java CPSC559.WorkerClass 9001 /tmp/users1.csv /tmp/books1.csv 9002,9003 &
cd src && java CPSC559.WorkerClass 9002 /tmp/users2.csv /tmp/books2.csv 9001,9003 &
cd src && java CPSC559.WorkerClass 9003 /tmp/users3.csv /tmp/books3.csv 9001,9002 &
