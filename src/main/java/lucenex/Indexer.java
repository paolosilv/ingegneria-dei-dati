package lucenex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Indexer {

    protected File[] listFiles(String path){
        File folder = new File(path);
        if(folder.isDirectory()) {
            return folder.listFiles();
        }
        return null;
    }

    protected void indexDocs(Directory directory, Codec codec) throws IOException {
        long timeIndexing1 = System.currentTimeMillis();

        Analyzer defaultAnalyzer = new StandardAnalyzer();
        CharArraySet stopWords = new CharArraySet(Arrays.asList("in", "dei", "di", "della", "e", "delle"), true);
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put("nome", new StandardAnalyzer());
        perFieldAnalyzers.put("contenuto", new StandardAnalyzer(stopWords));

        Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        if (codec != null) {
            config.setCodec(codec);
        }
        IndexWriter writer = new IndexWriter(directory, config);
        writer.deleteAll();
        File[] fileList = listFiles("src\\main\\resources\\txtfiles");
        if (fileList != null) {
            String queryContent = "";
            for (File file : fileList) {
                String fileName = file.getName();
                fileName = fileName.replaceFirst(".txt", "");
                FileReader fileReader = new FileReader(file);
                char[] fileContent = new char[4096];
                int fileSize = fileReader.read(fileContent);
                for (int i = 0; i < fileSize; i++) {
                    queryContent = queryContent + fileContent[i];
                }
                Document doc = new Document();
                doc.add(new TextField("nome", fileName, Field.Store.YES));
                doc.add(new TextField("contenuto", queryContent, Field.Store.YES));

                writer.addDocument(doc);

                queryContent = "";
            }
            writer.commit();
            writer.close();
            long timeIndexing2 = System.currentTimeMillis();
            System.out.println("Indexing time in ms: " + (timeIndexing2 - timeIndexing1) + "\n");
        }
    }
}
