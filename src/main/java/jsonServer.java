import com.google.gson.Gson;
import models.rfdModel;
import models.rfwModel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class jsonServer {

    private CSVreader csVreader = null;
    private ServerSocket server = null;
    private Socket socket = null;
    private InputStream input = null;
    private OutputStream output = null;
    jsonServer(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("JSON Server started");
            System.out.println("Waiting for client connection");
            socket = server.accept();
            System.out.println("Client established");
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            closeServer();
        }
    }

    //Main program detecting the input and communication with the client
    private void clientCommunication() throws IOException {
        String jsonData = null;

        //receive serialized message from the client
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        while ((jsonData = in.readLine()) != null )
        {
            Gson gson = new Gson();
            rfwModel rfwData = gson.fromJson(jsonData, rfwModel.class);

            System.out.println("\nClient serialized data received. Client RFW id: " + rfwData.RFWId);
            System.out.println("Serialized data: " + jsonData);
            List<String> data = readRfwData(rfwData);
            int lastBatchId = rfwData.batchId + rfwData.batchSize;

            System.out.println("Serializing and sending the data to the client\n");
            //Serializing the data with JSON and sending it to the client
            String serializedString = jsonSerialization(rfwData.RFWId, lastBatchId, data);

            PrintWriter out = new PrintWriter(output, true);
            out.println(serializedString);
            out.flush();
        }
    }

    //reading and setting the CSV files in the CSVreader class
    private void readCSVFiles(){
        try {
            csVreader = new CSVreader();
            csVreader.readCSVFiles();
        }
        catch (Exception e){
            System.out.println(e.toString());
            closeServer();
        }
    }

    //retrieve the list of data from the csv file with the JSON data
    private List<String> readRfwData(rfwModel clientRFW){
        int workload = clientRFW.workLoad - 1;
        int batchUnit = clientRFW.batchUnit;
        int batchId = clientRFW.batchId;
        int batchSize = clientRFW.batchSize;

        int lineStart = batchUnit * (batchId);
        int lineEnd = batchUnit * (batchId + batchSize);
        return csVreader.getFileData(clientRFW.benchType - 1, workload, lineStart, lineEnd);
    }

    //JSON serialization of the data
    private String jsonSerialization(int rfwId, int lastBatchId, List<String> data){
        rfdModel serverRFD = new rfdModel();
        serverRFD.RFWId = rfwId;
        serverRFD.lastBatchId = lastBatchId;
        serverRFD.dataList = data;

        Gson gson = new Gson();
        String json = gson.toJson(serverRFD);

        return json;
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
        jsonServer server = new jsonServer(5588);
        server.readCSVFiles();
        server.clientCommunication();
        server.closeServer();
        System.exit(0);
    }

}

