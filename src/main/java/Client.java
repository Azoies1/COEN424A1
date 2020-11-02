
import com.google.gson.Gson;
import models.rfdModel;
import models.rfwModel;
import workload.WorkloadProto;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private static int RFWId = 1;
    private OutputStream protoOutput = null;
    private InputStream protoInput = null;
    private Socket protoSocket = null;

    private OutputStream jsonOutput = null;
    private InputStream jsonInput = null;
    private Socket jsonSocket = null;

    Client(int protoPort, int jsonPort)
    {
        try {
            //ProtoBuf Server socket connection
            protoSocket = new Socket("localhost", protoPort);
            System.out.println("Connected to the protoBuf server: " + protoSocket.getRemoteSocketAddress());
            protoOutput = protoSocket.getOutputStream();
            protoInput = protoSocket.getInputStream();

            //Json Server socket connection
            jsonSocket = new Socket("localhost", jsonPort);
            System.out.println("Connected to the json server: " + jsonSocket.getRemoteSocketAddress() + "\n");
            jsonOutput = jsonSocket.getOutputStream();
            jsonInput = jsonSocket.getInputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Connection Error: The server(s) is not started yet.\n"
                    + "Please exit the client and start again when the server(s) is up");
            closeClient();
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
                int serialType;

                //Benchmark type
                System.out.println("Please Enter the number for the Benchmark Type(-1 to exit):\n"
                        + "1. DVD store testing\n"
                        + "2. DVD store training\n"
                        + "3. NDBench testing\n"
                        + "4. NDBench training");
                bench = numberTypeCheck(sc);

                //Workload type
                System.out.println("Please Enter the number for the Workload Metric(-1 to exit):\n"
                        + "1. CPU utlization average\n"
                        + "2. Network in average\n"
                        + "3. Network out average\n"
                        + "4. Memory utilization average");
                workload = numberTypeCheck(sc);

                System.out.println("Please Enter the Batch unit(-1 to exit):");
                batchUnit = numberCheck(sc);

                System.out.println("Please Enter the Batch Id(-1 to exit):");
                batchId = numberCheck(sc);

                System.out.println("Please Enter the Batch Size(-1 to exit):");
                batchSize = numberCheck(sc);

                System.out.println("Choose the type of serialization to send:\n"
                        + "1. Protobuf serialization\n"
                        + "2. Json serialization");
                serialType = serialTypeCheck(sc);

                //Protobuff Serialization communication between client and server
                if (serialType == 1) {
                    sendProtoBufMessage(bench, workload, batchUnit, batchId, batchSize);
                    readProtoBufMessage();
                }
                //JSON Serialization communication between client and server
                else{
                    sendJsonMessage(bench, workload, batchUnit, batchId, batchSize);
                    readJsonMessage();
                }
                System.out.println("---------------\n");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
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

    //Checks if it is a valid input and between 1 and 4
    private int numberTypeCheck(Scanner sc){
        while (true){
                int newNum = numberCheck(sc);
                if (newNum > 0 && newNum < 5)
                    return newNum;
                System.out.println("Please enter a valid number between the options");
        }
    }

    //Checks if it is a valid input and between 1 or 2
    private int serialTypeCheck(Scanner sc){
        while (true){
            int newNum = numberCheck(sc);
            if (newNum == 1 || newNum == 2)
                return newNum;
            System.out.println("Please enter a valid number between the options");
        }
    }

    //Protobuf Serializing the data and sending the message to the server
    private void sendProtoBufMessage(int bench, int workload, int batchUnit, int batchId, int batchSize) throws IOException {
        WorkloadProto.clientRFW clientRFW = null;

        //serializing protobuf data
        WorkloadProto.clientRFW.Builder clientRFWBuilder = WorkloadProto.clientRFW.newBuilder();
        clientRFWBuilder.setRFWId(RFWId);
        clientRFWBuilder.setBenchType(bench);
        clientRFWBuilder.setWorkLoad(workload);
        clientRFWBuilder.setBatchUnit(batchUnit);
        clientRFWBuilder.setBatchId(batchId);
        clientRFWBuilder.setBatchSize(batchSize);
        RFWId++;
        clientRFW = clientRFWBuilder.build();

        //sending protobuf data
        clientRFW.writeDelimitedTo(protoOutput);
    }

    //Retrieving the ProtoBuf message from the server
    //Deserializing the message and printing out the results
    private void readProtoBufMessage() throws IOException {
        WorkloadProto.serverRFD serverRFD = null;
        //retrieve data from the server
        while ((serverRFD = serverRFD.parseDelimitedFrom(protoInput)) != null) {
            System.out.println(serverRFD.toByteArray());
            System.out.println(serverRFD.toString());
            System.out.println("\nServer Data Received. RFW Id: " + serverRFD.getRFWId());
            System.out.println("Last Batch Id: " + serverRFD.getLastBatchId());
            System.out.println("Data Size: " + serverRFD.getItemCount());
            System.out.println("Data:");
            for (String item : serverRFD.getItemList()) {
                System.out.println(item);
            }
            break;
        }
    }

    //JSON Serializing the data and sending the message to the server
    private void sendJsonMessage(int bench, int workload, int batchUnit, int batchId, int batchSize){
        //assigning the user input to the rfwModel
        rfwModel rfwObject = new rfwModel();
        rfwObject.RFWId = RFWId;
        rfwObject.benchType = bench;
        rfwObject.workLoad = workload;
        rfwObject.batchUnit = batchUnit;
        rfwObject.batchId = batchId;
        rfwObject.batchSize = batchSize;

        //serialise the RFW array(json format)
        Gson gson = new Gson();
        String json = gson.toJson(rfwObject);

        //sends serialised data to the server
        PrintWriter out = new PrintWriter(jsonOutput, true);
        out.println(json);
        out.flush();
    }

    //Retrieving the JSON message from the server
    //Deserializing the message and printing out the results
    private void readJsonMessage() throws IOException {
        String jsonData = null;

        //retrieve the serialized message from the server
        BufferedReader in = new BufferedReader(new InputStreamReader(jsonInput));
        while ( (jsonData = in.readLine()) != null )
        {
            //Deserialize the message
            Gson gson = new Gson();
            rfdModel rfdData = gson.fromJson(jsonData, rfdModel.class);

            //Print out the deserialized message
            System.out.println("\nServer Data Received. RFW Id: " + rfdData.RFWId);
            System.out.println("Last Batch Id: " + rfdData.lastBatchId);
            System.out.println("Data Size: " + rfdData.dataList.size());
            System.out.println("Data:");
            for (String item : rfdData.dataList) {
                System.out.println(item);
            }
            break;
        }
    }


    //Closing the outputStream, inputStream, and the socket of both protoBuf and Json Server
    private void closeClient(){
        try {
            if (protoInput != null) protoInput.close();
            if (protoOutput != null) protoOutput.close();
            if (protoSocket != null) protoSocket.close();
            if (jsonInput != null) jsonInput.close();
            if (jsonOutput != null) jsonOutput.close();
            if (jsonSocket != null) jsonSocket.close();
            System.exit(0);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String [] args) throws IOException {

        //Starting the client object with two server ports
        //The first  port is to connect to the protoBuf server
        //The second  port is to connect to the JSON server
        Client client = new Client(8877, 5588);
        client.serverCommunication();
    }

}
