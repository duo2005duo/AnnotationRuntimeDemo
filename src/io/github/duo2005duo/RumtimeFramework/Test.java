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
	String name(); // name���������ֶ���
}

@Retention(RetentionPolicy.RUNTIME)
@interface Table {
	String name(); // name�������ñ���
}

@Table(name = "BeanTable") // ע��@Table ����ʵ�ֽ������ὲ
class Bean {
	@Column(name = "field") // ע��@Colomn ����ʵ�ֽ������ὲ
	int field;
	@Column(name = "description")
	String description;
}

public class Test {
	public static void main(String... args) {
		// Utils����������Ҫ����Ŀ�ܹ��ߣ��������ὲ
		System.out.println(Utils.createTable(Bean.class));
	}
}

class Utils {
	public static String createTable(Class<?> bean) {
		String tableName = getTableName(bean);
		List<NameAndType> columns = getColumns(bean);
		if (tableName != null && !tableName.equals("") && !columns.isEmpty()) {
			StringBuilder createTableSql = new StringBuilder("create table ");
			//�ӱ���
			createTableSql.append(tableName);
			createTableSql.append("(");
			
			//�ӱ����ֶ�
			for (int i = 0; i < columns.size(); i++) {
				NameAndType column = columns.get(i);
				createTableSql.append(column.name);
				createTableSql.append(" ");
				createTableSql.append(column.type);
				// ׷����һ���ֶζ���ǰ��Ҫ��Ӷ���
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
	 * ��������Ҫ���ֶ��е�Name��type
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
	 * ��ȡ��������Ҫ���ֶ�����
	 */
	private static List<NameAndType> getColumns(Class<?> bean) {
		List<NameAndType> columns = new ArrayList<NameAndType>();
		Field[] fields = bean.getDeclaredFields();
		if (fields != null) {
			//����Bean�еı����Ƿ���Ҫ����sql�ֶ�
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.isAnnotationPresent(Column.class)) {
					//����sql�ֶε���
					String name = null;
					Annotation annotation = field.getAnnotation(Column.class);
					try {
						Method method = Column.class.getMethod("name");
						name = (String) method.invoke(annotation);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//����sql�ֶε�����
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
		//�ж��Ƿ���Tableע��
		if (bean.isAnnotationPresent(Table.class)) {
			//��ȡע�����
			Annotation annotation = bean.getAnnotation(Table.class);
			try {
				//��ȡע��@Table����Ӧ��name
				Method method = Table.class.getMethod("name");
				name = (String) method.invoke(annotation);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return name;
	}

}
