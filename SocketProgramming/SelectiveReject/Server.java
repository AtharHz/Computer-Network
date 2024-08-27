package SelectiveReject;

// mostly receiver

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Server {
    private static StringBuilder data;
    private static int lastReceivedIndex=-1;
    private static int receivedSequence=-1;
    private static String sendingFrame;
    private static String lastReceivedFrame;
    private static int windowSize;
    private static ArrayList<Integer> rejectedFrames;
    private static boolean  inBand=false;
    private static int k=3;
    private static final double PROBABILITY = 0.1;
    private static int lastRightSequence=-1;


    public static void main(String[] args) {
        rejectedFrames=new ArrayList<>();
        data=new StringBuilder();
        windowSize= (int) (Math.pow(2,k-1));
        Random random=new Random();
        try{
            ServerSocket serverSocket=new ServerSocket(234);
            Socket socket=serverSocket.accept();
            while(true) {
                sendingFrame ="null";
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                String frame = null;
                frame = bufferedReader.readLine();
                System.out.println("frame: " + frame);
                if (!frame.equals("null") && frame != null) {
                    if (frame.equals("over")) {
                        printWriter.println("COPY");
                        System.out.println("************    End    ************");
                        break;
                    }
                    System.out.println("char:" + (char) Integer.parseInt(frame.substring(k, k + 8), 2));
                    int sendAck = random.nextInt(5);
                    lastReceivedFrame = frame;
                    checkIfReject(sendAck);
                    System.out.println("this was received from client: " + frame);
                    System.out.println("Received string from client: " + data);
                }
                printWriter.println(sendingFrame);
                System.out.println();
            }
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void checkIfReject(int sendAck){
        long start = System.currentTimeMillis();
        long end = start + 500 ;
        while (System.currentTimeMillis() < end) {}
        if(lastReceivedFrame==null) {
            return;
        }else {
            // p bit 1
            if (lastReceivedFrame.charAt(lastReceivedFrame.length() - 1) == '1') {
                if(rejectedFrames.size()==0){
                    sendRRAck();
                }else {
                    sendingFrame = REJAck(rejectedFrames.get(0) - 1);
                }
                return;
            }
            //k bit sequence number and 8 bit data and p bit
            int tempReceivedSequence = receivedSequence;
            receivedSequence = Integer.parseInt(lastReceivedFrame.substring(0, k), 2);
            if(rejectedFrames.size()!=0) {
                for (Integer num : rejectedFrames) {
                    if (num == receivedSequence) {
                        int temp2 = tempReceivedSequence - num;
                        if (temp2 < 0) temp2 += 8;
                        int temp = lastReceivedIndex - temp2 - 1;
                        System.out.println("lastReceivedIndex " + lastReceivedIndex);
                        System.out.println("tempReceivedSequence " + tempReceivedSequence);
                        System.out.println("num " + num);
                        if (temp < 0) temp = -1;
                        System.out.println("previous :" + data.charAt(temp + 1));
                        data.setCharAt(temp + 1, (char) Integer.parseInt(lastReceivedFrame.substring(k, k + 8), 2));
                        System.out.println("replaced :" + data.charAt(temp + 1));
                        rejectedFrames.remove(num);
                        receivedSequence = tempReceivedSequence;
                        return;
                    }
                }
            }
            System.out.println("lastRightSequence"+lastRightSequence);
            if (receivedSequence == (lastRightSequence + 1) % (windowSize * 2)) {
                lastReceivedIndex++;
                lastRightSequence=lastReceivedIndex;
                System.out.println("------------------------"+lastReceivedIndex);
                data.append((char) Integer.parseInt(lastReceivedFrame.substring(k, k + 8), 2));
                if (sendAck == 0) {
                    sendRRAck();
                }
            }else {
                int firstRejected=0;
                if(rejectedFrames.size()!=0) {
                     firstRejected= rejectedFrames.get(0);
                }else {
                    firstRejected=lastReceivedIndex;
                }
                for (int i = 2; i <= windowSize; i++) {
                    if (receivedSequence == ((firstRejected + i) % (2 * windowSize))) {
                        inBand = true;
                        break;
                    }

                }
                sendREJAck(tempReceivedSequence);
                int number=0;
                if(inBand) {
                    for (int i = 1; i <= windowSize; i++) {
                        if (receivedSequence == ((firstRejected + i) % (2 * windowSize))) {
                            data.append((char) Integer.parseInt(lastReceivedFrame.substring(k, k + 8), 2));
                            lastRightSequence=receivedSequence;
                            number=i;
                            break;
                        } else {
                            data.append('~'); //this means this frame was rejected and needs to be filled later
                            rejectedFrames.add(Integer.parseInt(Integer.toBinaryString((tempReceivedSequence+i)%(windowSize*2)),2));
                        }
                    }
                }
                lastReceivedIndex+=number;
                inBand=false;
            }
        }
        return ;
    }

    public static void sendRRAck(){
        sendingFrame = RRAck();
        System.out.println("Server Sending Ack RR:\t\t"+sendingFrame);
    }

    public static void sendREJAck(int prevSeq){
        sendingFrame=REJAck(prevSeq);
        System.out.println("Server Sending Ack REJ:\t\t"+sendingFrame);
    }

    public static String RRAck(){ // RR: 00000000
        StringBuilder stb=new StringBuilder();
        String temp=Integer.toBinaryString((receivedSequence+1)%(windowSize*2));
        while(temp.length()!=k){
            temp='0'+temp;
        }
        stb.append(temp);
        for(int i=0;i<8+1;i++){
            stb.append(0);
        }
        return stb.toString();
    }

    public static String REJAck(int prevSeq){ // rej: 11111111
        StringBuilder stb=new StringBuilder();
        String temp=Integer.toBinaryString((prevSeq+1)%(windowSize*2));
        while(temp.length()!=k){
            temp='0'+temp;
        }
        stb.append(temp);
        for(int i=0;i<8;i++){
            stb.append(1);
        }
        stb.append(0);
        return stb.toString();
    }
}

