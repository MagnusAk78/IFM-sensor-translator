import socket
import time

ip, port = 'localhost', 3010

# create an ipv4 (AF_INET) socket object using the tcp protocol (SOCK_STREAM)
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# connect the client
# client.connect((target, port))
client.connect((ip, port))

i = 0
# send some data
while True:
    client.send('startsensor1-' + str(i) + '<>sensor2-' + str(1000 - i) + 'end')
    time.sleep(1)
    i = i+1
    if i > 10000:
        i = 0
    print 'sent data, i = ' + str(i)