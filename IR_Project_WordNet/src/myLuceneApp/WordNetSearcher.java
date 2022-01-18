package myLuceneApp;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilterFactory;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.synonym.SynonymGraphFilterFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import txtparsing.MyQuery;
import utils.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class WordNetSearcher {

    public WordNetSearcher() {
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
            CustomAnalyzer query_analyzer = customAnalyzerForQueryExpansion();

            // create a query parser on the field "contents"
            QueryParser parser = new QueryParser(field, query_analyzer);

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
                    file.write(queries.getQueryId().trim() + " 0 " + hitDoc.get("title") + " 0 " + hits[i].score + " IR_Project_WordNetLucene\n");
                }
            }
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize a WordNetLucene instance
     */
    public static void main(String[] args) {
        WordNetSearcher WordNetSearcher = new WordNetSearcher();
    }

    private static CustomAnalyzer customAnalyzerForQueryExpansion() throws IOException {
        //Read synonyms from wn_s.pl file
        Map<String, String> sffargs = new HashMap<>();
        sffargs.put("synonyms", "wn_s_no_verbs.pl");
        sffargs.put("format", "wordnet");

        //    Create custom analyzer for analyzing query text.
        //    Custom analyzer should analyze query text like the EnglishAnalyzer and have
        //    an extra filter for finding the synonyms of each token from the Map sffargs
        //    and add them to the query.
        CustomAnalyzer.Builder builder = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(SynonymGraphFilterFactory.class, sffargs)
                .addTokenFilter(EnglishPossessiveFilterFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .addTokenFilter(PorterStemFilterFactory.class);
        CustomAnalyzer analyzer = builder.build();
        return analyzer;
    }

}
