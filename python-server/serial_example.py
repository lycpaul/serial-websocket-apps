import serial,time,os,struct


if os.path.exists("/dev/ttyUSB1") :
    port = serial.Serial("/dev/ttyUSB1", baudrate=115200, timeout=3.0)
elif os.path.exists("/dev/ttyUSB0") :
    port = serial.Serial("/dev/ttyUSB0", baudrate=115200, timeout=3.0)
#port = serial.Serial("/dev/ttyS0", baudrate=115200, timeout=3.0) #for gpio uart

data = ["VALUE",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

while True:
    if port.read().decode() == "S" :
        key = struct.unpack("<B", port.read(1))
        value = struct.unpack("<I", port.read(4))
        data[int(key[0])+1] = value[0]
    print(data)
