package myLuceneApp;

/**
 * @author Dimitrios Stefanou
 */

import java.io.*;
import java.util.Scanner;

public class WordNetVerbDelete {

    public static void main(String[] args) {
        try {
            File oldwn = new File("src/wn_s.pl");
            Scanner myReader = new Scanner(oldwn);
            FileWriter myWriter = new FileWriter("src/wn_s_no_verbs.pl");
            // Create new wordnet file, without lines containing "',v", meaning verbs.
            // ex. s(100002684,1,'object',n,1,51) is not a verb, added.
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (!data.contains("',v"))
                    myWriter.write(data + "\n");
            }
            myReader.close();
            myWriter.close();
            System.out.println("Deletion complete!");
        } catch (FileNotFoundException e) {
            System.out.println("File not in correct folder! Make sure wn_s.pl is in src!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error during read/write!");
            e.printStackTrace();
        }
    }
}
