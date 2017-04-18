package com.wind.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.wind.commons.Constant;
import com.wind.main.LinkMain;
import org.apache.commons.lang.*;
import org.bson.*;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.wind.entity.Link;
import com.wind.utils.DocLinkTransfer;
import com.wind.utils.MongodbUtil;

@Service
public class LinkService {
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
        return mongodbUtil.getMongoCollection("wind", "link");
    }

    //-----------------------------------------------------------
    /**
     * 插入
     * 
     * @author qianchun @date 2016年3月3日 下午2:20:25
     * @param link
     * @return
     */
    public boolean add(Link link) {
        if (link == null) {
            return false;
        }

        //获取并插入自增主键id
        MongoCollection<Document> coll = getColl();
        long id = idsService.getNextIndex("link");
        if (id == 0) {
            return false;
        }
        link.setId(id);

        //插入
        Document doc = DocLinkTransfer.link2Document(link);
        return mongodbUtil.insert(coll, doc);
    }

    /**
     * 批量插入
     * 
     * @author qianchun @date 2016年3月3日 下午2:20:25
     * @param linkList
     * @return
     */
    public boolean batchAdd(List<Link> linkList) {

        if (linkList == null || linkList.size() == 0) {
            return false;
        }
        MongoCollection<Document> coll = getColl();
        for (int i = 0; i < linkList.size(); i++) {
            Link link = linkList.get(i);
            if (link != null) {
                long id = idsService.getNextIndex("link");
                if (id == 0) {
                    return false;
                }
                link.setId(id);
            }
        }

        List<Document> docList = DocLinkTransfer.link2Document(linkList);
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
     * @param link
     * @return
     */
    public boolean updateById(long id, Link link) {
        if (id == 0 || link == null) {
            return false;
        }
        MongoCollection<Document> coll = getColl();
        BsonDocument filter = new BsonDocument().append("id", new BsonInt64(id));
        Document document = DocLinkTransfer.link2Document(link);
        document = mongodbUtil.findOneAndReplace(coll, filter, document);
        return document != null ? true : false;
    }

    public boolean updateByIsParse(int oldStatus, int newStatus) {
        MongoCollection<Document> coll = getColl();
        BsonDocument filter = new BsonDocument().append("is_parse", new BsonInt32(oldStatus));
        Document document = new Document("$set", new Document("is_parse", newStatus));
        boolean flag = mongodbUtil.update(getColl(), filter, document);
        return document != null ? true : false;
    }

    //---------------------------- 查询数据 -----------------------------------

    /**
     * 根据ID查询
     * 
     * @param id
     * @return
     */
    public Link findById(long id) {
        Document doc = mongodbUtil.findById(getColl(), id);
        return DocLinkTransfer.document2Link(doc);
    }

    /**
     * 查询
     * 
     * @author qianchun @date 2016年3月3日 下午2:21:06
     * @param params
     * @return
     */

    /**
     * 根据is_parse和ur过滤,支持分页
     * 
     * @param params
     * @param start
     * @param limit
     * @return
     */
    public List<Link> findByMap(Map<String, Object> params, int start, int limit) {
        List<Link> linkList = null;
        MongoCollection<Document> coll = getColl();

		BsonDocument filter = new BsonDocument();

		if(params!=null && params.get("url")!=null) {
			String url = params.get("url").toString();
			filter.append("url", new BsonString(url));
		}

		if(params!=null && params.get("is_parse")!=null) {
			int isParse = Integer.parseInt(params.get("is_parse").toString());
			filter.append("is_parse", new BsonInt32(isParse));
		}

        List<Document> docList = mongodbUtil.find(coll, filter, null, start, limit);
        if (docList != null) {
            linkList = DocLinkTransfer.document2Link(docList);
        }
        return linkList;
    }

//    /**
//     * 根据is_parse和ur过滤,支持分页
//     *
//     * @param isParse
//     * @param start
//     * @param limit
//     * @return
//     */
//    public List<Link> findByIsParsel(int isParse, int start, int limit) {
//        List<Link> linkList = null;
//        MongoCollection<Document> coll = getColl();
//        BsonDocument filter = new BsonDocument().append("is_parse", new BsonInt32(isParse));
//        List<Document> docList = mongodbUtil.find(coll, filter, null, start, limit);
//        if (docList != null) {
//            linkList = DocLinkTransfer.document2Link(docList);
//        }
//        return linkList;
//    }

    /**
     * 统计
     * 
     * @author qianchun @date 2016年3月29日 下午5:48:54
     * @param params
     * @return
     */
    public long count(Map<String, Object> params) {
        List<Link> linkList = null;
        MongoCollection<Document> coll = getColl();

        BsonDocument filter = new BsonDocument();
        if (params != null && params.get("is_parse") != null) {
            filter.append("is_parse", new BsonInt32((int) params.get("is_parse")));
        }
        if (params != null && params.get("url") != null) {
            filter.append("url", new BsonString(params.get("url").toString()));
        }
        return mongodbUtil.count(coll, filter);
    }

    //-----------------------------------------------------------
    public  void insertIfNotExist(String linkUrl) {
		synchronized(LinkMain.allUrls) {
			Map<String, Object> paramsMap = new HashMap<>();
			paramsMap.clear();
			paramsMap.put("url", linkUrl);
			long count = this.count(paramsMap);
			if (count == 0) {
				Link tmpLink = new Link();
				tmpLink.setIsParse(Constant.LINK_IS_PARSE.NO);
				tmpLink.setFrom(Constant.ArticleFrom.CSDNBLOGS);
				tmpLink.setUrl(linkUrl);
				this.add(tmpLink);
			}
			if (count > 1) {
				System.out.println("有重复数据");
			}
		}
    }
}
