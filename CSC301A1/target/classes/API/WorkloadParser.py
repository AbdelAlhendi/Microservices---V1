import requests
import json
import sys

def parse(argv):
    config = "config.json"
    with open(config, "r") as j:
            data2 = json.load(j)
    try:
        file = open(argv)
        for line in file:
            line = line.strip("\n")
            line = line.split(" ")
            port = data2.get("OrderService").get("port")
            ip = data2.get("OrderService").get("ip")
            if line[0] == "USER":

                if line[1] == "create": #========================================= USER IF
                    if len(line) == 2:
                        line.append("")
                    if len(line) == 3:
                        line.append("")
                    if len(line) == 4:
                        line.append("")
                    if len(line) == 5:
                        line.append("")

                    url = "http://" + str(ip) + ":" + str(port) + "/user"

                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2], "username": line[3],
                            "email" : line[4], "password" : line[5]}


                    make_post_request(url, data)

                elif line[1] == "update":
                    if len(line) == 5:
                        line.insert(2, "")
                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2], "username" : line[3][9:],
                            "email" : line[4][6:], "password" : line[5][9:]}

                    url = "http://" + str(ip) + ":" + str(port) + "/user"

                    make_post_request(url, data)

                elif line[1] == "delete":
                    if len(line) == 2:
                        line.append("")
                    if len(line) == 3:
                        line.append("")
                    if len(line) == 4:
                        line.append("")
                    if len(line) == 5:
                        line.append("")

                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2], "username": line[3],
                            "email" : line[4], "password" : line[5]}

                    url = "http://" + str(ip) + ":" + str(port) + "/user"

                    make_post_request(url, data)

                elif line[1] == "get":
                    if len(line) == 2:
                        line.append("")
                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2]}

                    url = "http://" + str(ip) + ":" + str(port) + "/user"

                    make_get_request(url, data)

            elif line[0] == "PRODUCT": #========================================= PRODUCT IF
                if line[1] == "create":
                    if len(line) == 2:
                        line.append("")
                    if len(line) == 3:
                        line.append("")
                    if len(line) == 4:
                        line.append("")
                    if len(line) == 5:
                        line.append("")
                    if len(line) == 6:
                        line.append("")

                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2], "name": line[3],
                            "description" : line[4],
                            "price" : line[5],
                            "quantity" : line[6]}

                    url = "http://" + str(ip) + ":" + str(port) + "/product"

                    make_post_request(url, data)

                elif line[1] == "update":
                    if len(line) == 6:
                        line.insert(2, "")
                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2], "name" : line[3][5:],
                            "description" : line[4][12:], "price" : line[5][6:],
                            "quantity" : line[6][9:]}

                    url = "http://" + str(ip) + ":" + str(port) + "/product"
                    
                    make_post_request(url, data)

                elif line[1] == "delete":
                    if len(line) == 2:
                        line.append("")
                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2]}

                    url = "http://" + str(ip) + ":" + str(port) + "/product"

                    make_post_request(url, data)

                elif line[1] == "get":
                    if len(line) == 2:
                        line.append("")
                    data = {"serviceType" : line[0], "command" : line[1],
                            "id" : line[2]}

                    url = "http://" + str(ip) + ":" + str(port) + "/product"

                    make_get_request(url, data)

            elif line[0] == "ORDER": #============================================ ORDER IF
                if line[1] == "place":
                    if len(line) == 2:
                        line.append("")
                    if len(line) == 3:
                        line.append("")
                    if len(line) == 4:
                        line.append("")

                    data = {"serviceType" : line[0], "command" : line[1], 
                            "productid" : line[2], "userid" : line[3],
                            "quantity" : line[4]}

                    url = "http://" + str(ip) + ":" + str(port) + "/order"

                    make_post_request(url, data)
            elif line[0] == "shutdown": #========================================= shutdown if
                data = {}
                url = "http://" + str(ip) + ":" + str(port) + "/shutdown"

                make_post_request(url, data)
            elif line[0] == "restart":#=========================================== restart if
                data = {}
                url = "http://" + str(ip) + ":" + str(port) + "/restart"

                make_post_request(url, data)

        file.close()
    except Exception as e:
        print(e)


def make_post_request(url, data):
    try:
        with open("file", "w") as j:
            json.dump(data, j)

        with open("file", "r") as j:
            data2 = json.load(j)

        headers = {'Content-Type' : 'application/json', 'Authorization' : 'Bearer your_token'}
        response = requests.post(url, json=data2, headers=headers)
        print(response)
        if response.status_code == 200:
            print(f"POST request did work: {response.status_code}")
            print("Response: ", response.text)
        else:
            print(f"POST request did not work: {response.status_code}")
            print("Response: ", response.text)
    except Exception as e:
        print(e)


def make_get_request(url, data):
    try:
        with open("file", "w") as j:
            json.dump(data, j)

        with open("file", "r") as j:
            data2 = json.load(j)

        headers = {'Content-Type' : 'application/json', 'Authorization' : 'Bearer your_token'}
        response = requests.get(url, json=data2, headers=headers)
        print(response)
        if response.status_code == 200:
            print(f"GET request did work: {response.status_code}")
            print("Response: ", response.text)
        else:
            print(f"GET request did not work: {response.status_code}")
            print("Response: ", response.text)
    except Exception as e:
        print(e)


if __name__ == "__main__":
    parse(sys.argv[1])
