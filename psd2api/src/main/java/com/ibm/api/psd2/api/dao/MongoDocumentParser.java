package com.ibm.api.psd2.api.dao;

import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class MongoDocumentParser
{
	private static final Logger logger = LogManager.getLogger(MongoDocumentParser.class);

	private <T> Class<?> getAddMethodType(T t, String key) throws Exception
	{
		Class<?> c = null;
		Method[] ms = t.getClass().getMethods();
		for (Method m : ms)
		{
			if (m.getName().equals("add" + key))
			{
				c = m.getParameterTypes()[0];
				break;
			}
		}

		if (c == null)
		{
			throw new NoSuchMethodException("no add" + key
					+ " method found. Required due to current limitation in Java of Erasures");
		}

		return c;
	}

	private <T> Method getMethod(T t, String methodName) throws Exception
	{
		Class<?> c = null;
		Method[] ms = t.getClass().getMethods();
		Method rm = null;
		for (Method m : ms)
		{
			if (m.getName().equals(methodName))
			{
				rm = m;
				break;
			}
		}

		if (rm == null)
		{
			throw new NoSuchMethodException("no " + methodName
					+ " method found. Required due to current limitation in Java of Erasures");
		}

		return rm;
	}
	
	private <T> T setValue(T t, String key, Object value) throws Exception
	{
		try
		{
			Method m = getMethod(t, "set"+key);
//			m = t.getClass().getMethod("set" + key, value.getClass());
			m.invoke(t, value);
		}
		catch (Exception e)
		{
			logger.warn("Error while setting attribute: " + key + " exception: " + e.getMessage());
		}
		
		return t;
	}

	public <T> T parse(Document document, T t) throws Exception
	{
		Set<Entry<String, Object>> valueSet = document.entrySet();
		
		if (valueSet == null || valueSet.isEmpty())
		{
			return null;
		}

		for (Entry<String, Object> entry : valueSet)
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value != null)
			{
//				logger.info("key: " + key + " value: " + value + " Class: " + value.getClass());
				key = Character.toUpperCase(key.charAt(0)) + key.substring(1);
				if (value instanceof Document)
				{
					Method m = t.getClass().getMethod("get" + key);
					Class<?> c = m.getReturnType();
					Object o = parse((Document) value, c.newInstance());
					setValue(t, key, o);
				}
				else if (value instanceof ArrayList)
				{
					ArrayList<Document> al = (ArrayList<Document>) value;
					Class<?> c = getAddMethodType(t, key);
					Method toInvoke = getMethod(t, "add"+key);
					for (Iterator<Document> iterator = al.iterator(); iterator.hasNext();)
					{
						Document doc = iterator.next();
						toInvoke.invoke(t, parse(doc, c.newInstance()));
					}
				}
				else
				{
					setValue(t, key, value);
				}
			}
		}

		return t;
	}

	public <T> Document format(T t) throws Exception
	{
		if (t == null)
		{
			return new Document();
		}

		Document document = new Document();
		Method[] methods = t.getClass().getMethods();

		for (Method method : methods)
		{
			if (method.getName().startsWith("get") && !method.getName().equals("getClass")
					&& method.getParameterTypes().length == 0)
			{
				String key = method.getName().substring(3);
				key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
//				logger.debug("checking for key = " + key + ", method name = " + method.getName() + ", return type = " + method.getReturnType());
				if (method.getReturnType().isPrimitive() || method.getReturnType().isAssignableFrom(String.class))
				{
					Object value = method.invoke(t);
					if (value != null)
					{
						logger.debug("setting attribute: " + key + ", value class: " + value.getClass().getName());
						document.append(key, value);
					}
				}
				else if (method.getReturnType().isAssignableFrom(ArrayList.class))
				{
					ArrayList al = (ArrayList) method.invoke(t);

					if (al != null && !al.isEmpty())
					{
						ArrayList<Document> listDocuments = new ArrayList<>();
						for (Iterator iterator = al.iterator(); iterator.hasNext();)
						{
							Object object = (Object) iterator.next();
							Document d = format(object);
							listDocuments.add(d);
						}
						document.append(key, listDocuments);
					}
				}
				else if (method.getReturnType().isAssignableFrom(Set.class))
				{
					Set s = (Set) method.invoke(t);
					if (s != null && !s.isEmpty())
					{
						ArrayList<Document> listDocuments = new ArrayList<>();
						for (Iterator iterator = s.iterator(); iterator.hasNext();)
						{
							Object object = (Object) iterator.next();
							Document d = format(object);
							listDocuments.add(d);
						}
						document.append(key, listDocuments);
					}
				}
				else if (method.getReturnType().isAssignableFrom(Map.class))
				{
					// do nothing for the time being
				}
				else
				{
					Object o = method.invoke(t);
					Document d = format(o);
					document.append(key, d);
				}
			}
		}
		return document;
	}
}
