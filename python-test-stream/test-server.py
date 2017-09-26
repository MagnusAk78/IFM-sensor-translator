import socket

HOST = ''
PORT = 34100
ADDR = (HOST,PORT)
BUFSIZE = 4096

serv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

serv.bind(ADDR)
serv.listen(5)

print 'listening ...'

while True:
  conn, addr = serv.accept()
  print 'client connected ... ', addr
  myfile = open('testdata.d', 'w')

  while True:
    data = conn.recv(BUFSIZE)
    if not data: break
    myfile.write(data)
    print 'receiving data:' + data

  conn.close()
  print 'client disconnected'