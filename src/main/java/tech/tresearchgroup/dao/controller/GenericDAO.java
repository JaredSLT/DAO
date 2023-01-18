package tech.tresearchgroup.dao.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tresearchgroup.dao.controller.types.DatabaseType;
import tech.tresearchgroup.dao.controller.types.MySQLDatabaseType;
import tech.tresearchgroup.dao.controller.types.SQLiteDatabaseType;
import tech.tresearchgroup.dao.model.BasicObjectInterface;
import tech.tresearchgroup.dao.model.DatabaseTypeEnum;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GenericDAO extends BaseDAO implements GenericDatabaseAccessObject {
    private static final Logger logger = LoggerFactory.getLogger(GenericDAO.class);
    private static DatabaseType genericDatabase;

    public GenericDAO(HikariDataSource hikariDataSource,
                      DatabaseTypeEnum type,
                      Class theClass) throws SQLException {
        if (type.equals(DatabaseTypeEnum.MYSQL)) {
            genericDatabase = new MySQLDatabaseType(hikariDataSource);
        } else {
            genericDatabase = new SQLiteDatabaseType(hikariDataSource);
        }
        if (!tableExists(theClass.getSimpleName().toLowerCase())) {
            if (!createTables(theClass)) {
                logger.info("Failed to create: " + theClass.getSimpleName().toLowerCase() + " tables!");
            }
        }
    }

    @Override
    public boolean create(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return genericDatabase.create(object);
    }

    @Override
    public Object read(Long id, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return genericDatabase.read(id, theClass);
    }

    @Override
    public List readAll(Class theClass, boolean full) throws SQLException {
        return genericDatabase.readAll(theClass, full);
    }

    @Override
    public List readPaginated(int resultCount, int page, Class theClass, boolean full) throws SQLException {
        return genericDatabase.readPaginated(resultCount, page, theClass, full);
    }

    @Override
    public List readMany(List<Long> ids, Class theClass, boolean full) throws SQLException {
        return genericDatabase.readMany(ids, theClass, full);
    }

    @Override
    public List readOrderedBy(int resultCount, int page, Class theClass, String orderBy, boolean ascending, boolean full) throws SQLException {
        return genericDatabase.readOrderedBy(resultCount, page, theClass, orderBy, ascending, full);
    }

    @Override
    public ResultSet readManyOrderByPaginated(int resultCount, int page, List<Class> theClassList, List<String> orderByList, boolean ascending) throws SQLException {
        return genericDatabase.readManyOrderByPaginated(resultCount, page, orderByList, theClassList, ascending);
    }

    @Override
    public boolean update(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return genericDatabase.update(object);
    }

    @Override
    public boolean delete(long id, Class theClass) throws SQLException {
        return genericDatabase.delete(id, theClass);
    }

    @Override
    public Long getTotal(Class theClass) throws SQLException {
        return genericDatabase.getTotal(theClass);
    }

    @Override
    public List search(int maxResultsSize, String query, String returnColumn, String searchColumn, Class theClass) throws SQLException {
        return genericDatabase.search(maxResultsSize, query, returnColumn, searchColumn, theClass);
    }

    @Override
    public boolean createRelationship(Object firstObject, Object secondObject, String secondObjectName) throws SQLException, InvocationTargetException, IllegalAccessException {
        return genericDatabase.createRelationship(firstObject, secondObject, secondObjectName);
    }

    @Override
    public Object getObjectId(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return genericDatabase.getObjectId(object);
    }

    @Override
    public boolean tableExists(String tableName) throws SQLException {
        return genericDatabase.tableExists(tableName);
    }

    @Override
    public Long getTotalPages(int maxResultsSize, Class theClass) throws SQLException {
        return genericDatabase.getTotalPages(maxResultsSize, theClass);
    }

    @Override
    public boolean createTables(Class theClass) {
        return genericDatabase.createTables(theClass);
    }

    @Override
    public Object getFromResultSet(ResultSet resultSet, Object object) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        return genericDatabase.getFromResultSet(resultSet, object);
    }

    @Override
    public List<BasicObjectInterface> getAllFromResultSet(ResultSet resultSet, Class theClass, boolean full) throws SQLException {
        return genericDatabase.getAllFromResultSet(resultSet, theClass, full);
    }
}