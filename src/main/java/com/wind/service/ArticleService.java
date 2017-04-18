package com.wind.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.wind.commons.Constant.ServiceMsg;
import com.wind.entity.Article;
import com.wind.utils.DocumentArticleTransfer;
import com.wind.utils.MongodbUtil;

@Service
public class ArticleService {
	@Resource
	MongodbUtil mongodbUtil;
	@Resource
	IdsService idsService;
	
	
	/**
	 * 获取连接
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:20:49
	 * @return
	 */
	public MongoCollection<Document> getColl() {
		return mongodbUtil.getMongoCollection("wind", "article");
	}
	//-----------------------------------------------------------
	/**
	 *  插入
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:20:25
	 * @param article
	 * @return
	 */
	public boolean add(Article article) {
		if(article==null) {
			return false;
		}
		
		//获取并插入自增主键id
		MongoCollection<Document> coll = getColl();
		long id = idsService.getNextIndex("article");

		if(id==0) {
			return false;
		}

		article.setId(id);
		
		//插入
		Document doc = DocumentArticleTransfer.article2Document(article);
		return mongodbUtil.insert(coll, doc);
	}

	/**
	 *  批量插入
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:20:25
	 * @param articleList
	 * @return
	 */
	public boolean batchAdd(List<Article> articleList) {

		if(articleList==null || articleList.size()==0) {
			return false;
		}
		MongoCollection<Document> coll = getColl();
		for(int i=0; i<articleList.size(); i++) {
			Article article = articleList.get(i);
			if(article!=null) {
				long id = idsService.getNextIndex("article");
				if(id==0) {
					return false;
				}
				article.setId(id);
			}
		}
		
 		List<Document> docList = DocumentArticleTransfer.article2Document(articleList);
		if(docList==null || docList.size()==0) {
			return false;
		}
		return mongodbUtil.batchInsert(coll, docList);
	}
	
	/**
	 * 更新
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:32:23
	 * @param id
	 * @param article
	 * @return
	 */
	public boolean updateById(long id, Article article) {
		if(id==0 || article==null) {
			return false;
		}
		MongoCollection<Document> coll = getColl();
		BsonDocument filter = new BsonDocument().append("id", new BsonInt64(id));
		Document document = DocumentArticleTransfer.article2Document(article);
		coll.findOneAndUpdate(filter, document);
		document = mongodbUtil.findOneAndReplace(coll, filter, document);
		return document!=null ? true : false;
	}
	
	//---------------------------- 查询数据 -----------------------------------

	/**
	 * 根据条件查询查询一条数据
	 * @param params
	 * @return
     */
	public Article findOne(Map<String, Object> params) {

		BsonDocument filter = new BsonDocument();
		if(params!=null && params.get("original_link")!=null) {
			filter.append("original_link", new BsonString(params.get("original_link").toString()));
		}
		Document document = mongodbUtil.findOne(getColl(), filter);
		if(document!=null) {
			return DocumentArticleTransfer.document2Article(document);
		}
		return null;
	}
	/**
	 * 查询
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:21:06
	 * @param params
	 * @return
	 */
	public List<Article> find(Map<String, Object> params, int start, int limit) {
		List<Article> articleList = null;

		BsonDocument filter = new BsonDocument();
		if(params!=null && params.get("original_link")!=null) {
			filter.append("original_link", new BsonString(params.get("original_link").toString()));
		}
		List<Document> docList = mongodbUtil.find(getColl(), filter, null, start, limit);
		if(docList!=null) {
			articleList = DocumentArticleTransfer.document2Article(docList);
		}
		return articleList;
	}
	/**
	 * 根据uids查询
	 * 
	 * @author qianchun  @date 2016年3月14日 下午2:53:59
	 * @param uidList
	 * @return
	 */
	public List<Article> findByUids(List<Long> uidList, int start, int limit) {
		Map<String, Object> params = new HashMap<String, Object>();
		//添加分页条件

		//添加查询条件
		BsonDocument filter = null;
		BsonArray bsonArray = new BsonArray();
		if(uidList!=null && uidList.size()>0) {
			for(int i=0; i<uidList.size(); i++) {
				long uid = uidList.get(i);
				if(uid!=0) {
					BsonInt64 bson = new BsonInt64(uid);
					bsonArray.add(bson);
				}
			} 
			if(bsonArray.size()>0) {
				BsonDocument in = new BsonDocument().append("$in", bsonArray);
				filter = new BsonDocument().append("uid", in);
			}
		}
		
		//查询
		MongoCollection<Document> coll = getColl();
		params.put("filter", filter);
		List<Document> docList = mongodbUtil.find(coll, filter, null, start, limit);
		List<Article> articleList = null;
		if(docList!=null) {
			articleList = DocumentArticleTransfer.document2Article(docList);
		}
		return articleList;
	}
	/**
	 * 根据id查询
	 * 
	 * @author qianchun  @date 2016年3月14日 下午2:55:03
	 * @param id
	 * @return
	 */
	public Article findById(long id) {
		Article article = null;
		MongoCollection<Document> coll = getColl();
		Document document = mongodbUtil.findById(coll, id);
		if(document!=null) {
			article = DocumentArticleTransfer.document2Article(document);
		}
		return article;
	}
	//-----------------------------------------------------------
}
