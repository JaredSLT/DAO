package tech.tresearchgroup.dao.controller;

import tech.tresearchgroup.dao.controller.types.DatabaseTypeInterface;
import tech.tresearchgroup.dao.model.BasicObjectInterface;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseDAO {
    protected boolean hasRelationships(Field[] fields) {
        for (Field field : fields) {
            if (field.getType().toString().equals("interface java.util.List")) {
                return true;
            }
        }
        return false;
    }

    public void addFields(Field[] fields, Class theClass, Object object, PreparedStatement preparedStatement) throws InvocationTargetException, SQLException, IllegalAccessException {
        int id = 1;
        for (Field field : fields) {
            Class fieldClass = field.getType();
            Method method = ReflectionMethods.getGetter(field, theClass);
            Object fieldObject = method.invoke(object);
            if (fieldObject == null) {
                if (!field.getType().toString().equals("interface java.util.List")) {
                    preparedStatement.setNull(id, Types.NULL);
                }
            } else {
                if (field.getType().toString().equals("class java.util.Date")) {
                    Date date = (Date) fieldObject;
                    preparedStatement.setDate(id, new java.sql.Date(date.getTime()));
                } else if (Long.class.equals(fieldClass)) {
                    preparedStatement.setLong(id, (Long) fieldObject);
                } else if (Integer.class.equals(fieldClass)) {
                    preparedStatement.setInt(id, (Integer) fieldObject);
                } else if (field.getType().toString().equals("class java.lang.String")) {
                    preparedStatement.setString(id, (String) fieldObject);
                } else if (Float.class.equals(fieldClass)) {
                    preparedStatement.setFloat(id, (Float) fieldObject);
                } else if (Byte.class.equals(fieldClass)) {
                    preparedStatement.setByte(id, (Byte) fieldObject);
                } else if (Character.class.equals(fieldClass)) {
                    preparedStatement.setString(id, String.valueOf(fieldObject));
                } else if (Double.class.equals(fieldClass)) {
                    preparedStatement.setDouble(id, (Double) fieldObject);
                } else if (long.class.equals(fieldClass)) {
                    preparedStatement.setLong(id, (long) fieldObject);
                } else if (int.class.equals(fieldClass)) {
                    preparedStatement.setInt(id, (int) fieldObject);
                } else if (float.class.equals(fieldClass)) {
                    preparedStatement.setFloat(id, (Float) fieldObject);
                } else if (byte.class.equals(fieldClass)) {
                    preparedStatement.setByte(id, (byte) fieldObject);
                } else if (char.class.equals(fieldClass)) {
                    preparedStatement.setString(id, String.valueOf((char) fieldObject));
                } else if (boolean.class.equals(fieldClass)) {
                    preparedStatement.setBoolean(id, (boolean) fieldObject);
                } else if (double.class.equals(fieldClass)) {
                    preparedStatement.setDouble(id, (double) fieldObject);
                } else if (fieldClass.isEnum()) {
                    preparedStatement.setString(id, String.valueOf(fieldObject));
                } else {
                    preparedStatement.setNull(id, Types.NULL);
                }
            }
            if (!field.getType().toString().equals("interface java.util.List")) {
                id++;
            }
        }
    }

    public void createRelationships(Field[] fields,
                                    Class theClass,
                                    Object object,
                                    DatabaseTypeInterface databaseTypeInterface) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException, NoSuchMethodException {
        for (Field field : fields) {
            Method method = ReflectionMethods.getGetter(field, theClass);
            if (field.getType().equals(List.class)) {
                List list = (List) method.invoke(object);
                if (list != null) {
                    for (Object listObject : list) {
                        databaseTypeInterface.create(listObject);
                    }
                    List withIds = databaseTypeInterface.getObjectByIds(list);
                    if (withIds.size() > 0) {
                        for (Object listObject : withIds) {
                            databaseTypeInterface.createRelationship(object, listObject, field.getName());
                        }
                    }
                }
            }
        }
    }

    public List applySingularFieldsAndGetObjects(ResultSet resultSet, Object object, Field[] fields) {
        List<Field> interfaceFields = new LinkedList<>();
        for (Field field : fields) {
            try {
                Class fieldClass = field.getType();
                Method method = ReflectionMethods.getSetter(field, object.getClass(), fieldClass);
                if (Date.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getDate(field.getName()));
                } else if (Long.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getLong(field.getName()));
                } else if (Integer.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getInt(field.getName()));
                } else if (String.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getString(field.getName()));
                } else if (Float.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getFloat(field.getName()));
                } else if (Byte.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getByte(field.getName()));
                } else if (Character.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getString(field.getName()));
                } else if (Double.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getDouble(field.getName()));
                } else if (long.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getLong(field.getName()));
                } else if (int.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getInt(field.getName()));
                } else if (float.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getFloat(field.getName()));
                } else if (byte.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getByte(field.getName()));
                } else if (char.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getString(field.getName()));
                } else if (boolean.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getBoolean(field.getName()));
                } else if (double.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getDouble(field.getName()));
                } else if (field.getType().isEnum()) {
                    String string = resultSet.getString(field.getName());
                    Object valueMethod = ReflectionMethods.getValueOf(fieldClass, string);
                    if (valueMethod != null) {
                        method.invoke(object, valueMethod);
                    }
                } else if (field.getType().isArray()) {
                    //todo support this
                } else if (field.getType().isInterface()) {
                    interfaceFields.add(field);
                } else {
                    long id = resultSet.getLong(field.getName());
                    if (id != 0) {
                        BasicObjectInterface basicObjectInterface = (BasicObjectInterface) ReflectionMethods.getNewInstance(field.getType());
                        basicObjectInterface.setId(id);
                        method.invoke(object, basicObjectInterface);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return interfaceFields;
    }

    public List<Long> getIdsFromResultSet(ResultSet resultSet) throws SQLException {
        List<Long> objects = new LinkedList<>();
        while (resultSet.next()) {
            objects.add(resultSet.getLong("id"));
        }
        return objects;
    }

    public Long getTotalPages(int maxResultsSize, Class theClass, DatabaseTypeInterface databaseTypeInterface) throws SQLException {
        long total = databaseTypeInterface.getTotal(theClass);
        if (total != 0) {
            return total / (maxResultsSize + 1);
        }
        return 0L;
    }
}
