import asyncio, websockets, threading, time, random, serial, time, os

port = None

if os.path.exists("/dev/ttyUSB1") :
    port = serial.Serial("/dev/ttyUSB1", baudrate=115200, timeout=3.0)
elif os.path.exists("/dev/ttyUSB0") :
    port = serial.Serial("/dev/ttyUSB0", baudrate=115200, timeout=3.0)
elif os.path.exists("/dev/ttyS0") :
    port = serial.Serial("/dev/ttyS0", baudrate= 115200, timeout=3.0)

data = ["VALUE",1234,123.124,1423.25,45.23452,1,1,2,3,4,'very nice',656,123,142,142,35,124]

@asyncio.coroutine
def wsData(websocket, path):
    # handling the websocket data to the apps
    while True:
        temp = str(data).strip('[]')
        temp = temp.replace("'", "")
        yield from websocket.send(temp)
        time.sleep(0.1)

def wsThread():
    # handling the serial data
    while True:
        rcv = port.readline().decode().split()
        if len(rcv) == 16:
            data[1:] = rcv
        data[2] = 20 + random.randrange(100)/100
        data[8] = 30 + random.randrange(100)/100
        data[4] = 40 + random.randrange(100)/100
        data[5] = 50 + random.randrange(100)/100
        data[6] = 60 + random.randrange(100)/100
        data[7] = 70 + random.randrange(100)/100
        time.sleep(0.1)

if __name__ == '__main__' :
    thread = threading.Thread(target=wsThread)
    thread.start()

    start_server = websockets.serve(wsData, '192.168.1.1', 8887)
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
