import workload.WorkloadProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class protoBufServer {

    private CSVreader csVreader = null;
    private ServerSocket server = null;
    private Socket socket = null;
    private InputStream input = null;
    private OutputStream output = null;
    protoBufServer(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("ProtoBuf Server started");
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
            System.out.println(clientRFW.toByteArray());
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
        csVreader = new CSVreader();
        csVreader.readCSVFiles();
    }

    //retrieve the list of data from the csv file with the protobuff data
    private List<String> readRfwData(WorkloadProto.clientRFW clientRFW){
        int workload = clientRFW.getWorkLoad() - 1;
        int batchUnit = clientRFW.getBatchUnit();
        int batchId = clientRFW.getBatchId();
        int batchSize = clientRFW.getBatchSize();

        int lineStart = batchUnit * (batchId);
        int lineEnd = batchUnit * (batchId + batchSize);
        return csVreader.getFileData(clientRFW.getBenchType() - 1, workload, lineStart, lineEnd);
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
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            System.exit(0);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        //Potential to use the args for port number
        protoBufServer server = new protoBufServer(8877);
        server.readCSVFiles();
        server.clientCommunication();
        server.closeServer();
        System.exit(0);
    }

}
