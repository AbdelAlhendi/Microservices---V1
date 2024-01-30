# Microservices---V1
This README.txt file will contain the nesscary information to run the code.

Firstly, place all IPs and Ports in the config.json file in an appropriate JSON format. Please also ensure it is in a similar structure
to the following:

{
  "UserService": {
    "port": 14001,
    "ip": "127.0.0.1"
  }   ,
  "OrderService": {
    "port": 14000,
    "ip": "127.0.0.1"
  }   ,
  "ProductService": {
    "port": 15000,
    "ip": "127.0.0.1"
  }   ,
  "InterServiceCommunication": {
    "port": 14000,
    "ip": "127.0.0.1"
  }
}

Simply replace the nesscary "port" and "ip" values to correspond to the computer used and the wanted port number.

You must also ensure that the request import is installed on the device for the WorkloadParser. To do this, simply open up a terminal and write
"pip install requests". If requests is already installed, then this is not needed.

Before you can actually call the ./runme.sh, you need to give the current directory permission. Do this by writing chmod +x runme.sh
in the CSC301A1 directory you are currently in. Once this is done, you can call any ./runme.sh commands.

You must also use wget to install a vital driver for SQLite. To do this, simply call ./runme.sh -j
Now you should be ready to run the code.

To run the code itself, simply open up a terminal, navigate to the CSC301A1 directory, and call runme.sh commands with
the appropiate flags. The runme.sh has flags to compile all of the code, and start all individual services. 
Call ./runme.sh -c to first compile all of the code. Then call ./runme.sh -u, ./runme.sh -o, and ./runme.sh -p in any order 
to start up all the services. Finally, start the WorkloadParser with ./runme.sh -w. You can also add any .txt files as arguments to
runme.sh -w. For example, runme.sh -w "testfile.txt". To stop any of the servers, simply use ctrl-c on the command terminal you which to close. A "shutdown"
request should also shutdown everything as well.

Additional Note:
I've also included a git repository to this CSC301A1 folder as well. This folder will fully contain all needed files and folders, including the SQLite
drivers. This should not be needed at all hopefully. It is simply here as a worst case measure. Thank you for understanding.
