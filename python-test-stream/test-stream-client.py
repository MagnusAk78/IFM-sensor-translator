import socket
import time

ip, port = 'localhost', 34100

# create an ipv4 (AF_INET) socket object using the tcp protocol (SOCK_STREAM)
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# connect the client
# client.connect((target, port))
client.connect((ip, port))

# send some data
while True:
    myfile = open('testdata.d', 'r')
    for line in myfile: 
        client.send(line)
        print line        
        time.sleep(0.5)
        