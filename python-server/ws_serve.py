import asyncio, websockets, threading, time, random, serial, time, os, struct

if os.path.exists("/dev/ttyUSB1") :
    port = serial.Serial("/dev/ttyUSB1", baudrate=115200, timeout=3.0)
elif os.path.exists("/dev/ttyUSB0") :
    port = serial.Serial("/dev/ttyUSB0", baudrate=115200, timeout=3.0)
#port = serial.Serial("/dev/ttyS0", baudrate=115200, timeout=3.0) #for gpio uart

data = ["VALUE",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

@asyncio.coroutine
def wsData(websocket, path):
    while True:
        temp = str(data).strip('[]')
        temp = temp.replace("'", "")
        yield from websocket.send(temp)
        time.sleep(0.005)

def wsThread():
    while True:
        try:
            if port.read().decode() == "S" :
                key = struct.unpack("<B", port.read(1))
                value = struct.unpack("<I", port.read(4))
                data[int(key[0])+1] = value[0]
        except:
            print("Error in port.read()")
        time.sleep(0.0001)
            
if __name__ == '__main__' :
    thread = threading.Thread(target=wsThread)
    thread.start()

    start_server = websockets.serve(wsData, '192.168.1.1', 8887)
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
