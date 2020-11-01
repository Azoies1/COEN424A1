import workload.WorkloadProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8887);
        Socket socket = server.accept();
        System.out.println("Client established");
        readCSVFiles();

        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();

        WorkloadProto.clientRFW clientRFW = null;
        WorkloadProto.serverRFD serverRFD = null;
        List<String[]> fileData = null;

        //receive data from the client
        while ((clientRFW = clientRFW.parseDelimitedFrom(input)) != null) {
            System.out.println("\nClient data received. Client RFW id: " + clientRFW.getRFWId());
            List<String> data = readRfwData(clientRFW);
            int lastBatchId = clientRFW.getBatchId() + clientRFW.getBatchSize();

            System.out.println("Serializing and sending the data to the client\n");
            //Serializing the data with Protobuff and sending it to the client
            serverRFD = protoSerialization(clientRFW.getRFWId(), lastBatchId, data);
            serverRFD.writeDelimitedTo(output);
            break;
        }
    }

    //reading and setting the CSV files in the CSVreader class
    private static void readCSVFiles(){
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

    //retrieve the list of data from the csv file with the
    private static List<String> readRfwData(WorkloadProto.clientRFW clientRFW){
        int workload = clientRFW.getWorkLoad();
        int batchUnit = clientRFW.getBatchUnit();
        int batchId = clientRFW.getBatchId();
        int batchSize = clientRFW.getBatchSize();

        int lineStart = batchUnit * (batchId);
        int lineEnd = batchUnit * (batchId + batchSize);
        return CSVreader.getFileData(clientRFW.getBenchType(), workload, lineStart, lineEnd);
    }

    //Proto buff serialization of the data
    private static WorkloadProto.serverRFD protoSerialization(int rfwId, int lastBatchId, List<String> data){
        WorkloadProto.serverRFD.Builder serverRFD = WorkloadProto.serverRFD.newBuilder();
        serverRFD.setRFWId(rfwId);
        serverRFD.setLastBatchId(lastBatchId);
        serverRFD.addAllItem(data);
        return serverRFD.build();
    }

}
