package com.wind.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.wind.commons.Constant.MongoName;
import com.wind.commons.Constant.ServiceMsg;
import com.wind.commons.ServiceResult;
import com.wind.entity.ArticleLink;
import com.wind.utils.DocArticleLinkTransfer;
import com.wind.utils.MongodbUtil;

@Service
public class ArticleLinkService {
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
		return mongodbUtil.getMongoCollection(MongoName.DATABASE_WIND, MongoName.COLLECTION_ARTICLE_LINK);
	}
	//-----------------------------------------------------------
	/**
	 *  插入
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:20:25
	 * @param article
	 * @return
	 */
	public ServiceResult<ArticleLink> add(ArticleLink articleLink) {
		ServiceResult<ArticleLink> result = new ServiceResult<ArticleLink>();
		if(articleLink==null) {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.PARAMS_ERROR);
			return result;
		}
		
		//获取并插入自增主键id
		MongoCollection<Document> coll = getColl();
		long id = idsService.getNextIndex(MongoName.COLLECTION_ARTICLE_LINK);
		if(id==0) {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.ID_INCREMENT_ERROR);
			return result;
		}
		articleLink.setId(id);
		
		//插入
		Document doc = DocArticleLinkTransfer.articleLink2Document(articleLink);
		boolean flag = mongodbUtil.insert(coll, doc);
		if(flag) {
			result.setSuccess(true);
			return result;
		} else {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.FAIL);
			return result;
		}
	}

	/**
	 *  批量插入
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:20:25
	 * @param article
	 * @return
	 */
	public ServiceResult<ArticleLink> batchAdd(List<ArticleLink> articleLinkList) {
		ServiceResult<ArticleLink> result = new ServiceResult<ArticleLink>();
		
		if(articleLinkList==null || articleLinkList.size()==0) {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.PARAMS_ERROR);
			return result;
		}
		MongoCollection<Document> coll = getColl();
		for(int i=0; i<articleLinkList.size(); i++) {
			ArticleLink articleLink = articleLinkList.get(i);
			if(articleLink!=null) {
				long id = idsService.getNextIndex(MongoName.COLLECTION_ARTICLE_LINK);
				if(id==0) {
					result.setSuccess(false);
					result.setMsg(ServiceMsg.ID_INCREMENT_ERROR);
					return result;
				}
				articleLink.setId(id);
			}
		}
		
 		List<Document> docList = DocArticleLinkTransfer.articleLink2Document(articleLinkList);
		if(docList==null || docList.size()==0) {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.PARAMS_ERROR);
			return result;
		}
		boolean flag = mongodbUtil.batchInsert(coll, docList);
		if(flag) {
			result.setSuccess(true);
			result.setMsg(ServiceMsg.SUCCESS);
		} else {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.FAIL);
		}
		return result;
	}
	
	/**
	 * 更新
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:32:23
	 * @param params
	 * @return
	 */
	public ServiceResult<ArticleLink> updateById(long id, ArticleLink articleLink) {
		ServiceResult<ArticleLink> result = new ServiceResult<ArticleLink>();
		if(id==0 || articleLink==null) {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.PARAMS_ERROR);
			return result;
		}
		MongoCollection<Document> coll = getColl();
		BsonDocument filter = new BsonDocument().append("id", new BsonInt64(id));
		Document document = DocArticleLinkTransfer.articleLink2Document(articleLink);
		coll.findOneAndUpdate(filter, document);
		document = mongodbUtil.findOneAndReplace(coll, filter, document);
		if(document==null) {
			result.setSuccess(false);
			result.setMsg(ServiceMsg.SUCCESS);
		} else {
			result.setSuccess(true);
			result.setMsg(ServiceMsg.FAIL);
		}
		return result;
	}
	
	//---------------------------- 查询数据 -----------------------------------
	/**
	 * 查询
	 * 
	 * @author qianchun  @date 2016年3月3日 下午2:21:06
	 * @param params
	 * @return
	 */
	public ServiceResult<ArticleLink> find(Map<String, Object> params) {
		ServiceResult<ArticleLink> result = new ServiceResult<ArticleLink>();
		List<ArticleLink> linkList = null;
		MongoCollection<Document> coll = getColl();
		Map<String, Object> filterParams = new HashMap<>();

		if(params!=null && params.get("is_parse")!=null) {
			BsonDocument filter = new BsonDocument().append("is_parse", 
					new BsonInt32((int) params.get("is_parse")));
			filterParams.put("filter", filter);
		}
		if(params!=null && params.get("url")!=null) {
			BsonDocument filter = new BsonDocument().append("url", 
					new BsonString(params.get("url").toString()));
			filterParams.put("filter", filter);
		}
		if(params!=null && params.get("pstart")!=null && params.get("plimit")!=null) {
			filterParams.put("pstart", params.get("pstart"));
			filterParams.put("plimit", params.get("plimit"));
		}
		List<Document> docList = mongodbUtil.find(coll, filterParams);
		if(docList!=null) {
			linkList = DocArticleLinkTransfer.document2ArticleLink(docList);
		}
		result.setSuccess(true);
		result.setList(linkList);
		return result;
	}
	//-----------------------------------------------------------
}
