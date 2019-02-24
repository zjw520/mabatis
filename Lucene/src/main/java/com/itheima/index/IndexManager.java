package com.itheima.index;


import com.itheima.dao.Impl.BookDao;
import com.itheima.domel.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IndexManager {

    @Test
    public void createIndex() throws IOException {
        BookDao bookDao = new BookDao();
        List<Book> bookList = bookDao.findAll();
        List<Document> documents = new ArrayList<>();
        for (Book book : bookList) {
            Document doc = new Document();
            doc.add(new StringField("id",book.getId()+"", Field.Store.YES));
            doc.add(new TextField("bookName",book.getBookName(), Field.Store.YES));
            doc.add(new DoubleField("bookPrice",book.getPrice(), Field.Store.YES));
            doc.add(new StoredField("bookPic",book.getPic()));
            doc.add(new TextField("bookDesc",book.getBookDesc(), Field.Store.NO));
            documents.add(doc);
        }
        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        Directory directory = FSDirectory.open(new File("F:\\index"));
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        for (Document document : documents) {
            indexWriter.addDocument(document);
            indexWriter.commit();
        }
        indexWriter.close();
    }

    @Test
    public void searchIndex() throws ParseException, IOException {
        Analyzer analyzer = new IKAnalyzer();
        QueryParser queryParser = new QueryParser("bookName", analyzer);
        Query query = queryParser.parse("bookName:java");
        Directory directory = FSDirectory.open(new File("F:\\index"));
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("总选中的记录数" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            System.out.println("=================");
            System.out.println("文档id: " + scoreDoc.doc + "\t文档分值: " +
                    scoreDoc.score);
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("图书id: " + doc.get("id"));
            System.out.println("图书名称: " + doc.get("bookName"));
            System.out.println("图书价格: " + doc.get("bookPrice"));
            System.out.println("图书图片: " + doc.get("bookPic"));
            System.out.println("图书描述: " + doc.get("bookDesc"));
        }
        indexReader.close();
    }
}
