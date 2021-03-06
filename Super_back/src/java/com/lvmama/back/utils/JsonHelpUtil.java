package com.lvmama.back.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonHelpUtil {
	public static JSONArray path2JsonArray(JSONObject json, String... paths) {
		if (json == null) {
			return null;
		}
		JSONArray ar = null;
		int count = paths.length;
		if (count > 1) {
			JSONObject obj = json;
			for (int i = 0; i < count; i++) {
				if (count != (i + 1)) {
					obj = getJSONObject(obj, paths[i]);
					if (obj == null) {
						break;
					}
				} else {
					ar = obj.getJSONArray(paths[i]);
				}
			}
		} else {
			ar = getJSONArray(json, paths[0]);
		}
		return ar;
	}
	
	public static JSONObject path2JsonObject(JSONObject json, String... paths) {
		if (json == null) {
			return null;
		}
		int count = paths.length;
		JSONObject obj = json;
		for (int i = 0; i < count; i++) {
			obj = getJSONObject(obj, paths[i]);
			if (obj == null) {
				break;
			}
		}
		return obj;
	}
	
	/**
	 * 从JSON中获取一个String值
	 * @param json
	 * @param paths
	 * @return
	 */
	public static String path2String(JSONObject json, String... paths) {
		if (json == null) {
			return null;
		}
		if (paths.length == 1) {
			return json.getString(paths[0]);
		}
		int num = paths.length;
		String[] strs = new String[num - 1];
		for (int i = 0; i < num - 1; i++) {
			strs[i] = paths[i];
		}
		JSONObject obj = path2JsonObject(json, strs);
		if (obj != null) {
			return obj.getString(paths[num - 1]);
		}
		return null;
	}
	
	/**
	 * 更新JSON中的一个属性
	 * @param json
	 * @param value
	 * @param paths
	 */
	public static void updateJson(JSONObject json, Object value, String... paths) {
		if (json == null) {
			return;
		}
		if (paths.length == 1) {
			json.put(paths[0], value);
		}
		int num = paths.length;
		String[] strs = new String[num - 1];
		for (int i = 0; i < num - 1; i++) {
			strs[i] = paths[i];
		}
		JSONObject obj1 = path2JsonObject(json, strs);
		if (obj1 != null) {
			obj1.put(paths[num - 1], value);
		}
	}
	
	/**
	 * 更新JSON中的一个属性
	 * @param json
	 * @param value
	 * @param paths
	 */
	public static String updateJson(String json, Object value, String... paths) {
		if (json == null) {
			return json;
		}
		JSONObject jsonObj = JSONObject.fromObject(json);
		updateJson(jsonObj, value, paths);
		return jsonObj.toString();
	}
	
	public static Long path2Long(JSONObject json, String... paths) {
		String str = path2String(json, paths);
		if (str != null) {
			return Long.valueOf(str);
		}
		return null;
	}
	
	public static JSONArray path2JsonArray(String jsonStr, String... paths) {
		if (jsonStr == null) {
			return null;
		}
		return path2JsonArray(JSONObject.fromObject(jsonStr), paths);
	}
	
	public static JSONObject path2JsonObject(String jsonStr, String... paths) {
		if (jsonStr == null) {
			return null;
		}
		return path2JsonObject(JSONObject.fromObject(jsonStr), paths);
	}
	
	public static String[] getJsonKeys(JSONObject root) {
		if (root == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Set<String> keys = root.keySet();
		String[] strs = new String[keys.size()];
		int count = 0;
		for (String key : keys) {
			strs[count] = key;
			count++;
		}
		return strs;
	}
	
	private static JSONObject getJSONObject(JSONObject root, String key) {
		if (root.has(key)) {
			return root.getJSONObject(key);
		} else {
			return null;
		}
	}
	
	private static JSONArray getJSONArray(JSONObject root, String key) {
		if (root.has(key)) {
			return root.getJSONArray(key);
		} else {
			return null;
		}
	}
	
	/**
	 * JavaBean转Json
	 * @param c
	 * @param obj
	 * @return
	 */
	public static String javaBean2Json(Class<?> c, Object obj) {
		return JSONObject.fromObject(javaBean2Map(c, obj)).toString();
	}
	
	public static Map<String, Object> javaBean2Map(Class<?> c, Object obj) {
		List<String[]> fieldList = new ArrayList<String[]>();
		Field[] fields = c.getDeclaredFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			// 过滤
			if (fieldName.startsWith("this$")) {
				continue;
			}
			String[] s = fieldName.split("_");
			fieldList.add(s);
		}
		return javaBeanHeandle(c, obj, fieldList);
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> javaBeanHeandle(Class<?> c, Object obj, List<String[]> fieldList) {
		Map<String, Object> effDates = new HashMap<String, Object>();
		for (String[] fields : fieldList) {
			if (fields.length > 1) {
				Map<String, Object> fieldMap = effDates;
				for (int i = 0; i < fields.length; i++) {
					String field = fields[i];
					// 最后一个赋值
					if ((i + 1) == fields.length) {
						fieldMap.put(fields[i], JavaBeanUtil.getJavaBeanParam(c, obj, field2Method(fields)));
					} else {
						// 第一次
						if (i == 0) {
							if (effDates.containsKey(field)) {
								fieldMap = (Map<String, Object>) effDates.get(field);
							} else {
								fieldMap = new HashMap<String, Object>();
								effDates.put(field, fieldMap);
							}
						} else {
							if (fieldMap.containsKey(field)) {
								fieldMap = (Map<String, Object>) fieldMap.get(field);
							} else {
								Map<String, Object> tempMap = new HashMap<String, Object>();
								fieldMap.put(field, tempMap);
								fieldMap = tempMap;
							}
						}
					}
				}
			} else {
				effDates.put(fields[0], JavaBeanUtil.getJavaBeanParam(c, obj, field2Method(fields)));
			}
		}
		return effDates;
	}
	
	private static String field2Method(String[] fields) {
		String s = null;
		if (fields.length > 1) {
			for (String str : fields) {
				if (s != null) {
					s = s + "_" + str;
				} else {
					s = str;
				}
			}
		} else {
			s = fields[0];
		}
		return "get" + JavaBeanUtil.firstLetterUpperCase(s);
	}
}
