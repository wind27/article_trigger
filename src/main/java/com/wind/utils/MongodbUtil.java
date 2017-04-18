package com.wind.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import net.sf.json.JSONObject;

@Service
public class MongodbUtil {
    private final static Logger logger = LoggerFactory.getLogger(MongodbUtil.class);
    
    @Resource
    private MongoClient mongoClient;
    
    /**
     * 获取 database 连接
     * 
     * @author qianchun  @date 2016年3月3日 下午2:23:06
     * @param databaseName
     * @param collection
     * @return
     */
    public MongoCollection<Document> getMongoCollection(String databaseName, String collection) {
    	MongoDatabase db = null;
    	MongoCollection<Document> coll = null;
    	try {
    		db = mongoClient.getDatabase(databaseName);
    		if(db!=null) {
    			coll = db.getCollection(collection);
    		}
    		if(coll==null) {
    			logger.error("获取 mongodb 连接失败");
    		}
		} catch (Exception e) {
			if(mongoClient!=null) {
				mongoClient.close();
			}
			logger.error("连接 mongodb 失败！！！", e);
		}
    	return coll;
    }
    //---------------------------- 获取数据 -----------------------------------

	public Document findOne(MongoCollection<Document> coll, Bson filter) {
		if(coll==null) {
			return null;
		}
		try {
			FindIterable<Document> iterator = null;
			if(filter!=null) {
                iterator = coll.find(filter);
            } else {
                iterator = coll.find();
            }
			MongoCursor<Document> cursor = iterator.iterator();
			if(cursor!=null && cursor.hasNext()) {
                return cursor.next();
            }
		} catch (Exception e) {
			logger.error("查询异常", e);
			return null;
		}
		return null;
	}
	/**
	 * 根据条件查询
	 * @param coll
	 * @param filter
	 * @param sort
	 * @param start
	 * @param limit
     * @return
     */
    public List<Document> find(MongoCollection<Document> coll, Bson filter, Bson sort, int start, int limit) {
    	if(coll==null) {
    		return null;
    	}
    	try {
    		List<Document> documentList = new ArrayList<Document>();

			FindIterable<Document> iterator = null;
			if(filter!=null) {
				iterator = coll.find(filter);
			} else {
				iterator = coll.find();
			}

			if(start>=0 && limit > 0) {
				iterator.skip(start).limit(limit);
			}

			if(sort!=null) {
				iterator.sort(sort);
			}

    		MongoCursor<Document> cursor = iterator.iterator();
    		if(cursor!=null) {
    			while (cursor.hasNext()) {
    				Document doc = cursor.next();
    				if(doc!=null) {
    					documentList.add(doc);
    				}
    			}
    		}
    		return documentList;
		} catch (Exception e) {
			logger.error("查询异常", e);
			return null;
		}
    }

	/**
	 * 根据ID查询
	 * @param coll
	 * @param id
     * @return
     */
    public Document findById(MongoCollection<Document> coll, long id) {
    	if(coll==null) {
    		return null;
    	}
    	try {
    		Document doc = null;
    		FindIterable<Document> iterator = null;
			BsonDocument filter = new BsonDocument().append("id", new BsonInt32(1));
			iterator = coll.find(filter).skip(0).limit(1);
    		MongoCursor<Document> cursor = iterator.iterator();
    		if(cursor!=null) {
    			while (cursor.hasNext()) {
    				doc = cursor.next();
    			}
    		}
    		return doc;
		} catch (Exception e) {
			logger.error("查询异常", e);
			return null;
		}
    }

	/**
	 * 根据条件统计
	 * @param coll
	 * @param filter
     * @return
     */
    public long count(MongoCollection<Document> coll, BsonDocument filter) {
    	if(coll==null) {
    		return 0;
    	}
    	try {
    		return coll.count(filter);
    	} catch (Exception e) {
    		logger.error("统计异常");
    		return 0;
    	}
    }
    //---------------------------- 获取结束 -----------------------------------
	/**
	 * 插入
	 * @param coll
	 * @param doc
     * @return
     */
    public boolean insert(MongoCollection<Document> coll, Document doc) {
    	if(coll==null) {
    		return false;
    	}
    	try {
    		coll.insertOne(doc);
    		return true;
		} catch (Exception e) {
			logger.error("插入失败！！！", e);
			return false;
		}
    }
    
	/**
	 * 批量插入
	 * @param coll
	 * @param docList
     * @return
     */
    public boolean batchInsert(MongoCollection<Document> coll, List<Document> docList) {
    	if(coll==null) {
    		return false;
    	}
    	try {
    		coll.insertMany(docList);
    		return true;
		} catch (Exception e) {
			logger.error("批量插入失败！！！", e);
			return false;
		}
    }
    
	/**
	 * 根据条件替换一条
	 * @param coll
	 * @param filter
	 * @param document
     * @return
     */
	public Document findOneAndReplace(MongoCollection<Document> coll, Bson filter, Document document) {
    	if(coll==null) {
    		return null;
    	}
    	try {
    		document = coll.findOneAndReplace(filter, document);
    		return document;
		} catch (Exception e) {
			logger.error("修改失败！！！", e);
			return null;
		}
    }
    //----------------------------------------------------------------------------------
	/**
	 * 修改
	 * @param coll
	 * @param filter
	 * @param document
     * @return
     */
	public boolean update(MongoCollection<Document> coll, Bson filter, Bson document) {
    	if(coll==null) {
    		logger.error("连接 mongo 库失败!!!");
    		return false;
    	}
    	try {
    		UpdateResult result = coll.updateMany(filter, document);
    		return result.wasAcknowledged();
    	} catch (Exception e) {
    		logger.error("批量修改失败！！！", e);
			return false;
		}
    }
}
