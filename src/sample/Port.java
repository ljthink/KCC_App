package sample;

import purejavacomm.CommPortIdentifier;
import purejavacomm.SerialPort;

import java.io.IOException;
import java.util.ArrayList;

public class Port {

    private volatile SerialPort serialPort = null;

    private int baudRate = 115200;

    boolean Stop = false;

    private static volatile Port PortInstance;

    private ArrayList<Byte> inputData;

    public Port() {
        PortInstance=this;
    }

    public static Port getInstance(){

        if (null == PortInstance){

            synchronized (Port.class){

                if (null == PortInstance){

                    PortInstance = new Port();
                }
            }
        }
        return PortInstance;
    }

    public boolean open(String portName) {
        try {
            CommPortIdentifier portNumber = CommPortIdentifier.getPortIdentifier(portName);
            serialPort = (SerialPort) portNumber.open("Time to Digital Converter", 1000);
            serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            Stop = false;

            return true;
        } catch (Throwable thw) {
            thw.printStackTrace();

            return false;
        }
    }

    public boolean close() {
        if (null != serialPort) {

            try {
                serialPort.close();
                serialPort = null;
                Stop = true;
                return true;
            } catch (Exception ex) {

                return false;
            }
        }
        else{

            return false;
        }
    }

    private byte readOneByte(){
        byte oneByte;

        try {
            oneByte=(byte)serialPort.getInputStream().read();
        }
        catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return oneByte;
    }
    public long readTime() {
        Stop=false;
        long time=0;
        long unsignedData=0;
        int numberOfBytes=0;
        byte data;
        while(time==0){
            if(Stop){
                return 0;
            }
            while (!Stop)
            {
                try {
                    data = readOneByte();

                    if (0 > data) {
                        break;
                    }
                    if (data == 1) {
                        numberOfBytes++;
                        while (numberOfBytes != 0) {
                            switch (numberOfBytes) {
                                case 1:
                                    data = readOneByte();
                                    unsignedData = (data & 0xFF);
                                    time = time | (unsignedData << 32);
                                    numberOfBytes++;
                                    break;
                                case 2:
                                    data = readOneByte();
                                    unsignedData = (data & 0xFF);
                                    time = time | (unsignedData << 24);
                                    numberOfBytes++;
                                    break;
                                case 3:
                                    data = readOneByte();
                                    unsignedData = (data & 0xFF);
                                    time = time | (unsignedData << 16);
                                    numberOfBytes++;
                                    break;
                                case 4:
                                    data = readOneByte();
                                    unsignedData = (data & 0xFF);
                                    time = time | (unsignedData << 8);
                                    numberOfBytes++;
                                    break;
                                case 5:
                                    data = readOneByte();
                                    unsignedData = (data & 0xFF);
                                    time = time | (unsignedData);
                                    numberOfBytes = 0;
                                    Stop = true;
                                    break;
                            }
                        }
                    }

                }
                catch(Exception exe){
                    Controller.getInstance().log("Unable to receive data, try again \n");
                    return 0;
                }
            }
        }

        return time;
    }

    public void SendComment(Commend commend){
        try{
            serialPort.getOutputStream().write(commend.type);
            serialPort.getOutputStream().write(commend.value);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SendRead(byte b) {
        try {
            serialPort.getOutputStream().write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void SendStop(){
        try {
            serialPort.getOutputStream().write(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
