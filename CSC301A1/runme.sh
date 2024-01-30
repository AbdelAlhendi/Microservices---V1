if [[ $1 = "-c" ]]; then
    javac -d "compiled/" -cp "lib/*" src/main/java/API/UserService.java
    javac -d "compiled/" -cp "lib/*" src/main/java/API/OrderService.java
    javac -d "compiled/" -cp "lib/*" src/main/java/API/ProductService.java
elif [[ $1 = "-u" ]]; then
    java -cp "lib/*:compiled" API.UserService config.json
elif [[ $1 = "-p" ]]; then
    java -cp "lib/*:compiled" API.ProductService config.json
elif [[ $1 = "-i" ]]; then
    echo "No ISCS Implemented for this assignment."
elif [[ $1 = "-o" ]]; then
    java -cp "lib/*:compiled" API.OrderService config.json
elif [[ $1 = "-w" ]]; then
    python3 src/main/java/API/WorkloadParser.py "$2"
fi