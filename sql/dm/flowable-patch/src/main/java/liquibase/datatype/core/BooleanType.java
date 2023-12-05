package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtil;

import java.util.Locale;
import java.util.regex.Pattern;

@DataTypeInfo(name = "boolean", aliases = {"java.sql.Types.BOOLEAN", "java.lang.Boolean", "bit", "bool"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BooleanType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        if ((database instanceof Firebird3Database)) {
            return new DatabaseDataType("BOOLEAN");
        }

        if ((database instanceof Db2zDatabase) || (database instanceof FirebirdDatabase)) {
            return new DatabaseDataType("SMALLINT");
        } else if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("bit"));
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
            return new DatabaseDataType("BIT", 1);
        } else if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 1);
        } else if ((database instanceof SybaseASADatabase) || (database instanceof SybaseDatabase)) {
            return new DatabaseDataType("BIT");
        } else if (database instanceof DerbyDatabase) {
            if (((DerbyDatabase) database).supportsBooleanDataType()) {
                return new DatabaseDataType("BOOLEAN");
            } else {
                return new DatabaseDataType("SMALLINT");
            }
        } else if (database instanceof DB2Database) {
            if (((DB2Database) database).supportsBooleanDataType())
                return new DatabaseDataType("BOOLEAN");
            else
                return new DatabaseDataType("SMALLINT");
        } else if (database instanceof HsqlDatabase) {
            return new DatabaseDataType("BOOLEAN");
        } else if (database instanceof PostgresDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bit")) {
                return new DatabaseDataType("BIT", getParameters());
            }
        } else if (database instanceof DmDatabase) { // dhb52: DM Support
            return new DatabaseDataType("bit");
        }

        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        }

        String returnValue;
        if (value instanceof String) {
            value = ((String) value).replaceAll("'", "");
            if ("true".equals(((String) value).toLowerCase(Locale.US)) || "1".equals(value) || "b'1'".equals(((String) value).toLowerCase(Locale.US)) || "t".equals(((String) value).toLowerCase(Locale.US)) || ((String) value).toLowerCase(Locale.US).equals(this.getTrueBooleanValue(database).toLowerCase(Locale.US))) {
                returnValue = this.getTrueBooleanValue(database);
            } else if ("false".equals(((String) value).toLowerCase(Locale.US)) || "0".equals(value) || "b'0'".equals(
                ((String) value).toLowerCase(Locale.US)) || "f".equals(((String) value).toLowerCase(Locale.US)) || ((String) value).toLowerCase(Locale.US).equals(this.getFalseBooleanValue(database).toLowerCase(Locale.US))) {
                returnValue = this.getFalseBooleanValue(database);
            } else if (database instanceof PostgresDatabase && Pattern.matches("b?([01])\\1*(::bit|::\"bit\")?", (String) value)) {
                returnValue = "b'"
                    + value.toString()
                    .replace("b", "")
                    .replace("\"", "")
                    .replace("::it", "")
                    + "'::\"bit\"";
            } else {
                throw new UnexpectedLiquibaseException("Unknown boolean value: " + value);
            }
        } else if (value instanceof Long) {
            if (Long.valueOf(1).equals(value)) {
                returnValue = this.getTrueBooleanValue(database);
            } else {
                returnValue = this.getFalseBooleanValue(database);
            }
        } else if (value instanceof Number) {
            if (value.equals(1) || "1".equals(value.toString()) || "1.0".equals(value.toString())) {
                returnValue = this.getTrueBooleanValue(database);
            } else {
                returnValue = this.getFalseBooleanValue(database);
            }
        } else if (value instanceof DatabaseFunction) {
            return value.toString();
        } else if (value instanceof Boolean) {
            if (((Boolean) value)) {
                returnValue = this.getTrueBooleanValue(database);
            } else {
                returnValue = this.getFalseBooleanValue(database);
            }
        } else {
            throw new UnexpectedLiquibaseException("Cannot convert type " + value.getClass() + " to a boolean value");
        }

        return returnValue;
    }

    protected boolean isNumericBoolean(Database database) {
        if (database instanceof Firebird3Database) {
            return false;
        }
        if (database instanceof DerbyDatabase) {
            return !((DerbyDatabase) database).supportsBooleanDataType();
        } else if (database instanceof DB2Database) {
            return !((DB2Database) database).supportsBooleanDataType();
        }
        return (database instanceof Db2zDatabase)
            || (database instanceof FirebirdDatabase)
            || (database instanceof MSSQLDatabase)
            || (database instanceof MySQLDatabase)
            || (database instanceof OracleDatabase)
            || (database instanceof SQLiteDatabase)
            || (database instanceof SybaseASADatabase)
            || (database instanceof SybaseDatabase)
            || (database instanceof DmDatabase); // dhb52: DM Support
    }

    /**
     * The database-specific value to use for "false" "boolean" columns.
     */
    public String getFalseBooleanValue(Database database) {
        if (isNumericBoolean(database)) {
            return "0";
        }
        if (database instanceof InformixDatabase) {
            return "'f'";
        }
        return "FALSE";
    }

    /**
     * The database-specific value to use for "true" "boolean" columns.
     */
    public String getTrueBooleanValue(Database database) {
        if (isNumericBoolean(database)) {
            return "1";
        }
        if (database instanceof InformixDatabase) {
            return "'t'";
        }
        return "TRUE";
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.BOOLEAN;
    }

}
