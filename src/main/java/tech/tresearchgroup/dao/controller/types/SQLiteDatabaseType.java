package tech.tresearchgroup.dao.controller.types;

import com.zaxxer.hikari.HikariDataSource;
import tech.tresearchgroup.dao.controller.BaseDAO;
import tech.tresearchgroup.dao.model.BasicObjectInterface;
import tech.tresearchgroup.systemframework.model.KeyValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLiteDatabaseType extends BaseDAO implements DatabaseType {
    private final HikariDataSource hikariDataSource;

    public SQLiteDatabaseType(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public boolean create(Object object) {
        return false;
    }

    @Override
    public Object read(Long id, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return null;
    }

    @Override
    public List<BasicObjectInterface> getAllFromResultSet(ResultSet resultSet, Class theClass, boolean full) throws SQLException {
        return null;
    }

    @Override
    public List readAll(Class theClass, boolean full) throws SQLException {
        return null;
    }

    @Override
    public List readPaginated(int resultCount, int page, Class theClass, boolean full) throws SQLException {
        return null;
    }

    @Override
    public List readMany(List<Long> ids, Class theClass, boolean full) throws SQLException {
        return null;
    }

    @Override
    public List readOrderedBy(int resultCount, int page, Class theClass, String orderBy, boolean ascending, boolean full) throws SQLException {
        return null;
    }

    @Override
    public ResultSet readManyOrderByPaginated(int resultCount, int page, List<String> theClassList, List<Class> orderByList, boolean ascending) throws SQLException {
        return null;
    }

    @Override
    public boolean update(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return false;
    }

    @Override
    public boolean delete(long id, Class theClass) throws SQLException {
        return false;
    }

    @Override
    public Long getTotal(Class theClass) throws SQLException {
        return null;
    }

    @Override
    public List search(int maxResultsSize, String query, String returnColumn, String searchColumn, Class theClass) throws SQLException {
        return null;
    }

    @Override
    public List select(int maxResultsSize, List<KeyValue> clauses, String returnColumn, Class theClass) throws SQLException {
        return null;
    }

    @Override
    public boolean createRelationship(Object firstObject, Object secondObject, String secondObjectName) throws SQLException, InvocationTargetException, IllegalAccessException {
        return false;
    }

    @Override
    public Object getObjectId(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return null;
    }

    @Override
    public void addInterfaceFieldsToObject(List<BasicObjectInterface> objects, List<Field> fields) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {

    }

    @Override
    public boolean tableExists(String tableName) throws SQLException {
        return false;
    }

    @Override
    public boolean createTables(Class theClass) {
        return false;
    }

    @Override
    public List getObjectByIds(List objects) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        return null;
    }

    @Override
    public Long getTotalPages(int maxResultsSize, Class theClass) throws SQLException {
        return getTotalPages(maxResultsSize, theClass, this);
    }

    @Override
    public Object getFromResultSet(ResultSet resultSet, Object object) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        return null;
    }

    @Override
    public Object getIdFromUnique(String key, String value, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return null;
    }
}
