package io.github.duo2005duo.RumtimeFramework;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@interface Column {
	String name(); // name用来设置字段名
}

@Retention(RetentionPolicy.RUNTIME)
@interface Table {
	String name(); // name用来设置表名
}

@Table(name = "BeanTable") // 注解@Table 具体实现接下来会讲
class Bean {
	@Column(name = "field") // 注解@Colomn 具体实现接下来会讲
	int field;
	@Column(name = "description")
	String description;
}

public class Test {
	public static void main(String... args) {
		// Utils类是我们需要定义的框架工具，接下来会讲
		System.out.println(Utils.createTable(Bean.class));
	}
}

class Utils {
	public static String createTable(Class<?> bean) {
		String tableName = getTableName(bean);
		List<NameAndType> columns = getColumns(bean);
		if (tableName != null && !tableName.equals("") && !columns.isEmpty()) {
			StringBuilder createTableSql = new StringBuilder("create table ");
			//加表名
			createTableSql.append(tableName);
			createTableSql.append("(");
			
			//加表中字段
			for (int i = 0; i < columns.size(); i++) {
				NameAndType column = columns.get(i);
				createTableSql.append(column.name);
				createTableSql.append(" ");
				createTableSql.append(column.type);
				// 追加下一个字段定义前需要添加逗号
				if (i != columns.size() - 1) {
					createTableSql.append(",");
				}
			}
			createTableSql.append(")");
			return createTableSql.toString();
		}

		return null;
	}
	/**
	 * 建表所需要的字段中的Name和type
	 */
	private static class NameAndType {
		final String type;
		final String name;

		public NameAndType(String type, String name) {
			this.type = type;
			this.name = name;
		}
	}
	/**
	 * 获取建表所需要的字段数据
	 */
	private static List<NameAndType> getColumns(Class<?> bean) {
		List<NameAndType> columns = new ArrayList<NameAndType>();
		Field[] fields = bean.getDeclaredFields();
		if (fields != null) {
			//分析Bean中的变量是否需要生成sql字段
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.isAnnotationPresent(Column.class)) {
					//生成sql字段的名
					String name = null;
					Annotation annotation = field.getAnnotation(Column.class);
					try {
						Method method = Column.class.getMethod("name");
						name = (String) method.invoke(annotation);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//生成sql字段的类型
					String type = null;
					if (int.class.isAssignableFrom(field.getType())) {
						type = "integer";
					} else if (String.class.isAssignableFrom(field.getType())) {
						type = "text";
					} else {
						throw new RuntimeException("unspported type=" + field.getType().getSimpleName());
					}
					columns.add(new NameAndType(type, name));

				}

			}
		}
		return columns;
	}

	private static String getTableName(Class<?> bean) {
		String name = null;
		//判断是否有Table注解
		if (bean.isAnnotationPresent(Table.class)) {
			//获取注解对象
			Annotation annotation = bean.getAnnotation(Table.class);
			try {
				//获取注解@Table所对应的name
				Method method = Table.class.getMethod("name");
				name = (String) method.invoke(annotation);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return name;
	}

}
