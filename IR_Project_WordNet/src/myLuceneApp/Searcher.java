package myLuceneApp;

// tested for lucene 7.7.2 and jdk13

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import txtparsing.*;
import utils.IO;

/**
 * @author Tonia Kyriakopoulou modified by Dimitrios Stefanou
 */
public class Searcher {

    public Searcher() {
        try {
            String indexLocation = ("index"); //define where the index is stored
            String field = "contents"; //define which field will be searched            

            //Access the index using indexReaderFSDirectory.open(Paths.get(index))
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation))); //IndexReader is an abstract class, providing an interface for accessing an index.
            IndexSearcher indexSearcher = new IndexSearcher(indexReader); //Creates a searcher searching the provided index, Implements search over a single IndexReader.
            indexSearcher.setSimilarity(new BM25Similarity());

            //Search the index using indexSearcher
            search(indexSearcher, field);

            //Close indexReader
            indexReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches the index given a specific user query.
     */
    private void search(IndexSearcher indexSearcher, String field) {
        try {
            // define which analyzer to use for the normalization of user's query
            Analyzer analyzer = new EnglishAnalyzer();

            // create a query parser on the field "contents"
            QueryParser parser = new QueryParser(field, analyzer);

            // read user's query from stdin
            //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Reading queries from file...");
            String txt_file = IO.ReadEntireFileIntoAString("docs//queries.txt");
            String[] query_text = txt_file.split("\n");

            List<MyQuery> parsed_queries = new ArrayList<>();
            for (int i = 0; i < query_text.length; i = i + 3) {
                MyQuery myquery = new MyQuery("Q" + "000".substring(query_text[i].length()) + query_text[i], query_text[i + 1]);
                parsed_queries.add(myquery);
            }

            Scanner scanner = new Scanner(System.in);
            int k = 0;
            try {
                System.out.println("Specify number of k best results:");
                k = scanner.nextInt();
                if (k < 0) throw new IllegalArgumentException("Only positive numbers allowed!");
            } catch (InputMismatchException e) {
                System.err.println("Number not positive integer!");
                System.exit(-1);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            } finally {
                scanner.close();
            }

            FileWriter file = new FileWriter("my_resultsK" + k + ".txt");
            for (MyQuery queries : parsed_queries) {
                Query query = parser.parse(queries.getQueryText());
                System.out.println("Searching for: " + query.toString(field));

                // search the index using the indexSearcher
                TopDocs results = indexSearcher.search(query, k);
                ScoreDoc[] hits = results.scoreDocs;
                long numTotalHits = results.totalHits;
                System.out.println(numTotalHits + " total matching documents");

                //display results
                for (int i = 0; i < hits.length; i++) {
                    Document hitDoc = indexSearcher.doc(hits[i].doc);
                    System.out.println("\tScore " + hits[i].score + "\ttitle=" + hitDoc.get("title") + "\tcaption:" + hitDoc.get("caption") + "\tmesh:" + hitDoc.get("mesh"));
                    file.write(queries.getQueryId().trim() + " 0 " + hitDoc.get("title") + " 0 " + hits[i].score + " IR_Project_Baseline\n");
                }
            }
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize a Searcher
     */
    public static void main(String[] args) {
        Searcher searcher = new Searcher();
    }
}
