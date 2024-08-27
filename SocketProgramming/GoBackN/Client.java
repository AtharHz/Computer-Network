package GoBackN;
//mostly transmitter

import java.io.*;
import java.net.Socket;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public static String data;
    public static ArrayList<Integer> dataTimeOut;
    public static String lastReceivedFrame=null;
    public static int lastSentIndex=-1;
    public static int lastSentSequence=-1;
    public static int windowSize;
    public static int k=3;
    public static int lastAcknowledgedIndex=-1;
    public static int lastAcknowledgedSequence=-1;
    public static final double PROBABILITY = 0.05;
    public static int packet=0;

    public static void main(String[] args) {
        Scanner cin=new Scanner(System.in);
        windowSize= (int) Math.pow(2,k)-1;
        System.out.println("Enter the text you want to send to the server:");
        data=cin.nextLine();
        boolean askForRRPbit1=false;
        dataTimeOut=new ArrayList<>();
        for(int i=0;i<data.length();i++){
            dataTimeOut.add(-1);
        }

        try {
            Socket socket=new Socket("localhost",4545);

            while(true){
                PrintWriter printWriter=new PrintWriter(socket.getOutputStream(),true);
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if(lastSentIndex==data.length()-1){
                    printWriter.println("over");
                    break;
                }
                String ack=null;
                while((lastAcknowledgedSequence)!=(lastSentSequence+1)%(windowSize+1) && lastSentIndex<data.length()){
                    askForRRPbit1=true;

                    String transmitting=transmitFrame();
                    if(Math.random() > PROBABILITY){
                    printWriter.println(transmitting);
                    System.out.println("Transmitting to server: "+transmitting);
                    for(int i=0;i<lastSentIndex && dataTimeOut.get(i)>-1;i++){
                        if(dataTimeOut.get(i)>4){
                            //transmit this frame
                            Thread.sleep(1000);
                            printWriter.println(dataFrame(i));
                            dataTimeOut.set(i,0);
                        }
                        if(lastAcknowledgedIndex<i){
                            dataTimeOut.set(i,dataTimeOut.get(i)+1);
                        }
                    }
                    Thread.sleep(1000);
                    }else{
                        System.out.println("[X] Lost ack with frame " +transmitting);
                        String temp=null;
                        printWriter.println(temp);
                    }
                    ack=bufferedReader.readLine();
                    if(ack!=null && !ack.equals("null")) {
                        System.out.println("received ack"+ack);
                        lastReceivedFrame=ack;
                    }
                    break;
                }
                printWriter.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static String transmitFrame(){
        if(lastReceivedFrame!=null && !lastReceivedFrame.equals("null")){
            int sequence=Integer.parseInt(lastReceivedFrame.substring(0,k),2);
            boolean isRR=(lastReceivedFrame.charAt(k) == '0');
            if(!isRR){
                int tempp=lastSentSequence;

                if(sequence==0)
                    lastSentSequence=7;
                else lastSentSequence=(sequence-1)%(windowSize+1);
                lastSentIndex-=Math.abs(tempp-lastSentSequence);
                if(lastSentIndex<0) lastSentIndex=0;

                int temp=lastAcknowledgedSequence;
                if(sequence==0)
                    lastAcknowledgedSequence=7;
                else lastAcknowledgedSequence=(sequence-1)%(windowSize+1);
                lastAcknowledgedIndex-=Math.abs(lastAcknowledgedSequence-temp);
            }else if(isRR){
                int temp=lastAcknowledgedSequence;
                if(sequence==0)
                    lastAcknowledgedSequence=7;
                else lastAcknowledgedSequence=(sequence-1)%(windowSize+1);
                int counter=0;
                while(lastAcknowledgedSequence!=temp){
                    temp++;
                    temp%=(windowSize+1);
                    counter++;
                }
                lastAcknowledgedIndex+=counter;
            }
            lastReceivedFrame=null;
        }
        System.out.println("index of last acknowledged frame: "+lastAcknowledgedIndex);
        return dataFrame();

        //seq: 0 1 2 3 4 5 6 7
        //ind: 0 1 2 3 4 5 6 7
    }

    public static String dataFrame(){
        StringBuilder stb=new StringBuilder();
        String temp=Integer.toBinaryString((lastSentSequence+1)%(windowSize+1));
        while(temp.length()!=k){
            temp='0'+temp;
        }
        stb.append(temp);
        lastSentSequence++;
        lastSentSequence%=(windowSize+1);
        String temp2=null;
        temp2=Integer.toBinaryString(data.charAt(lastSentIndex+1));
        System.out.println("sending char: "+data.charAt(lastSentIndex+1));
        while(temp2.length()!=8){
            temp2='0'+temp2;
        }
        stb.append(temp2);
        lastSentIndex++;
        dataTimeOut.set(lastSentIndex,0);
        stb.append("0");
        return stb.toString();
    }

    public static String dataFrame(int dataIndex){
        StringBuilder stb=new StringBuilder();
        String temp=Integer.toBinaryString((dataIndex)%(windowSize+1));
        while(temp.length()!=k){
            temp='0'+temp;
        }
        stb.append(temp);
        String temp2=null;
        temp2=Integer.toBinaryString(data.charAt(dataIndex));
        System.out.println("sending char: "+data.charAt(dataIndex));
        while(temp2.length()!=8){
            temp2='0'+temp2;
        }
        stb.append(temp2);
        stb.append("0");
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
