package net.joywise.bigdata.news.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import net.joywise.bigdata.news.bean.News;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
	private static JedisPool pool = null;
	private static Logger logger = Logger.getLogger(RedisClient.class);
	private static String redisServerIp = null;
	private static Integer redisServerPort = null;

	private static void initialPool() {
		try {
			redisServerIp = "192.168.20.14";
			redisServerPort = Integer.parseInt("6379");
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(5);
			config.setMinIdle(2);
			config.setMaxWaitMillis(1000);
			config.setTestOnBorrow(true);
			pool = new JedisPool(config, redisServerIp, redisServerPort, 5000);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("create JedisPool error : " + e);
		}
	}

	/**
	 * ��ȡ����
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		String value = null;

		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			value = jedis.get(key);
			logger.info("check the key: " + key + " is stored in redis or not, " + (value == null ? false : true));
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("Redis get key error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}

		return value;
	}

	/**
	 * rpush
	 */
	public static long rpush(String key, String value) {
		long size = 0;
		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			size = jedis.rpush(key, new String[] { value });
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("rPush error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}

		return size;
	}

	/**
	 * rpush
	 */
	public static long rpush(String key, News value) {
		long size = 0;
		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			size = jedis.rpush(key.getBytes(), toByteArray(value));
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("rPush error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}

		return size;
	}

	/**
	 * lpop
	 */
	public static String lpop(String key) {
		String value = "";
		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			value = jedis.lpop(key);
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("lpop error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}

		return value;
	}
	
	public static Long del(String key) {
		Long value = 0l;
		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			value = jedis.del(key);
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("del error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}

		return value;
	}

	/**
	 * lpop
	 */
	public static News lpop(byte[] key) {
		News news = null;
		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			byte[] new1 = jedis.lpop(key);
			if(new1!=null){
				Object obj = toObject(new1);
				if(obj instanceof News){
					news=(News)obj;
				}
			}
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("lpop error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}

		return news;
	}

	/**
	 * ��������
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static String set(String key, String value) {

		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			return jedis.set(key, value);
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("Redis set key error : " + e);
			jedis.close();
		} finally {
			// ���������ӳ�
			jedis.close();
		}
		return null;

	}

	/**
	 * ��������
	 * 
	 * @param batches
	 * @return
	 */
	public static void setBatch(Map<String, String> batches) {

		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			Iterator<Entry<String, String>> iter = batches.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				String key = entry.getKey();
				String val = entry.getValue();
				jedis.set(key, val);
				logger.info("set key:" + key + " to redis,value is " + val);
			}
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("Redis setBatch error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}
	}

	/**
	 * ����key����ʱ��
	 * 
	 * @param batches
	 * @return
	 */
	public static void setExpire(String key) {

		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			jedis.expire(key, addDay(1));
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("Redis expire error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}
	}

	/**
	 * ��������
	 * 
	 * @param batches
	 * @return
	 */
	public static void flush() {

		Jedis jedis = null;
		try {
			if (pool == null) {
				initialPool();
			}
			jedis = pool.getResource();
			jedis.flushAll();
		} catch (Exception e) {
			// �ͷ�redis����
			logger.error("Redis flush error : " + e);
			jedis.close();
			e.printStackTrace();
		} finally {
			// ���������ӳ�
			jedis.close();
		}
	}

	/**
	 * ����ӵ�ǰʱ�䵽�����㳿����������������redis key ����ʱ��
	 * 
	 * @param n
	 * @return
	 */
	public static int addDay(int n) {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cd = Calendar.getInstance();
			cd.setTime(sdf.parse(sdf.format(new Date())));
			cd.add(Calendar.DATE, n);// ����һ��
			String addOne = sdf.format(cd.getTime()) + " 00:00:00";
			int lingdian = (int) sdf1.parse(addOne).getTime();
			int now = (int) new Date().getTime();
			return (lingdian - now) / 1000;

		} catch (Exception e) {
			logger.error("���ڼ����쳣��", e);
			return 0;
		}

	}

	/**
	 * ����ת����������
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] toByteArray(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bytes;
	}

	/**
	 * ������ת����
	 * @param bytes
	 * @return
	 */
	public static Object toObject(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;
	}
}