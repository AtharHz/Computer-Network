package SelectiveReject;

//mostly transmitter

import java.io.*;
import java.net.Socket;
import java.security.spec.ECField;
import java.util.Scanner;

public class Client {
    public static String data;
    public static String lastReceivedFrame=null;
    public static int lastSentIndex=-1;
    public static int lastSentSequence=-1;
    public static int windowSize;
    public static int k=3;
    public static int lastAcknowledgedIndex=-1;
    public static int lastAcknowledgedSequence=-1;
    private static DataInputStream input = null;
    private static DataOutputStream out = null;
    public static final double PROBABILITY = 0.1;
    public static int packet=0;
    public static boolean sending=true;
    public static boolean endSending=false;

    public static void main(String[] args) {
        Scanner cin=new Scanner(System.in);
        windowSize= (int) Math.pow(2,k-1);
        System.out.println("Enter the text you want to send to the server:");
        data=cin.nextLine();

        try {
            Socket socket=new Socket("localhost",234);

            while(true){
                System.out.println(".........................");
                PrintWriter printWriter=new PrintWriter(socket.getOutputStream(),true);
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if(lastSentIndex==data.length()-1){
                    printWriter.println("over");
                    endSending=true;
                }
                String transmitting="null";
                if((lastAcknowledgedSequence)!=(lastSentSequence+windowSize)%(windowSize*2)) {
                    sending = true;
                }
                String ack=null;
                if(sending && !endSending) {
                    transmitting = transmitFrame();
                    if(Math.random() > PROBABILITY ){
                        printWriter.println(transmitting);
                        System.out.println("Transmitting to server: " + transmitting);
                    }else{
                        System.out.println("[X] Lost ack with frame " +transmitting);
                        String temp=null;
                        printWriter.println(temp);
                    }
                }
                if(!sending){
                    printWriter.println("null");
                }
                ack=bufferedReader.readLine();
                if(ack!=null && !ack.equals("null")) {
                    if(ack.equals("COPY")){
                        break;
                    }
                    System.out.println("received ack"+ack);
                    lastReceivedFrame=ack;
                    transmitting = transmitFrame();
                    System.out.println("Transmitting to server in ack: " + transmitting);
                    printWriter.println(transmitting);
                }else if((lastAcknowledgedSequence)==(lastSentSequence+windowSize)%(windowSize*2)){
                    if (!sending) {
                        transmitting=RRPBit1Frame();
                        printWriter.println(transmitting);
                    }
                }
                sending=false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static String transmitFrame(){
        long start = System.currentTimeMillis();
        long end = start + 500 ;
        while (System.currentTimeMillis() < end) {}
        if(lastReceivedFrame!=null && !lastReceivedFrame.equals("null")){
            int sequence=Integer.parseInt(lastReceivedFrame.substring(0,k),2);
            boolean isRR=(lastReceivedFrame.charAt(k) == '0');
            if(!isRR){
                System.out.println("//////////////////////////////");
                System.out.println("lastSentIndex "+lastSentIndex);
                System.out.println("lastSentSequence "+lastSentSequence);
                System.out.println("sequence "+sequence);
                int temp3=lastSentSequence-sequence;
                if(temp3<0) temp3+=8;
                int tempIndex=lastSentIndex-temp3-1;
                if(tempIndex<0) tempIndex=-1;
                StringBuilder rejectedFrame=new StringBuilder();
                rejectedFrame.append(lastReceivedFrame.substring(0,k));
                System.out.println("data :"+data.charAt(tempIndex+1));
                String temp2=(Integer.toBinaryString(data.charAt(tempIndex+1)));
                while(temp2.length()!=8){
                    temp2='0'+temp2;
                }
                rejectedFrame.append(temp2);
                rejectedFrame.append("0");
                lastReceivedFrame=null;
                return rejectedFrame.toString();
            }else if(isRR){
                lastAcknowledgedSequence=(sequence-1)%(windowSize*2);
            }
            lastReceivedFrame=null;
        }
        return dataFrame();
    }

    public static String dataFrame(){
        if(endSending){
            return "";
        }
        StringBuilder stb=new StringBuilder();
        String temp=Integer.toBinaryString((lastSentSequence+1)%(windowSize*2));
        while(temp.length()!=k){
            temp='0'+temp;
        }
        stb.append(temp);
        lastSentSequence++;
        lastSentSequence%=(windowSize*2);
        String temp2=Integer.toBinaryString(data.charAt(lastSentIndex+1));
        System.out.println("sending char: "+data.charAt(lastSentIndex+1));
        while(temp2.length()!=8){
            temp2='0'+temp2;
        }
        stb.append(temp2);
        lastSentIndex++;
        stb.append("0");
        System.out.println("lastSentIndex"+lastSentIndex);
        return stb.toString();
    }

    public static String RRPBit1Frame(){
        StringBuilder stb=new StringBuilder();
        for(int i=0;i<k;i++){
            stb.append(0);
        }
        return stb.append("000000001").toString();
    }
}

