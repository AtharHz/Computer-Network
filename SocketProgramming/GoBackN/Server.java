package GoBackN;
// mostly receiver

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server {
    private static StringBuilder data;
    private static int lastReceivedIndex=-1;
    private static int lastValidReceivedSequence =-1;
    private static int receivedSequence=-1;
    private static String sendingFrame;
    private static String lastReceivedFrame;
    private static int windowSize;
    private static int k=3;
    private static final double PROBABILITY = 0.05;


    public static void main(String[] args) {
        data=new StringBuilder();
        windowSize= (int) (Math.pow(2,k)-1);
//        int counter=0;
        Random random=new Random();


        try{
            ServerSocket serverSocket=new ServerSocket(4545);
            Socket socket=serverSocket.accept();

            while(true){
                sendingFrame=null;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                String frame = null;
                frame = bufferedReader.readLine();
                if (!frame.equals("null") && frame != null) {
                    if (frame.equals("over")) {
                        System.out.println("End");
                        break;
                    }
                    if(lastReceivedFrame!=null && lastReceivedFrame!="null"){
                        if(lastReceivedFrame.charAt(lastReceivedFrame.length()-1)=='1'){
                            sendRRAck();
                            printWriter.println(sendingFrame);
                        }
                    }
                    int sendAck = random.nextInt(3);
                    lastReceivedFrame = frame;
                    checkIfReject(sendAck);
                    if (sendAck != 0) {
                        System.out.println("Sending this ack to client: " + sendingFrame);
                    }
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

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Final:  "+data.toString());
    }

    public static void checkIfReject(int sendAck){
        if(lastReceivedFrame==null)
            return;
        // p bit 1
        if(lastReceivedFrame.charAt(lastReceivedFrame.length()-1)=='1'){
            sendRRAck();
            return;
        }
        receivedSequence =Integer.parseInt(lastReceivedFrame.substring(0,k),2);
        if(receivedSequence ==(lastValidReceivedSequence+1)%(windowSize+1)){
            data.append((char)Integer.parseInt(lastReceivedFrame.substring(k,k+8),2));
            lastReceivedIndex++;
            lastValidReceivedSequence=receivedSequence;
            if(sendAck!=0){
                System.out.println("char:"+(char)Integer.parseInt(lastReceivedFrame.substring(k,k+8),2));
                sendRRAck();
            }
        }else{
            sendREJAck();
        }
    }

    public static void sendRRAck(){
        sendingFrame=RRAck();
        System.out.println("Server Sending Ack RR:\t\t"+sendingFrame);
    }

    public static void sendREJAck(){
        sendingFrame=REJAck();
        System.out.println("Server Sending Ack REJ:\t\t"+sendingFrame);
    }

    public static String RRAck(){ // RR: 00000000
        StringBuilder stb=new StringBuilder();
        String temp=Integer.toBinaryString((lastValidReceivedSequence+1)%(windowSize+1));
        while(temp.length()!=k){
            temp='0'+temp;
        }
        stb.append(temp);
        for(int i=0;i<8+1;i++){
            stb.append(0);
        }
        return stb.toString();
    }

    public static String REJAck(){ // rej: 11111111
        StringBuilder stb=new StringBuilder();
        System.out.println("This is the last valid received sequence: "+lastValidReceivedSequence);
        String temp = String.format("%3s", Integer.toBinaryString((lastValidReceivedSequence+1)%(windowSize+1))).replace(' ', '0');
        System.out.println("Here is the temp which is the sequence of rej: "+temp);
        System.out.println("        the last sent index of data in client:"+Client.lastSentIndex);

        stb.append(temp);
        for(int i=0;i<8;i++){
            stb.append(1);
        }
        stb.append(0);
        return stb.toString();
    }
}
