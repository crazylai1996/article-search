package com.search.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;

import com.search.entity.Article;
import com.search.mapper.ArticleMapper;

public class LuceneUtil {
	private static final String INDEXPATH="D:\\lucene";
	private static RAMDirectory ramDirectory;
	private static IndexWriter ramWriter;
	
	@Autowired
	private ArticleMapper articleMapper;

	static{
		try {
			FSDirectory fsDirectory = FSDirectory.open(Paths.get(INDEXPATH));
			ramDirectory = new RAMDirectory(fsDirectory,IOContext.READONCE);
			fsDirectory.close();
			
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
					new SmartChineseAnalyzer());//中文分词器
			indexWriterConfig.setIndexDeletionPolicy(
					new SnapshotDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy()));
			
			ramWriter = new IndexWriter(ramDirectory, indexWriterConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//于磁盘创建索引
	public void reCreatIndex(){
		try {
			Path path = Paths.get(INDEXPATH);
			//删除原有索引文件
			for (File file : path.toFile().listFiles()) {
				file.delete();
			}
			FSDirectory fsDirectory = FSDirectory.open(path);
			Analyzer analyzer = new SmartChineseAnalyzer();//中文分词器
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
			IndexWriter writer = new IndexWriter(fsDirectory, indexWriterConfig);
			List<Article> articles = articleMapper.findAll();
			for (Article article : articles) {
				writer.addDocument(toDocument(article));
			}
			writer.close();
			System.out.println("-----创建索引成功---");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//实体article对象转document索引对象
	public Document toDocument(Article article){
		Document doc = new Document();
		doc.add(new StringField("id",String.valueOf(article.getId()),Field.Store.YES));
		doc.add(new Field("title", article.getTitle(), TextField.TYPE_STORED));
		doc.add(new Field("details", article.getDetails(),TextField.TYPE_STORED));
		
		return doc;
	}
	
	//添加索引 
	public synchronized void addDocument(Article article) throws IOException{
		ramWriter.addDocument(toDocument(article));
		ramWriter.commit();
	}
	
	//删除索引
	public synchronized void deleteDocument(Long id) throws IOException{
		Term term = new Term("id",String.valueOf(id));
		ramWriter.deleteDocuments(term);
		ramWriter.commit();
	}
	
	//搜索索引 
	public List<Article> search(String keyword) throws IOException, ParseException, InvalidTokenOffsetsException{
		List<Article> list = new ArrayList<Article>();
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(ramDirectory));
		String [] fields = {"title","details"};
		Analyzer analyzer = new SmartChineseAnalyzer();
		QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
		Query query = queryParser.parse(keyword);
//		BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
//		Query query = MultiFieldQueryParser.parse(keyword, fields, clauses, analyzer);
		TopDocs hits = indexSearcher.search(query, 20);
		
		//高亮
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
		
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Article article = new Article();
			Document doc = indexSearcher.doc(scoreDoc.doc);
			article.setId(Long.valueOf(doc.get("id")));
			String title = doc.get("title");
			String details = doc.get("details");
			article.setTitle(highlighter.getBestFragment(analyzer.tokenStream("title", new StringReader(title)), title));
			article.setDetails(highlighter.getBestFragment(analyzer.tokenStream("details", new StringReader(details)), details));
			list.add(article);
		}
		
		return list;
	}
	
	//更新索引 
	public void updateDocument(Article article) throws IOException{
		Term term = new Term("id",String.valueOf(article.getId()));
		ramWriter.updateDocument(term, toDocument(article));
		ramWriter.commit();
	}
	
	//同步索引至磁盘
	public void indexSync(){
		IndexWriterConfig config = null;
		SnapshotDeletionPolicy snapshotDeletionPolicy = null;
		IndexCommit indexCommit = null;
		
		try {
			config = (IndexWriterConfig) ramWriter.getConfig();
			snapshotDeletionPolicy = (SnapshotDeletionPolicy) config.getIndexDeletionPolicy();
			indexCommit = snapshotDeletionPolicy.snapshot();
			config.setIndexCommit(indexCommit);
			Collection<String> fileNames = indexCommit.getFileNames();
			Path toPath = Paths.get(INDEXPATH);
			Directory toDir = FSDirectory.open(toPath);
			//删除所有原有索引文件
			for (File file : toPath.toFile().listFiles()) {
				file.delete();
			}
			//从ramdir复制新索引文件至磁盘
			for (String fileName : fileNames) {
				toDir.copyFrom(ramDirectory, fileName, fileName, IOContext.DEFAULT);
			}
			toDir.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("-----索引同步完成------");
	}
}
