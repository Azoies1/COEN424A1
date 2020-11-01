
import workload.WorkloadProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private static int RFWId = 1;
    private OutputStream output;
    private InputStream input;
    private Socket s;

    Client()
    {
        try {
            s = new Socket("localhost", 8887);
            System.out.println("Connected to the server");
            output = s.getOutputStream();
            input = s.getInputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Connection Error: Server is not started yet.\n"
                    + "Please exit the client and start again when the server is up");
            System.exit(0);
        }
    }

    //Loop to check ask for user inputs and to communicate with the server
    private void serverCommunication(){

        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Welcome to client for requesting workload data from the server");

            while (true) {
                int bench;
                int workload;
                int batchUnit;
                int batchId;
                int batchSize;

                //Benchmark type
                System.out.println("Please Enter the number for the Benchmark Type(-1 to exit):\n"
                        + "1. DVD store testing\n"
                        + "2. DVD store training\n"
                        + "3. NDBench testing\n"
                        + "4. NDBench training\n");
                bench = numberTypeCheck(sc);

                //Workload type
                System.out.println("Please Enter the number for the Workload Metric(-1 to exit):\n"
                        + "1. CPU utlization average\n"
                        + "2. Network in average\n"
                        + "3. Network out average\n"
                        + "4. Memory utilization average\n");
                workload = numberTypeCheck(sc);
                //check if within the options

                System.out.println("Please Enter the Batch unit(-1 to exit):");
                batchUnit = numberCheck(sc);
                //check if within bounds

                System.out.println("Please Enter the Batch Id(-1 to exit):");
                batchId = numberCheck(sc);

                System.out.println("Please Enter the Batch Size(-1 to exit):");
                batchSize = numberCheck(sc);

                //Protobuff Serialization communication between client and server
                WorkloadProto.clientRFW clientRFW = null;
                WorkloadProto.serverRFD serverRFD = null;
                //serializing protobuff data
                clientRFW = protoSerialization(bench, workload, batchUnit
                        , batchId, batchSize);

                //sending protobuff data
                clientRFW.writeDelimitedTo(output);

                //retrieve data from the server
                while ((serverRFD = serverRFD.parseDelimitedFrom(input)) != null) {
                    System.out.println("\nServer Data Received. RFW Id: " + serverRFD.getRFWId());
                    System.out.println("Last Batch Id: " + serverRFD.getLastBatchId());
                    System.out.println("Data Size: " + serverRFD.getItemCount());
                    System.out.println("Data:");
                    for (String item : serverRFD.getItemList()) {
                        System.out.println(item);
                    }
                    break;
                }

                System.out.println("---------------\n");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    //Checks if it is a valid input and between 1 and 4
    //Checks if the user wants to quit the program
    private int numberTypeCheck(Scanner sc){
        while (true){
            try {
                int newNum = sc.nextInt();
                if (newNum == -1)
                    closeClient();
                else if (newNum > 0 && newNum < 5)
                    return newNum;
                System.out.println("Please enter a valid number between the options");
            }
            catch (Exception e) {
                System.out.println("Please enter a valid number");
                sc.next();
            }
        }
    }

    //Checks if it is a valid input and greater than 0
    //Checks if the user wants to quit the program
    private int numberCheck(Scanner sc){
        while (true){
            try {
                int newNum = sc.nextInt();
                if (newNum == -1)
                    closeClient();
                else if (newNum > 0)
                    return newNum;
                System.out.println("Please enter a number above 0");
            }
            catch (Exception e) {
                System.out.println("Please enter a valid number");
                sc.next();
            }
        }
    }

    //Serializing the data with the Proto Buff serialization
    private WorkloadProto.clientRFW protoSerialization
            (int bench, int workload, int batchUnit, int batchId, int batchSize){
        WorkloadProto.clientRFW.Builder clientRFW = WorkloadProto.clientRFW.newBuilder();
        clientRFW.setRFWId(RFWId);
        clientRFW.setBenchType(bench);
        clientRFW.setWorkLoad(workload);
        clientRFW.setBatchUnit(batchUnit);
        clientRFW.setBatchId(batchId);
        clientRFW.setBatchSize(batchSize);
        RFWId++;
        return clientRFW.build();
    }

    //Closing the outputStream, inputStream, and the socket
    private void closeClient(){
        try {
            input.close();
            output.close();
            s.close();
            System.exit(0);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String [] args) throws IOException {

        Client client = new Client();
        client.serverCommunication();
    }

}
