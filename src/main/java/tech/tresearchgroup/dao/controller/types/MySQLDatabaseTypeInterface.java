package tech.tresearchgroup.dao.controller.types;

import com.zaxxer.hikari.HikariDataSource;
import tech.tresearchgroup.dao.controller.BaseDAO;
import tech.tresearchgroup.dao.controller.ReflectionMethods;
import tech.tresearchgroup.dao.model.BasicObjectInterface;
import tech.tresearchgroup.systemframework.model.KeyValue;

import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MySQLDatabaseTypeInterface extends BaseDAO implements DatabaseTypeInterface {
    private final HikariDataSource hikariDataSource;

    public MySQLDatabaseTypeInterface(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public boolean create(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        try (Connection connection = hikariDataSource.getConnection()) {
            Class<? extends Object> theClass = object.getClass();
            StringBuilder statementBuilder = new StringBuilder();
            Field[] fields = theClass.getDeclaredFields();
            statementBuilder.append("INSERT INTO `").append(theClass.getSimpleName().toLowerCase()).append("` VALUES (");
            for (int i = 0; i != fields.length; i++) {
                if (!fields[i].getType().toString().equals("interface java.util.List")) {
                    statementBuilder.append("?");
                    if (i != (fields.length - 1)) {
                        statementBuilder.append(", ");
                    }
                }
            }
            statementBuilder.append(")");
            PreparedStatement preparedStatement = connection.prepareStatement(statementBuilder.toString());
            addFields(fields, theClass, object, preparedStatement);
            boolean returnThis = preparedStatement.executeUpdate() != 0;
            if (hasRelationships(fields)) {
                Object objectWithId = getObjectId(object);
                Method getId = ReflectionMethods.getId(theClass);
                Long id = (Long) getId.invoke(objectWithId);
                Method setId = ReflectionMethods.setId(theClass, Long.class);
                setId.invoke(object, id);
                createRelationships(fields, theClass, object, this);
            }
            return returnThis;
        }
    }

    @Override
    public Object read(Long id, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from " + theClass.getSimpleName().toLowerCase() + " WHERE `id` = " + id + ";");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet, ReflectionMethods.getNewInstance(theClass));
            }
            return null;
        }
    }

    @Override
    public Object getFromResultSet(ResultSet resultSet, Object object) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        Field[] fields = object.getClass().getDeclaredFields();
        List interfaceFields = applySingularFieldsAndGetObjects(resultSet, object, fields);
        List list = new LinkedList();
        list.add(object);
        addInterfaceFieldsToObject(list, interfaceFields);
        return list.get(0);
    }

    @Override
    public Object getIdFromUnique(String key, String value, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from " + theClass.getSimpleName().toLowerCase() + " WHERE `" + key + "` = '" + value + "';");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet, ReflectionMethods.getNewInstance(theClass));
            }
            return null;
        }
    }

    @Override
    public List<BasicObjectInterface> getAllFromResultSet(ResultSet resultSet, Class theClass, boolean full) throws SQLException {
        List<BasicObjectInterface> objects = new LinkedList<>();
        try {
            List interfaceFields = null;
            while (resultSet.next()) {
                BasicObjectInterface object = (BasicObjectInterface) ReflectionMethods.getNewInstance(theClass);
                Field[] fields = object.getClass().getDeclaredFields();
                if (interfaceFields == null) {
                    interfaceFields = applySingularFieldsAndGetObjects(resultSet, object, fields);
                } else {
                    applySingularFieldsAndGetObjects(resultSet, object, fields);
                }
                objects.add(object);
            }
            if (interfaceFields != null && full) {
                addInterfaceFieldsToObject(objects, interfaceFields);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return objects;
    }

    @Override
    public List readAll(Class theClass, boolean full) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from " + theClass.getSimpleName().toLowerCase() + "");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<BasicObjectInterface> returnThis = getAllFromResultSet(resultSet, theClass, full);
            return returnThis;
        }
    }

    @Override
    public List readPaginated(int resultCount, int page, Class theClass, boolean full) throws SQLException {
        String statement;
        String simpleName = theClass.getSimpleName().toLowerCase();
        if (page == 0) {
            if (resultCount == 0) {
                statement = "SELECT * FROM " + simpleName;
            } else {
                statement = "SELECT * FROM " + simpleName + " LIMIT " + resultCount;
            }
        } else {
            statement = "SELECT * FROM " + simpleName + " LIMIT " + page + "," + resultCount;
        }
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return getAllFromResultSet(resultSet, theClass, full);
        }
    }

    @Override
    public List readMany(List<Long> ids, Class theClass, boolean full) throws SQLException {
        StringBuilder statement = new StringBuilder();
        String simpleClassName = theClass.getSimpleName().toLowerCase();
        statement.append("SELECT * FROM ").append(simpleClassName).append(" WHERE ");
        for (int i = 0; i != ids.size(); i++) {
            statement.append("id = ").append(ids.get(i));
            if (i != (ids.size() - 1)) {
                statement.append(" OR ");
            }
        }
        statement.append(";");
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(statement));
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return getAllFromResultSet(resultSet, theClass, full);
        }
    }

    @Override
    public List readOrderedBy(int resultCount, int page, Class theClass, String orderBy, boolean ascending, boolean full) throws SQLException {
        StringBuilder statement = new StringBuilder();
        String ascDesc = "DESC";
        if (ascending) {
            ascDesc = "ASC";
        }
        statement.append("SELECT * FROM ").append(theClass.getSimpleName().toLowerCase());
        if (orderBy != null && !orderBy.equals("none") && orderBy.length() > 0) {
            statement.append(" ORDER BY ").append(orderBy).append(" ").append(ascDesc);
        }
        if (page == 0) {
            if (resultCount != 0) {
                statement.append(" LIMIT ").append(resultCount);
            }
        } else {
            statement.append(" LIMIT ").append(resultCount).append(",").append(page * resultCount);
        }
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(statement));
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return getAllFromResultSet(resultSet, theClass, full);
        }
    }

    @Override
    public ResultSet readManyOrderByPaginated(int resultCount, int page, List<String> orderByList, List<Class> theClassList, boolean ascending) throws SQLException {
        StringBuilder statement = new StringBuilder();
        String ascDesc = "DESC";
        if (ascending) {
            ascDesc = "ASC";
        }
        for (int classCounter = 0; classCounter != theClassList.size(); classCounter++) {
            Class theClass = theClassList.get(classCounter);
            String theClassSimpleName = theClass.getSimpleName().toLowerCase();
            for (int i = 0; i != orderByList.size(); i++) {
                String orderBy = orderByList.get(i);
                if (page == 0) {
                    if (resultCount == 0) {
                        statement
                                .append("(SELECT `id`, '")
                                .append(theClassSimpleName.toLowerCase())
                                .append("-")
                                .append(orderBy).append("' AS mediaType FROM ")
                                .append(theClassSimpleName)
                                .append(" ORDER BY ")
                                .append(orderBy)
                                .append(" ")
                                .append(ascDesc)
                                .append(")");
                    } else {
                        statement
                                .append("(SELECT `id`, '")
                                .append(theClassSimpleName.toLowerCase())
                                .append("-")
                                .append(orderBy).append("' AS mediaType FROM ")
                                .append(theClassSimpleName)
                                .append(" ORDER BY ")
                                .append(orderBy)
                                .append(" ")
                                .append(ascDesc)
                                .append(" LIMIT ")
                                .append(resultCount).append(")");
                    }
                } else {
                    statement
                            .append("(SELECT `id`, '")
                            .append(theClassSimpleName.toLowerCase())
                            .append("-")
                            .append(orderBy).append("' AS mediaType FROM ")
                            .append(theClassSimpleName)
                            .append(" ORDER BY ")
                            .append(orderBy)
                            .append(" ")
                            .append(ascDesc)
                            .append(" LIMIT ")
                            .append(resultCount)
                            .append(",")
                            .append(page * resultCount)
                            .append(")");
                }
                if (i != (orderByList.size() - 1)) {
                    statement.append(" UNION ALL ");
                }
            }
            if (classCounter != (theClassList.size() - 1)) {
                if (classCounter != (theClassList.size() - 1)) {
                    statement.append(" UNION ALL ");
                }
            }
        }
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(statement));
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return resultSet;
        }
    }

    @Override
    public boolean update(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Class theClass = object.getClass();
        StringBuilder statementBuilder = new StringBuilder();
        Field[] fields = theClass.getDeclaredFields();
        statementBuilder.append("UPDATE ").append(theClass.getSimpleName().toLowerCase()).append(" SET ");
        for (int i = 0; i != fields.length; i++) {
            statementBuilder.append(fields[i].getName()).append(" = ?");
            if (i != (fields.length - 1)) {
                statementBuilder.append(", ");
            }
        }
        Method getId = ReflectionMethods.getId(object.getClass());
        Long id = (Long) getId.invoke(object);
        statementBuilder.append(" WHERE id = ").append(id);
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statementBuilder.toString());
            addFields(fields, theClass, object, preparedStatement);
            boolean returnThis = preparedStatement.executeUpdate() == 0;
            createRelationships(fields, theClass, object, this);
            return returnThis;
        }
    }

    @Override
    public boolean delete(long id, Class theClass) throws SQLException {
        String statementBuilder = "DELETE FROM " + theClass.getSimpleName().toLowerCase() + " " + "WHERE " + "id" + "=?";
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statementBuilder);
            preparedStatement.setLong(1, id);
            boolean returnThis = preparedStatement.executeUpdate() != 0;
            return returnThis;
        }
    }

    @Override
    public Long getTotal(Class theClass) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) AS COUNT FROM " + theClass.getSimpleName().toLowerCase());
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                Long returnThis = resultSet.getLong("COUNT");
                return returnThis;
            }
            return null;
        }
    }

    @Override
    public List search(int maxResultsSize, String query, String returnColumn, String searchColumn, Class theClass) throws SQLException {
        String statement;
        if (returnColumn.equals("*")) {
            statement = "SELECT * FROM " + theClass.getSimpleName().toLowerCase() + " where " + searchColumn + " LIKE '%" + query + "%' LIMIT " + maxResultsSize;
        } else {
            statement = "SELECT " + returnColumn + " from " + theClass.getSimpleName().toLowerCase() + " where " + searchColumn + " LIKE '%" + query + "%' LIMIT " + maxResultsSize;
        }
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return getAllFromResultSet(resultSet, theClass, false);
        }
    }

    @Override
    public boolean createRelationship(Object firstObject, Object secondObject, String secondObjectName) throws SQLException, InvocationTargetException, IllegalAccessException {
        String firstClassName = firstObject.getClass().getSimpleName().toLowerCase();
        String relationTableName = firstClassName + "_" + secondObjectName;
        if (tableExists(firstClassName + "_" + secondObjectName.toLowerCase())) {
            Method firstIdGetter = ReflectionMethods.getId(firstObject.getClass());
            Long firstId = (Long) firstIdGetter.invoke(firstObject);

            Method secondIdGetter = ReflectionMethods.getId(secondObject.getClass());
            Long secondId = (Long) secondIdGetter.invoke(secondObject);
            String stringBuilder = "INSERT INTO " +
                    relationTableName +
                    "(`" +
                    firstClassName +
                    "_id`, `" +
                    secondObjectName +
                    "_id`) VALUES ('" +
                    firstId +
                    "', '" +
                    secondId +
                    "');";
            try (Connection connection = hikariDataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder);
                boolean returnThis = preparedStatement.executeUpdate() != 0;
                return returnThis;
            }
        } else {
            System.err.println("Failed to create relation because table: " + relationTableName + " doesn't exist.");
        }
        return false;
    }

    @Override
    public Object getObjectId(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<? extends Object> objectClass = object.getClass();
        Field[] fields = objectClass.getDeclaredFields();
        List values = new LinkedList();
        for (int i = 0; i != fields.length; i++) {
            Method getter = ReflectionMethods.getGetter(fields[i], objectClass);
            Object value = getter.invoke(object);
            if (value != null) {
                values.add(fields[i].getName());
                values.add(value);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT id FROM ").append(objectClass.getSimpleName().toLowerCase()).append(" WHERE ");
        for (int i = 0; i != values.size(); i += 2) {
            if (ReflectionMethods.isNotArray(values.get(i + 1).getClass()) && !ReflectionMethods.isObject(values.get(i + 1).getClass())) {
                stringBuilder.append(values.get(i));
                stringBuilder.append(" = '").append(values.get(i + 1)).append("'");
                if (i != (values.size() - 2)) {
                    stringBuilder.append(" AND ");
                }
            }
        }
        stringBuilder.append(";");
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(stringBuilder));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                Object newObject = ReflectionMethods.getNewInstance(objectClass);
                Method setId = ReflectionMethods.setId(newObject.getClass(), Long.class);
                setId.invoke(newObject, id);
                return newObject;
            }
            return null;
        }
    }

    @Override
    public void addInterfaceFieldsToObject(List<BasicObjectInterface> objects, List<Field> fields) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        if (fields.size() == 0) {
            return;
        }
        StringBuilder selectString = new StringBuilder();
        selectString.append("id, ");
        StringBuilder joins = new StringBuilder();
        String declaringClassName = fields.get(0).getDeclaringClass().getSimpleName().toLowerCase();
        for (int i = 0; i != fields.size(); i++) {
            Field field = fields.get(i);
            //SELECT
            selectString.append(field.getName());
            selectString.append("_id");
            if ((fields.size() - 1) != i) {
                selectString.append(", ");
            }
            //JOIN
            joins.append("LEFT JOIN ");
            joins.append(declaringClassName.toLowerCase());
            joins.append("_");
            joins.append(field.getName().toLowerCase());
            joins.append(" ON ");
            joins.append(declaringClassName.toLowerCase());
            joins.append(".id");
            joins.append(" = ");
            joins.append(declaringClassName.toLowerCase());
            joins.append("_");
            joins.append(field.getName().toLowerCase());
            joins.append(".");
            joins.append(declaringClassName.toLowerCase());
            joins.append("_id ");
        }
        //WHERE
        StringBuilder whereClauses = new StringBuilder();
        String declaringClassLower = declaringClassName.toLowerCase();
        whereClauses.append("WHERE ").append(declaringClassLower).append(".id = ").append(objects.get(0).getId());
        for (int i = 1; i != objects.size(); i++) {
            whereClauses.append(" OR ").append(declaringClassLower).append(".id = ").append(objects.get(i).getId());
            if (i == (objects.size() - 1)) {
                whereClauses.append(";");
            }
        }
        String statement = "SELECT " + selectString + " FROM " + declaringClassName + " " + joins + whereClauses;
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            //Extract to objects
            while (resultSet.next()) {
                Long objectColumnId = resultSet.getLong("id");
                Object object = null;
                for (BasicObjectInterface itObject : objects) {
                    if (itObject.getId().equals(objectColumnId)) {
                        object = itObject;
                        break;
                    }
                }
                for (Field field : fields) {
                    long columnId = resultSet.getLong(field.getName().toLowerCase() + "_id");
                    if (columnId != 0) {
                        Method getList = ReflectionMethods.getGetter(field, object.getClass());
                        List list = (List) getList.invoke(object);
                        if (list == null) {
                            list = new LinkedList();
                        }
                        try {
                            ParameterizedType pt = (ParameterizedType) field.getGenericType();
                            Type subType = pt.getActualTypeArguments()[0];
                            BasicObjectInterface basicObjectInterface = (BasicObjectInterface) ReflectionMethods.getNewInstance(Class.forName(subType.getTypeName()));
                            basicObjectInterface.setId(columnId);
                            list.add(basicObjectInterface);
                            Method listSetter = ReflectionMethods.getSetter(field, object.getClass(), List.class);
                            listSetter.invoke(object, list);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean tableExists(String tableName) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SHOW TABLES LIKE '" + tableName + "';");
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    @Override
    public boolean createTables(Class theClass) throws SQLException {
        Field[] fields = theClass.getDeclaredFields();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(theClass.getSimpleName().toLowerCase()).append("` (");
        for (Field field : fields) {
            Class fieldClass = field.getType();
            if (Date.class.equals(fieldClass)) {
                if (field.getName().equals("created") || field.getName().equals("updated")) {
                    sql.append("`").append(field.getName()).append("` datetime(6) NULL DEFAULT current_timestamp(6) ON UPDATE current_timestamp(6), ");
                } else {
                    sql.append("`").append(field.getName()).append("` datetime(6) NULL, ");
                }
            } else if (Long.class.equals(fieldClass) || long.class.equals(fieldClass)) {
                if (field.getName().equals("id")) {
                    sql.append("`id` bigint(20) NULL AUTO_INCREMENT, ");
                } else {
                    sql.append("`").append(field.getName()).append("` bigint(20) NULL, ");
                }
            } else if (Integer.class.equals(fieldClass) || int.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` int(11) NULL, ");
            } else if (String.class.equals(fieldClass) || fieldClass.isEnum() || field.getType().isEnum()) {
                sql.append("`").append(field.getName()).append("` varchar(255) NULL, ");
            } else if (Float.class.equals(fieldClass) || float.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` float NULL, ");
            } else if (Byte.class.equals(fieldClass) || byte.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` binary(50) NULL, ");
            } else if (Character.class.equals(fieldClass) || char.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` char(50) NULL, ");
            } else if (Double.class.equals(fieldClass) || double.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` double NULL, ");
            } else if (boolean.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` bit(1) NULL, ");
            } else if (field.getType().isArray()) {
                System.out.println("ARRAYS ARE UNSUPPORTED");
            } else if (field.getType().isInterface()) {
                String simpleLowerClass = theClass.getSimpleName().toLowerCase();
                String simpleLowerFieldClass = field.getName().toLowerCase();
                StringBuilder constraintTables = new StringBuilder();
                constraintTables.append("CREATE TABLE IF NOT EXISTS `").append(simpleLowerClass).append("_").append(simpleLowerFieldClass).append("` (");
                constraintTables.append("`").append(simpleLowerClass).append("_id` bigint(20) NULL,");
                constraintTables.append("`").append(simpleLowerFieldClass).append("_id` bigint(20) NULL,");
                constraintTables.append("KEY `").append(simpleLowerClass).append("` (`").append(simpleLowerClass).append("_id`),");
                constraintTables.append("KEY `").append(simpleLowerFieldClass).append("` (`").append(simpleLowerFieldClass).append("_id`)");
                constraintTables.append(") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
                try (Connection connection = hikariDataSource.getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(constraintTables.toString());
                    preparedStatement.execute();
                }
            } else {
                sql.append("`").append(field.getName()).append("` bigint(20) NULL, ");
            }
        }
        sql.append("PRIMARY KEY (`id`)");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            preparedStatement.execute();
        }
        return true;
    }

    @Override
    public List getObjectByIds(List objects) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        List list = new LinkedList();
        for (Object object : objects) {
            list.add(getObjectId(object));
        }
        if (list.size() > 0) {
            return list;
        }
        return null;
    }

    @Override
    public Long getTotalPages(int maxResultsSize, Class theClass) throws SQLException {
        return getTotalPages(maxResultsSize, theClass, this);
    }

    @Override
    public List select(int maxResultsSize, List<KeyValue> clauses, String returnColumn, Class theClass) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(returnColumn).append(" from ").append(theClass.getSimpleName().toLowerCase()).append(" where ");
        for (int i = 0; i < clauses.size(); i++) {
            KeyValue keyValue = clauses.get(i);
            if (i != clauses.size() - 1) {
                stringBuilder.append("`").append(keyValue.getKey()).append("` = '").append(keyValue.getValue()).append("' AND ");
            } else {
                stringBuilder.append("`").append(keyValue.getKey()).append("` = '").append(keyValue.getValue()).append("'");
            }
        }
        stringBuilder.append(" LIMIT ").append(maxResultsSize);
        try (Connection connection = hikariDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return getAllFromResultSet(resultSet, theClass, false);
        }
    }
}
