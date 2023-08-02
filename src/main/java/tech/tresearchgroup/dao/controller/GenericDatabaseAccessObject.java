package tech.tresearchgroup.dao.controller;

import tech.tresearchgroup.dao.model.BasicObjectInterface;
import tech.tresearchgroup.systemframework.model.KeyValue;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface GenericDatabaseAccessObject {
    boolean create(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    Object read(Long id, Class theClass) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException;

    List<Object> readAll(Class theClass, boolean full) throws SQLException;

    List readPaginated(int resultCount, int page, Class theClass, boolean full) throws SQLException;

    List readMany(List<Long> ids, Class theClass, boolean full) throws SQLException;

    List readOrderedBy(int resultCount, int page, Class theClass, String orderedBy, boolean ascending, boolean full) throws SQLException;

    ResultSet readManyOrderByPaginated(int resultCount, int page, List<Class> theClassList, List<String> orderByList, boolean ascending) throws SQLException;

    boolean update(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    boolean delete(long id, Class theClass) throws SQLException;

    Long getTotal(Class theClass) throws SQLException;

    List search(int maxResultsSize, String query, String returnColumn, String searchColumn, Class theClass) throws SQLException;

    List select(int maxResultsSize, List<KeyValue> clauses, String returnColumn, Class theClass) throws SQLException;

    boolean createRelationship(Object firstObject, Object secondObject, String secondObjectName) throws SQLException, InvocationTargetException, IllegalAccessException;

    Object getObjectId(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException;

    boolean tableExists(String tableName) throws SQLException;

    Long getTotalPages(int maxResultsSize, Class theClass) throws SQLException;

    boolean createTables(Class theClass) throws SQLException;

    Object getFromResultSet(ResultSet resultSet, Object object) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException;

    List<BasicObjectInterface> getAllFromResultSet(ResultSet resultSet, Class theClass, boolean full) throws SQLException;

    Object getIdFromUnique(String key, String value, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
