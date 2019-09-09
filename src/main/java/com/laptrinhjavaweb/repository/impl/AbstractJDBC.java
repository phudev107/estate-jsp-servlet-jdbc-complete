package com.laptrinhjavaweb.repository.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import com.laptrinhjavaweb.annotation.Column;
import com.laptrinhjavaweb.annotation.Table;
import com.laptrinhjavaweb.mapper.ResultSetMapper;
import com.laptrinhjavaweb.paging.Pageble;
import com.laptrinhjavaweb.paging.Sorter;
import com.laptrinhjavaweb.repository.GenericJDBC;

public class AbstractJDBC<T> implements GenericJDBC<T> {

	private Class<T> zClass;

	@SuppressWarnings("unchecked")
	public AbstractJDBC() {
		Type type = getClass().getGenericSuperclass();
		ParameterizedType parameterizedType = (ParameterizedType) type;
		zClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}

	private Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String databaseURL = "jdbc:mysql://localhost:3306/estate4month2019";
			String user = "root";
			String password = "1234";
			return DriverManager.getConnection(databaseURL, user, password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*@Override
	public List<T> query(String sql, Object... parameters) {
		ResultSetMapper<T> resultSetMapper = new ResultSetMapper<>();
		try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(sql);
				ResultSet resultset = statement.executeQuery()) {
//        	Class.forName("com.mysql.jdbc.Driver");
			if (conn != null) {
				// set parameter to statement
				for (int i = 0; i < parameters.length; i++) {
					int index = i + 1;
					statement.setObject(index, parameters[i]);
				}
				return resultSetMapper.mapRow(resultset, this.zClass);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void update(String sql, Object... parameters) {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			statement = conn.prepareStatement(sql);
			Class.forName("com.mysql.jdbc.Driver");
			if (conn != null) {
				// set parameter to statement
				for (int i = 0; i < parameters.length; i++) {
					int index = i + 1;
					statement.setObject(index, parameters[i]);
				}
				statement.executeUpdate();
				conn.commit();
			}
		} catch (ClassNotFoundException | SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
	}*/

	@Override
	public Long insert(Object object) {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			String tableName = "";

			String sql = creatSQLInsert();
			statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			// Class.forName("com.mysql.jdbc.Driver");
			if (conn != null) {
				Class<?> zClass = object.getClass();
				Field[] fields = zClass.getDeclaredFields();

				for (int i = 0; i < fields.length; i++) {
					int index = i + 1;
					Field field = fields[i];
					field.setAccessible(true);
					statement.setObject(index, field.get(object));
				}
				Class<?> parentClass = zClass.getSuperclass();
				int indexParent = fields.length + 1;
				while (parentClass != null) {
					// convert data logic
					for (int i = 0; i < parentClass.getDeclaredFields().length; i++) {
						Field field = parentClass.getDeclaredFields()[i];
						field.setAccessible(true);
						statement.setObject(indexParent, field.get(object));
						indexParent = indexParent + 1;
					}
					parentClass = parentClass.getSuperclass();
				}
				int rowsInserted = statement.executeUpdate();
				resultset = statement.getGeneratedKeys();
				conn.commit();
				if (rowsInserted > 0) {
					while (resultset.next()) {
						Long id = resultset.getLong(1);
						return id;
					}
				}
			}
		} catch (SQLException | IllegalAccessException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();

			}
		}
		return null;
	}

	private String creatSQLInsert() {
		String tableName = "";
		if (zClass.isAnnotationPresent(Table.class)) {
			Table table = zClass.getAnnotation(Table.class);
			tableName = table.name();
		}
		StringBuilder fields = new StringBuilder("");
		StringBuilder params = new StringBuilder("");
		for (Field field : zClass.getDeclaredFields()) {
			if (fields.length() > 1) {
				fields.append(",");
				params.append(",");
			}
			if (field.isAnnotationPresent(Column.class)) {
				Column column = field.getAnnotation(Column.class);
				fields.append(column.name());
				params.append("?");

			}
		}

		// checkparent class
		Class<?> parentClass = zClass.getSuperclass();
		while (parentClass != null) {
			// convert data logic
			for (Field field : parentClass.getDeclaredFields()) {
				if (fields.length() > 1) {
					fields.append(",");
					params.append(",");
				}
				if (field.isAnnotationPresent(Column.class)) {
					Column column = field.getAnnotation(Column.class);
					fields.append(column.name());
					params.append("?");
				}
			}
			parentClass = parentClass.getSuperclass();
		}
		String sql = "INSERT INTO " + tableName + "(" + fields.toString() + ") VALUE(" + params.toString() + ")";
		return sql;
	}

	@Override
	public void update(Object object) {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			String tableName = "";
			String sql = creatSQLUpdate();
			statement = conn.prepareStatement(sql);
			// Class.forName("com.mysql.jdbc.Driver");
			if (conn != null) {
				Class<?> zClass = object.getClass();
				Field[] fields = zClass.getDeclaredFields();

				for (int i = 0; i < fields.length; i++) {
					int index = i + 1;
					Field field = fields[i];
					field.setAccessible(true);
					statement.setObject(index, field.get(object));
				}
				Class<?> parentClass = zClass.getSuperclass();
				int indexParent = fields.length + 1;
				Object id = null;
				while (parentClass != null) {
					// convert data logic
					for (int i = 0; i < parentClass.getDeclaredFields().length; i++) {
						Field field = parentClass.getDeclaredFields()[i];
						field.setAccessible(true);
						String name = field.getName();
						if (!name.equals("id")) {
							statement.setObject(indexParent, field.get(object));
							indexParent = indexParent + 1;
						} else {
							id = field.get(object);
						}
					}
					parentClass = parentClass.getSuperclass();
				}
				statement.setObject(indexParent, id);
				statement.executeUpdate();
				conn.commit();
			}
		} catch (SQLException | IllegalAccessException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();

			}
		}
	}

	private String creatSQLUpdate() {
		String tableName = "";
		if (zClass.isAnnotationPresent(Table.class)) {
			Table table = zClass.getAnnotation(Table.class);
			tableName = table.name();
		}
		StringBuilder sets = new StringBuilder("");
		String where = null;
		for (Field field : zClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class)) {
				Column column = field.getAnnotation(Column.class);
				String ColumnName = column.name();
				String value = ColumnName + " = ?";
				if (!ColumnName.equals("id")) {
					if (sets.length() > 1) {
						sets.append(",");
					}
					sets.append(value);
				}
			}
		}
		// checkparent class
		Class<?> parentClass = zClass.getSuperclass();
		while (parentClass != null) {
			// convert data logic
			for (Field field : parentClass.getDeclaredFields()) {
				if (field.isAnnotationPresent(Column.class)) {
					Column column = field.getAnnotation(Column.class);
					String ColumnName = column.name();
					String value = ColumnName + " = ?";
					if (!ColumnName.equals("id")) {
						if (sets.length() > 1) {
							sets.append(",");
						}
						sets.append(value);
					} else {
						where = "where" + value;
					}
				}
			}
			parentClass = parentClass.getSuperclass();
		}
		String sql = "UPDATE" + tableName + "SET" + sets.toString() + where;
		return sql;
	}

	@Override
	public void delete(long id) {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);

			String tableName = "";
			if (zClass.isAnnotationPresent(Table.class)) {
				Table table = zClass.getAnnotation(Table.class);
				tableName = table.name();
			}

			String sql = "Delete from" + tableName + "where id=?";
			statement = conn.prepareStatement(sql);
			if (conn != null) {
				statement.setObject(1, id);
				statement.executeUpdate();
				conn.commit();
			}
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();

			}
		}
	}

	@Override
	public <T> T findById(long id) {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		ResultSetMapper<T> resultSetMapper = new ResultSetMapper<>();

		String tableName = "";
		if (zClass.isAnnotationPresent(Table.class)) {
			Table table = zClass.getAnnotation(Table.class);
			tableName = table.name();
		}

		String sql = "SELECT * FROM " + tableName + "WHERE id = ?";
		try {
			conn = getConnection();
			statement = conn.prepareStatement(sql);
			resultset = statement.executeQuery();
			statement.setObject(1, id);
			if (conn != null) {
				return resultSetMapper.mapRow(resultset, this.zClass).get(0);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (resultset != null) {
					resultset.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();

			}
		}
		return null;
	}

	@Override
	public List<T> findAll(Map<String, Object> properties, Pageble pageble, Object... where) {
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		ResultSetMapper<T> resultSetMapper = new ResultSetMapper<>();
		StringBuilder sql = createSQLfindAll(properties);
		if (where != null && where.length > 0) {
			sql.append(where[0]);
		}

		if (pageble != null) {
			if (pageble.getOffset() != null && pageble.getLimit() != null) {
				sql.append(" limit " + pageble.getOffset() + ", " + pageble.getLimit() + "");
			}
			if (pageble.getSorter() != null) {
				Sorter sorter = pageble.getSorter();
				sql.append(" order by " + sorter.getSortName() + " " + sorter.getSortBy() + "");
			}

		}

		try {
			conn = getConnection();
			statement = conn.createStatement();
			resultset = statement.executeQuery(sql.toString());
			if (conn != null) {
				return resultSetMapper.mapRow(resultset, this.zClass);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (resultset != null) {
					resultset.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();

			}
		}
		return null;
	}

	private StringBuilder createSQLfindAll(Map<String, Object> properties) {
		String tableName = "";
		if (zClass.isAnnotationPresent(Table.class)) {
			Table table = zClass.getAnnotation(Table.class);
			tableName = table.name();
		}
		StringBuilder result = new StringBuilder("Select * from " + tableName + "where 1=1");
		if (properties != null && properties.size() > 0) {
			String[] params = new String[properties.size()];
			Object[] values = new Object[properties.size()];
			int i = 0;
			for (Map.Entry<?, ?> item : properties.entrySet()) {
				params[i] = (String) item.getKey();
				values[i] = item.getValue();
				i++;
			}
			for (int i1 = 0; i1 < params.length; i1++) {
				if (values[i1] instanceof String) {
					result.append(" and lower(" + params[i1] + ") like '%" + values[i1] + "%' ");
				} else if (values[i1] instanceof Integer) {
					result.append(" and " + params[i1] + " = " + values[i1] + " ");
				}
			}
		}
		return result;
	}

}
