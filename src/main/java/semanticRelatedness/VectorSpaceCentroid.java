package semanticRelatedness;

import distanceMetrics.DistanceMetric;
import distanceMetrics.Norm;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class VectorSpaceCentroid {

    QueryParser parser;
    IndexReader reader;
    IndexSearcher searcher;
    String field = "contents";
    DefaultSimilarity similarity;

    double[] centroid;
    int totalDocs;
    double scoreSumLog;
    public double vectorLength;

    public double[] getCentroid() {
        return centroid;
    }

    public double numDocs() {
        return totalDocs;
    }

    public double getScoreSumLog() {
        return scoreSumLog;
    }

    public double getVectorLength() {
        return Math.sqrt(vectorLength);
    }

    VectorSpaceCentroid(String luceneIndexPath, String text, Norm norm) throws IOException, ParseException {
        reader = DirectoryReader.open(FSDirectory.open(new File(luceneIndexPath)));
        Analyzer analyzer = new DutchAnalyzer(Version.LUCENE_43);
        parser = new QueryParser(Version.LUCENE_43, field, analyzer);
        similarity = new DefaultSimilarity();
        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);
        buildCentroid(text, norm);
    }

    public VectorSpaceCentroid(IndexReader reader2, IndexSearcher searcher2,
            DefaultSimilarity similarity2, QueryParser parser2, String text, Norm norm) throws ParseException, IOException {
        reader = reader2;
        parser = parser2;
        similarity = similarity2;
        searcher = searcher2;
        buildCentroid(text, norm);

    }

    VectorSpaceCentroid(double[] centroid) {
        totalDocs = centroid.length;
        this.centroid = centroid;
    }

    public int json(BufferedWriter bw) throws IOException {
        bw.write("{\"totalDocs\":" + totalDocs);
        bw.write(",\"scores\":[");

        for (int j = 0; j < totalDocs; j++) {
            if (j != 0) {
                bw.write(",");
            }
            if (centroid[j] == 0.0) {
                bw.write("0");
            } else {
                bw.write(String.format("%.17f", centroid[j]).replace(",", "."));
            }

        }
        bw.write("]}");
        return totalDocs;
    }

    @Override
    public String toString() {
        return Arrays.toString(centroid);
    }

    public void buildCentroid(String text, Norm norm) throws ParseException, IOException {
        HashMap<String, Integer> parsedTokensCount = new HashMap<String, Integer>();

        double maxScore = 0;

        TokenStream ts = parser.getAnalyzer().tokenStream(field, new StringReader(text));
        CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
        ts.reset();

        while (ts.incrementToken()) {
            String token = termAttr.toString();
            if (parsedTokensCount.containsKey(token)) {
                parsedTokensCount.put(token, parsedTokensCount.get(token) + 1);
            } else {
                parsedTokensCount.put(token, 1);
            }
        }
        ts.end();
        ts.close();

        totalDocs = reader.numDocs();
        centroid = new double[totalDocs];
        double scoreSum = 0;

        HashMap<String, TopDocs> resultsCache = new HashMap<String, TopDocs>();
        for (String parsedToken : parsedTokensCount.keySet()) {
            int termFreq = parsedTokensCount.get(parsedToken);
            Term term = new Term(field, parsedToken);
            int docFreq = reader.docFreq(term);
            if (docFreq == 0) {
                continue;
            }
            double idf = similarity.idf(docFreq, totalDocs);
            double tfidf = (similarity.tf(termFreq) * Math.pow(idf, 2));
            TopDocs results;
            if (resultsCache.containsKey(parsedToken)) {
                results = resultsCache.get(parsedToken);
            } else {
                results = searcher.search(new TermQuery(term, docFreq), docFreq);
                resultsCache.put(parsedToken, results);
            }

            if (results.totalHits < 1) {
                continue;
            }
            double inc;
            for (ScoreDoc doc : results.scoreDocs) {
                inc = (tfidf * doc.score);
                centroid[doc.doc] += inc;
                scoreSum += inc;
                maxScore = Math.max(centroid[doc.doc], maxScore);
            }
        }
        switch(norm) {
                case SUM_NORM :
                    sumNorm(scoreSum);
                    break;
                case MAX_NORM :
                    maxNorm(maxScore);
                    break;
                case NO_NORM :
                    noNorm(scoreSum);
                    break;
                case UNIT_NORM :
                    unitNorm(scoreSum);
                    break;
        }
    }

    
    private void sumNorm(double scoreSum) {
        if (scoreSum != 0) {
            // USE SUM NORM
            for (int i = 0; i < centroid.length; i++) {
                double score = centroid[i] / scoreSum;
                centroid[i] = score;
                vectorLength += score * score;
            }
            scoreSumLog = 0;    // Math.log(1) = 0
        }
    }

    private void noNorm(double scoreSum) {
        for (int i = 0; i < centroid.length; i++) {
            double score = centroid[i];
            vectorLength += score * score;
        }
        if(scoreSum != 0) {
            scoreSumLog = Math.log(scoreSum);
        }
    }

    private void maxNorm(double maxScore) {
        if(maxScore != 0) {
            double sum = 0.0;
            for (int i = 0; i < centroid.length; i++) {
                double score = centroid[i] / maxScore;
                sum += score;
                centroid[i] = score;
                vectorLength += score * score;
            }
            scoreSumLog = Math.log(sum);
        }
    }

    private void unitNorm(double scoreSum) {
        if (scoreSum != 0) {
            double squareSum = 0;
            for (int i = 0; i < centroid.length; i++) {
                squareSum += centroid[i]*centroid[i];
            }
            squareSum = Math.sqrt(squareSum);
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] = centroid[i] / squareSum;
                scoreSum += centroid[i];
            }
            vectorLength = 1; // here vector length is really 1
            scoreSumLog = Math.log(scoreSum);
        }
    }
}
