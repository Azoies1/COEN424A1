import workload.WorkloadProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8887);
        Socket socket = server.accept();

        System.out.println("Client established");
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();

        WorkloadProto.clientRFW clientRFW = null;

        while ((clientRFW = clientRFW.parseDelimitedFrom(input)) != null) {
            System.out.println(clientRFW.toString());
        }
    }
}
