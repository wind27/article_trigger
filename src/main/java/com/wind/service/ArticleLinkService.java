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
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.wind.commons.Constant.MongoName;
import com.wind.commons.Constant.ServiceMsg;
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
     * @author qianchun @date 2016年3月3日 下午2:20:49
     * @return
     */
    public MongoCollection<Document> getColl() {
        return mongodbUtil.getMongoCollection(MongoName.DATABASE_WIND,
                MongoName.COLLECTION_ARTICLE_LINK);
    }

    //-----------------------------------------------------------
    /**
     * 插入
     * 
     * @author qianchun @date 2016年3月3日 下午2:20:25
     * @param articleLink
     * @return
     */
    public boolean add(ArticleLink articleLink) {
        if (articleLink == null) {
            return false;
        }

        //获取并插入自增主键id
        MongoCollection<Document> coll = getColl();
        long id = idsService.getNextIndex(MongoName.COLLECTION_ARTICLE_LINK);
        if (id == 0) {
            return false;
        }
        articleLink.setId(id);

        //插入
        Document doc = DocArticleLinkTransfer.articleLink2Document(articleLink);
        return mongodbUtil.insert(coll, doc);
    }

    /**
     * 批量插入
     * 
     * @author qianchun @date 2016年3月3日 下午2:20:25
     * @param articleLinkList
     * @return
     */
    public boolean batchAdd(List<ArticleLink> articleLinkList) {
        if (articleLinkList == null || articleLinkList.size() == 0) {
            return false;
        }
        MongoCollection<Document> coll = getColl();
        for (int i = 0; i < articleLinkList.size(); i++) {
            ArticleLink articleLink = articleLinkList.get(i);
            if (articleLink != null) {
                long id = idsService.getNextIndex(MongoName.COLLECTION_ARTICLE_LINK);
                if (id == 0) {
                    return false;
                }
                articleLink.setId(id);
            }
        }

        List<Document> docList = DocArticleLinkTransfer.articleLink2Document(articleLinkList);
        if (docList == null || docList.size() == 0) {
            return false;
        }
        return mongodbUtil.batchInsert(coll, docList);
    }

    /**
     * 更新
     * 
     * @author qianchun @date 2016年3月3日 下午2:32:23
     * @param id
     * @param articleLink
     * @return
     */
    public boolean updateById(long id, ArticleLink articleLink) {
        if (id == 0 || articleLink == null) {
            return false;
        }
        MongoCollection<Document> coll = getColl();
        BsonDocument filter = new BsonDocument().append("id", new BsonInt64(id));
        Document document = DocArticleLinkTransfer.articleLink2Document(articleLink);
        coll.findOneAndUpdate(filter, document);
        document = mongodbUtil.findOneAndReplace(coll, filter, document);
        return document != null ? true : false;
    }

    //---------------------------- 查询数据 -----------------------------------
    /**
     * 查询
     * 
     * @author qianchun @date 2016年3月3日 下午2:21:06
     * @param params
     * @return
     */
    public List<ArticleLink> find(Map<String, Object> params, int start, int limit) {
        List<ArticleLink> linkList = null;
        MongoCollection<Document> coll = getColl();

        BsonDocument filter = new BsonDocument();

        if (params != null && params.get("is_parse") != null) {
            filter.append("is_parse", new BsonInt32((int) params.get("is_parse")));
        }

        if (params != null && params.get("url") != null) {
            filter.append("url", new BsonString(params.get("url").toString()));
        }

        List<Document> docList = mongodbUtil.find(coll, filter, null, start, limit);
        if (docList != null) {
            linkList = DocArticleLinkTransfer.document2ArticleLink(docList);
        }
        return linkList;
    }

    /**
     * 根据条件查询符合条件的一个
     * @param params
     * @return
     */
    public ArticleLink findOne(Map<String, Object> params) {
        BsonDocument filter = new BsonDocument();

        if (params != null && params.get("is_parse") != null) {
            filter.append("is_parse", new BsonInt32((int) params.get("is_parse")));
        }

        if (params != null && params.get("url") != null) {
            filter.append("url", new BsonString(params.get("url").toString()));
        }

        Document document = mongodbUtil.findOne(getColl(), filter);
        return DocArticleLinkTransfer.document2ArticleLink(document);
    }
    //-----------------------------------------------------------
}
