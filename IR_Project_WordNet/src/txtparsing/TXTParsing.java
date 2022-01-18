package txtparsing;

import org.apache.lucene.util.ArrayUtil;
import utils.IO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tonia Kyriakopoulou
 * modified by Dimitrios Stefanou
 */
public class TXTParsing {

    public static List<MyDoc> parse(String file) throws Exception {
        try {
            //Parse txt file
            String txt_file = IO.ReadEntireFileIntoAString(file);
            String[] docs = txt_file.split("///");
            System.out.println("Read: " + docs.length + " docs");

            //Parse each document from the txt file
            List<MyDoc> parsed_docs = new ArrayList<MyDoc>();
            for (String doc : docs) {
                String[] adoc = doc.split("[\n:]");
                StringBuilder meshText = new StringBuilder();
                /* int = 0 empty element, unused
                *  int = 1 title element
                *  int = 2 caption element
                *  rest of elements are mesh parts, to be merged
                */
                for (int i = 3; i < adoc.length; i++) {
                    meshText.append(adoc[i]);
                }
                MyDoc mydoc = new MyDoc(adoc[1], adoc[2], meshText.toString());
                parsed_docs.add(mydoc);
            }

            return parsed_docs;
        } catch (Throwable err) {
            err.printStackTrace();
            return null;
        }

    }

}
