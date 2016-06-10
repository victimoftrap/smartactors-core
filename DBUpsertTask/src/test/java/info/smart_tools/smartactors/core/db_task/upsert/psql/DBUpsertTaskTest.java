package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.PreparedQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.UpsertMessage;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.FieldName;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.api.support.membermodification.MemberModifier;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.field;

public class DBUpsertTaskTest {

    private DBUpsertTask task;
    private JDBCCompiledQuery compiledQuery;

    @BeforeClass
    public static void before() throws ScopeProviderException {
        ScopeProvider.subscribeOnCreationNewScope(
            scope -> {
                try {
                    scope.setValue(IOC.getIocKey(), new StrategyContainer());
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
    }

    @Before
    public void setUp()
        throws InvalidArgumentException, RegistrationException, ResolutionException, ReadValueException, ChangeValueException {

        compiledQuery = mock(JDBCCompiledQuery.class);
        String collectionName = "collection";
        UpsertMessage upsertMessage = mock(UpsertMessage.class);
        when(upsertMessage.getCollectionName()).thenReturn(collectionName);

        IOC.register(
            IOC.getKeyForKeyStorage(),
            new ResolveByNameIocStrategy(
                (a) -> {
                    try {
                        return new Key<IKey>((String) a[0]);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
        );
        IKey<DBInsertTask> keyDBInsertTask = Keys.getOrAdd(DBInsertTask.class.toString());
        IKey<UpsertMessage> keyUpsertMessage= Keys.getOrAdd(UpsertMessage.class.toString());
        IKey<QueryStatement> keyQueryStatement = Keys.getOrAdd(QueryStatement.class.toString());
        IKey<IFieldName> keyFieldName = Keys.getOrAdd(IFieldName.class.toString());
        IOC.register(
            keyDBInsertTask,
            new SingletonStrategy(mock(DBInsertTask.class))
        );
        IOC.register(
            keyUpsertMessage,
            new SingletonStrategy(upsertMessage)
        );
        QueryStatement queryStatement = mock(QueryStatement.class);
        when(queryStatement.getBodyWriter()).thenReturn(new StringWriter());
        IOC.register(
            keyQueryStatement,
            new SingletonStrategy(queryStatement)
        );
        IOC.register(
            keyFieldName,
            new CreateNewInstanceStrategy(
                (arg) -> {
                    try {
                        return new FieldName(String.valueOf(arg[0]));
                    } catch (InvalidArgumentException ignored) {}
                    return null;
                }
            )
        );

        task = new DBUpsertTask();
    }

    @Test
    public void ShouldPrepareInsertQuery_When_IdIsNull()
        throws Exception {

        IKey<String> keyString = Keys.getOrAdd(String.class.toString());
        IOC.register(
            keyString,
            new CreateNewInstanceStrategy(
                (arg) -> null)
        );

        IObject upsertMessage = mock(IObject.class);
        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        task.setConnection(connection);
        task.prepare(upsertMessage);

        DBInsertTask dbInsertTask = (DBInsertTask) MemberModifier.field(DBUpsertTask.class, "dbInsertTask").get(task);

        verify(dbInsertTask).setConnection(connection);
        verify(dbInsertTask).prepare(upsertMessage);
    }

    @Test
    public void ShouldPrepareUpdateQuery_When_IdIsGiven()
        throws Exception {

        IKey<String> keyString = Keys.getOrAdd(String.class.toString());
        IOC.register(
            keyString,
            new CreateNewInstanceStrategy(String::valueOf));

        IObject upsertMessage = mock(IObject.class);
        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        task.setConnection(connection);
        task.prepare(upsertMessage);

        QueryStatement updateQueryStatement = (QueryStatement) MemberModifier.field(DBUpsertTask.class, "updateQueryStatement").get(task);

        verify(updateQueryStatement).pushParameterSetter(any(SQLQueryParameterSetter.class));
        verify(connection).compileQuery(eq(updateQueryStatement));
    }

    @Test
    public void ShouldExecuteUpdate_When_ModeIsSetToUpdate()
        throws Exception {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        field(DBUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(DBUpsertTask.class, "mode").set(task, "update");

        task.setConnection(connection);
        task.execute();

        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void ShouldExecuteUpdate_When_ModeIsSetToInsert()
        throws Exception {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        field(DBUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(DBUpsertTask.class, "mode").set(task, "insert");
        IObject rawUpsertQuery = mock(IObject.class);
        field(DBUpsertTask.class, "rawUpsertQuery").set(task, rawUpsertQuery);
        IFieldName fieldName = mock(IFieldName.class);
        field(DBUpsertTask.class, "idFieldName").set(task, fieldName);
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.first()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(123L);

        task.setConnection(connection);
        task.execute();

        verify(preparedStatement).executeQuery();
        verify(rawUpsertQuery).setValue(eq(fieldName), eq(123L));
    }

    @Test(expected = TaskExecutionException.class)
    public void ShouldThrowException_When_NoDocumentsHaveBeenUpdated()
        throws ResolutionException, ReadValueException, ChangeValueException, StorageException, TaskSetConnectionException, TaskExecutionException, TaskPrepareException, IllegalAccessException {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        field(DBUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(DBUpsertTask.class, "mode").set(task, "update");

        task.setConnection(connection);
        task.execute();
    }

    @Test(expected = TaskExecutionException.class)
    public void ShouldThrowException_When_NoDocumentsHaveBeenInserted()
        throws ResolutionException, ReadValueException, ChangeValueException, StorageException, TaskSetConnectionException, TaskExecutionException, TaskPrepareException, IllegalAccessException {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        field(DBUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(DBUpsertTask.class, "mode").set(task, "insert");

        task.setConnection(connection);
        task.execute();
    }
}
