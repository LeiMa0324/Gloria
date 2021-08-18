package users;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class dataProcess {
    public static void main(String[] args) throws ParseException, FileNotFoundException {



        Scanner scanner = new Scanner(new File("src/main/resources/Revision/Nasdaq.csv"));
        scanner.nextLine();

        int eventNum =0;
        ArrayList<String> kleenes = new ArrayList();
        kleenes.add("TVIX");
        kleenes.add("ARDX");
        kleenes.add("QQQ");
        kleenes.add("TQQQ");
        kleenes.add("CDTX");
        kleenes.add("NAKD");
        kleenes.add("AMD");
        kleenes.add("ROKU");
        kleenes.add("SQQQ");
        kleenes.add("ABEO");

        String lastEvent = "";
        String lastTimeStamp= "";
        ArrayList<String> processedStream = new ArrayList<>();
        ArrayList<String> tempBurst = new ArrayList<>();
        ArrayList<ArrayList<String>> bursts = new ArrayList<>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");

            String event = values[0];
            String timeStamp = values[1];
            lastEvent = lastEvent.equals("")?event: lastEvent;
            lastTimeStamp = lastTimeStamp.equals("")?timeStamp:lastTimeStamp;

            if (event.equals(lastEvent)&&timeStamp.equals(lastTimeStamp)){
                tempBurst.add(line);
            }else {
                ArrayList<String> finishedBurst = (ArrayList<String>) tempBurst.clone();
                bursts.add(finishedBurst);
                tempBurst.clear();

                tempBurst.add(line);
                lastEvent = event;
                lastTimeStamp = timeStamp;
            }

        }
        int vol_threshold_1 = 30;   //vol>30,
        int vol_threshold_2 = 100;  //vol>100

        ArrayList<ArrayList<String>> newBursts = new ArrayList<>();
        for (ArrayList<String> burst: bursts){
            ArrayList<String> newBurst = new ArrayList<>();

            if (kleenes.contains(burst.get(0).split(",")[0])){
                double chance = Math.random();

                if (chance<0.3){
                    //alternating vol values in the burst

                    for (int i =0; i< burst.size(); i++){
                        Random random = new Random();
                        int vol = (i%2==0)? random.nextInt(vol_threshold_2-vol_threshold_1)+vol_threshold_1: random.nextInt(vol_threshold_2)+100;
                        String[] values = burst.get(i).split(",");
                        values[6] = vol+"";

                        StringBuilder newline = new StringBuilder();
                        newline.append(values[0]);
                        for (int j=1;j< values.length; j++){
                            newline.append(",").append(values[j]);
                        }
                        newBurst.add(newline.toString());
                    }
                }else{
                    for (String event: burst){
                        Random random = new Random();
                        int vol = random.nextInt(vol_threshold_2)+100;
                        String[] values =event.split(",");
                        values[6] = vol+"";

                        StringBuilder newline = new StringBuilder();
                        newline.append(values[0]);
                        for (int j=1;j< values.length; j++){
                            newline.append(",").append(values[j]);
                        }

                        newBurst.add(newline.toString());

                    }
                }
            }
            else {
                newBurst = burst;
            }

            newBursts.add(newBurst);
        }

        File file = new File("newNasdaq.csv");


        for (ArrayList<String> burst: newBursts){
            for (String line: burst){
                try {
                    if(!file.exists()){
                        file.createNewFile();

                    }
                    FileWriter fileWriter = new FileWriter(file, true);
                    CSVWriter writer = new CSVWriter(fileWriter);
                    //write the data
                    writer.writeNext(line.split(","));
                    writer.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
