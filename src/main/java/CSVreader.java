
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVreader {

    private List<List<String[]>> csvDataList = new ArrayList<>();

    public void readCSVFiles(){
        try {
            String csvFile = "./DVD-testing.csv";
            readCSVFile(csvFile);
            csvFile = "./DVD-training.csv";
            readCSVFile(csvFile);
            csvFile = "./NDBench-testing.csv";
            readCSVFile(csvFile);
            csvFile = "./NDBench-training.csv";
            readCSVFile(csvFile);
            System.out.println("Data Files read\n");
        }catch (Exception e)
        {
            throw e;
        }
    }

    public void readCSVFile(String csvFile) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            List<String[]> listOfLines = new ArrayList<>();
            br = new BufferedReader(new FileReader(csvFile));

            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] lineArray = line.split(cvsSplitBy);
                lineArray = Arrays.copyOf(lineArray,4);
                listOfLines.add(lineArray);
            }
            csvDataList.add(listOfLines);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> getFileData(int benchType, int workload, int lineStart, int lineEnd){
    	    List<String> returnData = new ArrayList<>();

    	    //Get the data of the file
    	    List<String[]> dataFile = csvDataList.get(benchType);

    	    //Go through each line of the data
    	    for (int i = lineStart; i < lineEnd; i++){
    	        //retrieve the workload data
                returnData.add(dataFile.get(i)[workload]);
            }
    	    return returnData;
    }

}