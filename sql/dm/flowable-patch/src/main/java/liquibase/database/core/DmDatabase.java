package liquibase.database.core;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutorService;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.RawCallStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtil;

public class DmDatabase extends AbstractJdbcDatabase {
    private static final String PRODUCT_NAME = "DM DBMS";

    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    /**
     * Is this AbstractDatabase subclass the correct one to use for the given connection.
     *
     * @param conn
     */
    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    /**
     * If this database understands the given url, return the default driver class name.  Otherwise return null.
     *
     * @param url
     */
    @Override
    public String getDefaultDriver(String url) {
        if(url.startsWith("jdbc:dm")) {
            return "dm.jdbc.driver.DmDriver";
        }

        return null;
    }

    /**
     * Returns an all-lower-case short name of the product.  Used for end-user selecting of database type
     * such as the DBMS precondition.
     */
    @Override
    public String getShortName() {
        return "dm";
    }

    @Override
    public Integer getDefaultPort() {
        return 5236;
    }

    /**
     * Returns whether this database support initially deferrable columns.
     */
    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    private static final Pattern PROXY_USER = Pattern.compile(".*(?:thin|oci)\\:(.+)/@.*");

    protected final int SHORT_IDENTIFIERS_LENGTH = 30;
    protected final int LONG_IDENTIFIERS_LEGNTH = 128;
    public static final int ORACLE_12C_MAJOR_VERSION = 12;

    private Set<String> reservedWords = new HashSet<>();
    private Set<String> userDefinedTypes;
    private Map<String, String> savedSessionNlsSettings;

    private Boolean canAccessDbaRecycleBin;
    private Integer databaseMajorVersion;
    private Integer databaseMinorVersion;

    /**
     * Default constructor for an object that represents the Oracle Database DBMS.
     */
    public DmDatabase() {
        super.unquotedObjectsAreUppercased = true;
        //noinspection HardCodedStringLiteral
        super.setCurrentDateTimeFunction("SYSTIMESTAMP");
        // Setting list of Oracle's native functions
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("SYSDATE"));
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("SYSTIMESTAMP"));
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP"));
        //noinspection HardCodedStringLiteral
        super.sequenceNextValueFunction = "%s.nextval";
        //noinspection HardCodedStringLiteral
        super.sequenceCurrentValueFunction = "%s.currval";
    }

    private void tryProxySession(final String url, final Connection con) {
        Matcher m = PROXY_USER.matcher(url);
        if (m.matches()) {
            Properties props = new Properties();
            props.put("PROXY_USER_NAME", m.group(1));
            try {
                Method method = con.getClass().getMethod("openProxySession", int.class, Properties.class);
                method.setAccessible(true);
                method.invoke(con, 1, props);
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).info("Could not open proxy session on OracleDatabase: " + e.getCause().getMessage());
            }
        }
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        if (databaseMajorVersion == null) {
            return super.getDatabaseMajorVersion();
        } else {
            return databaseMajorVersion;
        }
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        if (databaseMinorVersion == null) {
            return super.getDatabaseMinorVersion();
        } else {
            return databaseMinorVersion;
        }
    }

    @Override
    public String getJdbcCatalogName(CatalogAndSchema schema) {
        return null;
    }

    @Override
    public String getJdbcSchemaName(CatalogAndSchema schema) {
        return correctObjectName((schema.getCatalogName() == null) ? schema.getSchemaName() : schema.getCatalogName(), Schema.class);
    }

    @Override
    protected String getAutoIncrementClause(final String generationType, final Boolean defaultOnNull) {
        if (StringUtil.isEmpty(generationType)) {
            return super.getAutoIncrementClause();
        }

        String autoIncrementClause = "GENERATED %s AS IDENTITY"; // %s -- [ ALWAYS | BY DEFAULT [ ON NULL ] ]
        String generationStrategy = generationType;
        if (Boolean.TRUE.equals(defaultOnNull) && generationType.toUpperCase().equals("BY DEFAULT")) {
            generationStrategy += " ON NULL";
        }
        return String.format(autoIncrementClause, generationStrategy);
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        if (tableName.length() > 27) {
            //noinspection HardCodedStringLiteral
            return "PK_" + tableName.toUpperCase(Locale.US).substring(0, 27);
        } else {
            //noinspection HardCodedStringLiteral
            return "PK_" + tableName.toUpperCase(Locale.US);
        }
    }

    @Override
    public boolean isReservedWord(String objectName) {
        return reservedWords.contains(objectName.toUpperCase());
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    /**
     * Oracle supports catalogs in liquibase terms
     *
     * @return false
     */
    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        if (getConnection() instanceof OfflineConnection) {
            return getConnection().getCatalog();
        }
        try {
            //noinspection HardCodedStringLiteral
            return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForObject(new RawCallStatement("select sys_context( 'userenv', 'current_schema' ) from dual"), String.class);
        } catch (Exception e) {
            //noinspection HardCodedStringLiteral
            Scope.getCurrentScope().getLog(getClass()).info("Error getting default schema", e);
        }
        return null;
    }

    @Override
    public String getDefaultCatalogName() {//NOPMD
        return (super.getDefaultCatalogName() == null) ? null : super.getDefaultCatalogName().toUpperCase(Locale.US);
    }

    /**
     * <p>Returns an Oracle date literal with the same value as a string formatted using ISO 8601.</p>
     *
     * <p>Convert an ISO8601 date string to one of the following results:
     * to_date('1995-05-23', 'YYYY-MM-DD')
     * to_date('1995-05-23 09:23:59', 'YYYY-MM-DD HH24:MI:SS')</p>
     * <p>
     * Implementation restriction:<br>
     * Currently, only the following subsets of ISO8601 are supported:<br>
     * <ul>
     * <li>YYYY-MM-DD</li>
     * <li>YYYY-MM-DDThh:mm:ss</li>
     * </ul>
     */
    @Override
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            return "TO_DATE(" + normalLiteral + ", 'YYYY-MM-DD')";
        } else if (isTimeOnly(isoDate)) {
            return "TO_DATE(" + normalLiteral + ", 'HH24:MI:SS')";
        } else if (isTimestamp(isoDate)) {
            return "TO_TIMESTAMP(" + normalLiteral + ", 'YYYY-MM-DD HH24:MI:SS.FF')";
        } else if (isDateTime(isoDate)) {
            int seppos = normalLiteral.lastIndexOf('.');
            if (seppos != -1) {
                normalLiteral = normalLiteral.substring(0, seppos) + "'";
            }
            return "TO_DATE(" + normalLiteral + ", 'YYYY-MM-DD HH24:MI:SS')";
        }
        return "UNSUPPORTED:" + isoDate;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example == null) {
            return false;
        }

        if (this.isLiquibaseObject(example)) {
            return false;
        }

        if (example instanceof Schema) {
            //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
            if ("SYSTEM".equals(example.getName()) || "SYS".equals(example.getName()) || "CTXSYS".equals(example.getName()) || "XDB".equals(example.getName())) {
                return true;
            }
            //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
            if ("SYSTEM".equals(example.getSchema().getCatalogName()) || "SYS".equals(example.getSchema().getCatalogName()) || "CTXSYS".equals(example.getSchema().getCatalogName()) || "XDB".equals(example.getSchema().getCatalogName())) {
                return true;
            }
        } else if (isSystemObject(example.getSchema())) {
            return true;
        }
        if (example instanceof Catalog) {
            //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
            if (("SYSTEM".equals(example.getName()) || "SYS".equals(example.getName()) || "CTXSYS".equals(example.getName()) || "XDB".equals(example.getName()))) {
                return true;
            }
        } else if (example.getName() != null) {
            //noinspection HardCodedStringLiteral
            if (example.getName().startsWith("BIN$")) { //oracle deleted table
                boolean filteredInOriginalQuery = this.canAccessDbaRecycleBin();
                if (!filteredInOriginalQuery) {
                    filteredInOriginalQuery = StringUtil.trimToEmpty(example.getSchema().getName()).equalsIgnoreCase(this.getConnection().getConnectionUserName());
                }

                if (filteredInOriginalQuery) {
                    return !((example instanceof PrimaryKey) || (example instanceof Index) || (example instanceof
                            liquibase.statement.UniqueConstraint));
                } else {
                    return true;
                }
            } else //noinspection HardCodedStringLiteral
                if (example.getName().startsWith("AQ$")) { //oracle AQ tables
                    return true;
                } else //noinspection HardCodedStringLiteral
                    if (example.getName().startsWith("DR$")) { //oracle index tables
                        return true;
                    } else //noinspection HardCodedStringLiteral
                        if (example.getName().startsWith("SYS_IOT_OVER")) { //oracle system table
                            return true;
                        } else //noinspection HardCodedStringLiteral,HardCodedStringLiteral
                            if ((example.getName().startsWith("MDRT_") || example.getName().startsWith("MDRS_")) && example.getName().endsWith("$")) {
                                // CORE-1768 - Oracle creates these for spatial indices and will remove them when the index is removed.
                                return true;
                            } else //noinspection HardCodedStringLiteral
                                if (example.getName().startsWith("MLOG$_")) { //Created by materliaized view logs for every table that is part of a materialized view. Not available for DDL operations.
                                    return true;
                                } else //noinspection HardCodedStringLiteral
                                    if (example.getName().startsWith("RUPD$_")) { //Created by materialized view log tables using primary keys. Not available for DDL operations.
                                        return true;
                                    } else //noinspection HardCodedStringLiteral
                                        if (example.getName().startsWith("WM$_")) { //Workspace Manager backup tables.
                                            return true;
                                        } else //noinspection HardCodedStringLiteral
                                            if ("CREATE$JAVA$LOB$TABLE".equals(example.getName())) { //This table contains the name of the Java object, the date it was loaded, and has a BLOB column to store the Java object.
                                                return true;
                                            } else //noinspection HardCodedStringLiteral
                                                if ("JAVA$CLASS$MD5$TABLE".equals(example.getName())) { //This is a hash table that tracks the loading of Java objects into a schema.
                                                    return true;
                                                } else //noinspection HardCodedStringLiteral
                                                    if (example.getName().startsWith("ISEQ$$_")) { //System-generated sequence
                                                        return true;
                                                    } else //noinspection HardCodedStringLiteral
                                                        if (example.getName().startsWith("USLOG$")) { //for update materialized view
                                                            return true;
                                                        } else if (example.getName().startsWith("SYS_FBA")) { //for Flashback tables
                                                            return true;
                                                        }
        }

        return super.isSystemObject(example);
    }

    @Override
    public boolean supportsAutoIncrement() {
        // Oracle supports Identity beginning with version 12c
        boolean isAutoIncrementSupported = false;

        try {
            if (getDatabaseMajorVersion() >= 12) {
                isAutoIncrementSupported = true;
            }

            // Returning true will generate create table command with 'IDENTITY' clause, example:
            // CREATE TABLE AutoIncTest (IDPrimaryKey NUMBER(19) GENERATED BY DEFAULT AS IDENTITY NOT NULL, TypeID NUMBER(3) NOT NULL, Description NVARCHAR2(50), CONSTRAINT PK_AutoIncTest PRIMARY KEY (IDPrimaryKey));

            // While returning false will continue to generate create table command without 'IDENTITY' clause, example:
            // CREATE TABLE AutoIncTest (IDPrimaryKey NUMBER(19) NOT NULL, TypeID NUMBER(3) NOT NULL, Description NVARCHAR2(50), CONSTRAINT PK_AutoIncTest PRIMARY KEY (IDPrimaryKey));

        } catch (DatabaseException ex) {
            isAutoIncrementSupported = false;
        }

        return isAutoIncrementSupported;
    }


//    public Set<UniqueConstraint> findUniqueConstraints(String schema) throws DatabaseException {
//        Set<UniqueConstraint> returnSet = new HashSet<UniqueConstraint>();
//
//        List<Map> maps = new Executor(this).queryForList(new RawSqlStatement("SELECT UC.CONSTRAINT_NAME, UCC.TABLE_NAME, UCC.COLUMN_NAME FROM USER_CONSTRAINTS UC, USER_CONS_COLUMNS UCC WHERE UC.CONSTRAINT_NAME=UCC.CONSTRAINT_NAME AND CONSTRAINT_TYPE='U' ORDER BY UC.CONSTRAINT_NAME"));
//
//        UniqueConstraint constraint = null;
//        for (Map map : maps) {
//            if (constraint == null || !constraint.getName().equals(constraint.getName())) {
//                returnSet.add(constraint);
//                Table table = new Table((String) map.get("TABLE_NAME"));
//                constraint = new UniqueConstraint(map.get("CONSTRAINT_NAME").toString(), table);
//            }
//        }
//        if (constraint != null) {
//            returnSet.add(constraint);
//        }
//
//        return returnSet;
//    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        //noinspection HardCodedStringLiteral
        if ("BINARY_FLOAT".equals(dataTypeName.toUpperCase())) {
            return 0;
        }
        //noinspection HardCodedStringLiteral
        if ("BINARY_DOUBLE".equals(dataTypeName.toUpperCase())) {
            return 0;
        }
        return super.getDataTypeMaxParameters(dataTypeName);
    }

    public String getSystemTableWhereClause(String tableNameColumn) {
        List<String> clauses = new ArrayList<String>(Arrays.asList("BIN$",
                "AQ$",
                "DR$",
                "SYS_IOT_OVER",
                "MLOG$_",
                "RUPD$_",
                "WM$_",
                "ISEQ$$_",
                "USLOG$",
                "SYS_FBA"));

        for (int i = 0;i<clauses.size(); i++) {
            clauses.set(i, tableNameColumn+" NOT LIKE '"+clauses.get(i)+"%'");
        }
        return "("+ StringUtil.join(clauses, " AND ") + ")";
    }

    @Override
    public boolean jdbcCallsCatalogsSchemas() {
        return true;
    }

    public Set<String> getUserDefinedTypes() {
        if (userDefinedTypes == null) {
            userDefinedTypes = new HashSet<>();
            if ((getConnection() != null) && !(getConnection() instanceof OfflineConnection)) {
                try {
                    try {
                        //noinspection HardCodedStringLiteral
                        userDefinedTypes.addAll(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForList(new RawSqlStatement("SELECT DISTINCT TYPE_NAME FROM ALL_TYPES"), String.class));
                    } catch (DatabaseException e) { //fall back to USER_TYPES if the user cannot see ALL_TYPES
                        //noinspection HardCodedStringLiteral
                        userDefinedTypes.addAll(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForList(new RawSqlStatement("SELECT TYPE_NAME FROM USER_TYPES"), String.class));
                    }
                } catch (DatabaseException e) {
                    //ignore error
                }
            }
        }

        return userDefinedTypes;
    }

    @Override
    public String generateDatabaseFunctionValue(DatabaseFunction databaseFunction) {
        //noinspection HardCodedStringLiteral
        if ((databaseFunction != null) && "current_timestamp".equalsIgnoreCase(databaseFunction.toString())) {
            return databaseFunction.toString();
        }
        if ((databaseFunction instanceof SequenceNextValueFunction) || (databaseFunction instanceof
                SequenceCurrentValueFunction)) {
            String quotedSeq = super.generateDatabaseFunctionValue(databaseFunction);
            // replace "myschema.my_seq".nextval with "myschema"."my_seq".nextval
            return quotedSeq.replaceFirst("\"([^\\.\"]+)\\.([^\\.\"]+)\"", "\"$1\".\"$2\"");

        }

        return super.generateDatabaseFunctionValue(databaseFunction);
    }

    @Override
    public ValidationErrors validate() {
        ValidationErrors errors = super.validate();
        DatabaseConnection connection = getConnection();
        if ((connection == null) || (connection instanceof OfflineConnection)) {
            //noinspection HardCodedStringLiteral
            Scope.getCurrentScope().getLog(getClass()).info("Cannot validate offline database");
            return errors;
        }

        if (!canAccessDbaRecycleBin()) {
            errors.addWarning(getDbaRecycleBinWarning());
        }

        return errors;

    }

    public String getDbaRecycleBinWarning() {
        //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,
        // HardCodedStringLiteral
        //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
        return "Liquibase needs to access the DBA_RECYCLEBIN table so we can automatically handle the case where " +
                "constraints are deleted and restored. Since Oracle doesn't properly restore the original table names " +
                "referenced in the constraint, we use the information from the DBA_RECYCLEBIN to automatically correct this" +
                " issue.\n" +
                "\n" +
                "The user you used to connect to the database (" + getConnection().getConnectionUserName() +
                ") needs to have \"SELECT ON SYS.DBA_RECYCLEBIN\" permissions set before we can perform this operation. " +
                "Please run the following SQL to set the appropriate permissions, and try running the command again.\n" +
                "\n" +
                "     GRANT SELECT ON SYS.DBA_RECYCLEBIN TO " + getConnection().getConnectionUserName() + ";";
    }

    public boolean canAccessDbaRecycleBin() {
        if (canAccessDbaRecycleBin == null) {
            DatabaseConnection connection = getConnection();
            if ((connection == null) || (connection instanceof OfflineConnection)) {
                return false;
            }

            Statement statement = null;
            try {
                statement = ((JdbcConnection) connection).createStatement();
                @SuppressWarnings("HardCodedStringLiteral") ResultSet resultSet = statement.executeQuery("select 1 from dba_recyclebin where 0=1");
                resultSet.close(); //don't need to do anything with the result set, just make sure statement ran.
                this.canAccessDbaRecycleBin = true;
            } catch (Exception e) {
                //noinspection HardCodedStringLiteral
                if ((e instanceof SQLException) && e.getMessage().startsWith("ORA-00942")) { //ORA-00942: table or view does not exist
                    this.canAccessDbaRecycleBin = false;
                } else {
                    //noinspection HardCodedStringLiteral
                    Scope.getCurrentScope().getLog(getClass()).warning("Cannot check dba_recyclebin access", e);
                    this.canAccessDbaRecycleBin = false;
                }
            } finally {
                JdbcUtils.close(null, statement);
            }
        }

        return canAccessDbaRecycleBin;
    }

    @Override
    public boolean supportsNotNullConstraintNames() {
        return true;
    }

    /**
     * Tests if the given String would be a valid identifier in Oracle DBMS. In Oracle, a valid identifier has
     * the following form (case-insensitive comparison):
     * 1st character: A-Z
     * 2..n characters: A-Z0-9$_#
     * The maximum length of an identifier differs by Oracle version and object type.
     */
    public boolean isValidOracleIdentifier(String identifier, Class<? extends DatabaseObject> type) {
        if ((identifier == null) || (identifier.length() < 1))
            return false;

        if (!identifier.matches("^(i?)[A-Z][A-Z0-9\\$\\_\\#]*$"))
            return false;

        /*
         * @todo It seems we currently do not have a class for tablespace identifiers, and all other classes
         * we do know seem to be supported as 12cR2 long identifiers, so:
         */
        return (identifier.length() <= LONG_IDENTIFIERS_LEGNTH);
    }

    /**
     * Returns the maximum number of bytes (NOT: characters) for an identifier. For Oracle <=12c Release 20, this
     * is 30 bytes, and starting from 12cR2, up to 128 (except for tablespaces, PDB names and some other rather rare
     * object types).
     *
     * @return the maximum length of an object identifier, in bytes
     */
    public int getIdentifierMaximumLength() {
        try {
            if (getDatabaseMajorVersion() < ORACLE_12C_MAJOR_VERSION) {
                return SHORT_IDENTIFIERS_LENGTH;
            } else if ((getDatabaseMajorVersion() == ORACLE_12C_MAJOR_VERSION) && (getDatabaseMinorVersion() <= 1)) {
                return SHORT_IDENTIFIERS_LENGTH;
            } else {
                return LONG_IDENTIFIERS_LEGNTH;
            }
        } catch (DatabaseException ex) {
            throw new UnexpectedLiquibaseException("Cannot determine the Oracle database version number", ex);
        }

    }
}
