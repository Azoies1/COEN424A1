import workload.WorkloadProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Server {

    private ServerSocket server;
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    Server(){
        try {
            server = new ServerSocket(8887);
            System.out.println("Server started");
            System.out.println("Waiting for client connection");
            socket = server.accept();
            System.out.println("Client established");
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Main program detecting the input and communication with the client
    private void clientCommunication() throws IOException {

        //Protobuff Serialization communication between client and server
        WorkloadProto.clientRFW clientRFW = null;
        WorkloadProto.serverRFD serverRFD = null;

        //receive data from the client
        while ((clientRFW = clientRFW.parseDelimitedFrom(input)) != null) {
            System.out.println("\nClient data received. Client RFW id: " + clientRFW.getRFWId());
            System.out.println(clientRFW.toString());
            List<String> data = readRfwData(clientRFW);
            int lastBatchId = clientRFW.getBatchId() + clientRFW.getBatchSize();

            System.out.println("Serializing and sending the data to the client\n");
            //Serializing the data with Protobuff and sending it to the client
            serverRFD = protoSerialization(clientRFW.getRFWId(), lastBatchId, data);
            serverRFD.writeDelimitedTo(output);
        }
    }

    //reading and setting the CSV files in the CSVreader class
    private void readCSVFiles(){
        String csvFile = "./src/main/java/data/DVD-testing.csv";
        CSVreader.Serialize(csvFile);
        csvFile = "./src/main/java/data/DVD-training.csv";
        CSVreader.Serialize(csvFile);
        csvFile = "./src/main/java/data/NDBench-testing.csv";
        CSVreader.Serialize(csvFile);
        csvFile = "./src/main/java/data/NDBench-training.csv";
        CSVreader.Serialize(csvFile);
        System.out.println("Data Files read\n");
    }

    //retrieve the list of data from the csv file with the protobuff data
    private List<String> readRfwData(WorkloadProto.clientRFW clientRFW){
        int workload = clientRFW.getWorkLoad() - 1;
        int batchUnit = clientRFW.getBatchUnit();
        int batchId = clientRFW.getBatchId();
        int batchSize = clientRFW.getBatchSize();

        int lineStart = batchUnit * (batchId);
        int lineEnd = batchUnit * (batchId + batchSize);
        return CSVreader.getFileData(clientRFW.getBenchType() - 1, workload, lineStart, lineEnd);
    }

    //Proto buff serialization of the data
    private WorkloadProto.serverRFD protoSerialization(int rfwId, int lastBatchId, List<String> data){
        WorkloadProto.serverRFD.Builder serverRFD = WorkloadProto.serverRFD.newBuilder();
        serverRFD.setRFWId(rfwId);
        serverRFD.setLastBatchId(lastBatchId);
        serverRFD.addAllItem(data);
        return serverRFD.build();
    }

    //Closing the outputStream, inputStream, and the socket
    private void closeServer(){
        try {
            input.close();
            output.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.readCSVFiles();
        server.clientCommunication();
        server.closeServer();
        System.exit(0);
    }

}
