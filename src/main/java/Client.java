
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main (String [] args) throws IOException {
        Socket s = new Socket("localhost", 8887);

        System.out.println("Welcome to client for requesting workload data from the server\n");

        Scanner sc = new Scanner(System.in);

        try {
            while (true) {
                //Benchmark type
                System.out.println("Please Enter the number for the Benchmark Type:\n"
                        + "1. DVD store\n"
                        + "2. NDBench\n");
                int bench = sc.nextInt();
                //check if within the options

                System.out.println("Please Enter the number for the Workload Metric:\n"
                        + "1. CPU utlization average\n"
                        + "2. Network in average\n"
                        + "3. Network out average\n"
                        + "4. Memory utilization average\n");
                int workload = sc.nextInt();
                //check if within the options

                System.out.println("Please Enter the Batch unit (sample size)");
                int batchUnit = sc.nextInt();
                //check if within bounds

                System.out.println("Please Enter the Batch Id");
                int batchId = sc.nextInt();

                System.out.println("Please Enter the Batch Size (batches to return)");
                int batchSize = sc.nextInt();

                System.out.println(bench + " " + workload + " " + batchUnit
                        + " " + batchId + " " + batchSize + "\n");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

}
