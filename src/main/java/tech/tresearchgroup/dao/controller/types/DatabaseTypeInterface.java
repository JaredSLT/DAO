package tech.tresearchgroup.dao.controller.types;

import tech.tresearchgroup.dao.model.BasicObjectInterface;
import tech.tresearchgroup.systemframework.model.KeyValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DatabaseTypeInterface {
    boolean create(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException;

    Object read(Long id, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException;

    List<BasicObjectInterface> getAllFromResultSet(ResultSet resultSet, Class theClass, boolean full) throws SQLException;

    List readAll(Class theClass, boolean full) throws SQLException;

    List readPaginated(int resultCount, int page, Class theClass, boolean full) throws SQLException;

    List readMany(List<Long> ids, Class theClass, boolean full) throws SQLException;

    List readOrderedBy(int resultCount, int page, Class theClass, String orderBy, boolean ascending, boolean full) throws SQLException;

    ResultSet readManyOrderByPaginated(int resultCount, int page, List<String> orderByList, List<Class> theClassList, boolean ascending) throws SQLException;

    boolean update(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    boolean delete(long id, Class theClass) throws SQLException;

    Long getTotal(Class theClass) throws SQLException;

    List search(int maxResultsSize, String query, String returnColumn, String searchColumn, Class theClass) throws SQLException;

    List select(int maxResultsSize, List<KeyValue> clauses, String returnColumn, Class theClass) throws SQLException;

    boolean createRelationship(Object firstObject, Object secondObject, String secondObjectName) throws SQLException, InvocationTargetException, IllegalAccessException;

    Object getObjectId(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException;

    void addInterfaceFieldsToObject(List<BasicObjectInterface> objects, List<Field> fields) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException;

    boolean tableExists(String tableName) throws SQLException;

    boolean createTables(Class theClass) throws SQLException;

    List getObjectByIds(List objects) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException;

    Long getTotalPages(int maxResultsSize, Class theClass) throws SQLException;

    Object getFromResultSet(ResultSet resultSet, Object object) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException;

    Object getIdFromUnique(String key, String value, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
