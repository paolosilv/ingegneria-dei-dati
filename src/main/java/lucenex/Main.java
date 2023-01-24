package lucenex;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        /* Indexing */
        // indexing("target/idxMain");

        /* Matching Process */
        executeMatching("target/idxMain");
    }



    private static void indexing(String pathIndex){
        Indexer indexer = new Indexer();
        Path path = Paths.get("target/idxMain");
        try (Directory directory = FSDirectory.open(path)) {
            indexer.indexDocs(directory, new SimpleTextCodec());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runQuery(IndexSearcher searcher, Query query, boolean explain) throws IOException {
        /* Print the first 6 ranked documents */
        TopDocs hits = searcher.search(query, 6);
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("doc "+scoreDoc.doc + ": "+ doc.get("nome") + " (" + scoreDoc.score +")");
            if (explain) {
                Explanation explanation = searcher.explain(query, scoreDoc.doc);
                System.out.println(explanation);
            }
        }
    }

    private static void executeMatching(String pathIndex) throws ParseException {
        System.out.println("Welcome in the Apache Lucene Exercise!\nPlease insert the field name (nome or contenuto) for the query: ");
        Scanner in = new Scanner(System.in);

        String fieldName = in.nextLine();
        if(!fieldName.equals("nome") && !fieldName.equals("contenuto")){
            System.out.println("incorrect field, use only : nome or contenuto");
            return;
        }
        System.out.println("Field name: " + fieldName);

        System.out.println("\nPlease insert the query content: ");
        String fieldContent = in.nextLine();
        System.out.println("Field content: " + fieldContent + "\n");

        long timeIndexing1 = System.currentTimeMillis();

        System.out.println("\nRelevant documents: \n");
        /* Two types of queries: QueryParser and PhraseQuery */
        Query query = null;

        /* Phrase Query defined by ' ' characters to delimit the content of the query */
        if (fieldContent.startsWith("'")) {
            fieldContent = fieldContent.substring(1, fieldContent.length() - 1);
            /* split words of the phrase */
            String[] components = fieldContent.split(" ");
            query = new PhraseQuery(fieldName, components);
        } else {
            /* Query Parser is the default query */
            QueryParser parser = new QueryParser(fieldName, new StandardAnalyzer());
            query = parser.parse(fieldContent);
        }

        Path path = Paths.get(pathIndex);
        try (Directory directory = FSDirectory.open(path)) {
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                searcher.setSimilarity(new ClassicSimilarity());
                runQuery(searcher, query, true);
            } finally {
                directory.close();
            }

            long timeIndexing2 = System.currentTimeMillis();
            System.out.println("Execution time for the entire process (indexing + query) in ms: " +(timeIndexing2-timeIndexing1) + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
