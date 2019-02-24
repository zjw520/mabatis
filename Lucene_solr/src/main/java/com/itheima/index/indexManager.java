package com.itheima.index;

import com.itheima.dao.Impl.BookDaoImpl;
import com.itheima.domel.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class indexManager {
    @Test
    public void saveIndex() throws IOException {
        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        Directory directory = FSDirectory.open(new File("F:\\index"));
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        Term term = new Term("bookName","java");
        indexWriter.deleteAll();
        indexWriter.commit();
        indexWriter.close();
    }

    @Test
    public void IndexFindAll() throws IOException {
        //采集数据
        BookDaoImpl bookDao = new BookDaoImpl();
        List<Book> bookList = bookDao.findAll();
        //创建文档集合
        List<Document> documents = new ArrayList<>();
        for (Book book : bookList) {
            //创建文档对象
            Document doc = new Document();
            doc.add(new StringField("id",book.getId()+"", Field.Store.YES));
            doc.add(new TextField("bookName",book.getBookName(), Field.Store.YES));
            doc.add(new DoubleField("bookPrice",book.getPrice(), Field.Store.YES));
            doc.add(new StringField("bookPic",book.getPic(), Field.Store.YES));
            doc.add(new TextField("bookDesc",book.getBookDesc(), Field.Store.YES));
            documents.add(doc);
        }
        //创建分词器
        Analyzer analyzer = new IKAnalyzer();
        //创建索引库配置对象,用于配置索引库
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        //设置索引库打开方式
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        //创建索引库目录对象, 用于指定索引库储存位置
        Directory directory = FSDirectory.open(new File("F:\\index"));
        //创建索引库操作对象,用于把文档写入索引库
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        for (Document document : documents) {
            indexWriter.addDocument(document);
            indexWriter.commit();
        }
        indexWriter.close();

    }

    @Test
    public void insertIndex() throws IOException {
        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        Directory directory = FSDirectory.open(new File("F:\\index"));
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        Document document = new Document();
        document.add(new StringField("id","9688", Field.Store.YES));
        document.add(new TextField("name","lucene solr dubbo zookeeper", Field.Store.YES));
        Term term = new Term("name","f");
        indexWriter.updateDocument(term,document);
        indexWriter.commit();
        indexWriter.close();
    }

    public void search(org.apache.lucene.search.Query query) throws IOException {
        System.out.println("查询语法"+query);

        Directory directory = FSDirectory.open(new File("F:\\index"));
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        TopDocs topDocs = indexSearcher.search(query,10);
        System.out.println("总命中的记录数" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            System.out.println("================");
            System.out.println("文档id: " + scoreDoc.doc
                    + "\t文档分值：" + scoreDoc.score);
            // 根据文档id获取指定的文档
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("图书Id：" + doc.get("id"));
            System.out.println("图书名称：" + doc.get("bookName"));
            System.out.println("图书价格：" + doc.get("bookPrice"));
            System.out.println("图书图片：" + doc.get("bookPic"));
            System.out.println("图书描述：" + doc.get("bookDesc"));
        }
        indexReader.close();
    }

    @Test
    public void testTermQuery() throws IOException {
        Term term = new Term("bookName","java");
        TermQuery termQuery = new TermQuery(term);
        search(termQuery);
    }
    @Test
    public void testNumericRangeQuery() throws IOException {
        Query q = NumericRangeQuery.newDoubleRange("bookPrice",80d ,100d ,true ,true);
        search(q);
    }





}
