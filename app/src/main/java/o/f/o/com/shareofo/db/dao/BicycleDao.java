package o.f.o.com.shareofo.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import o.f.o.com.shareofo.db.model.BicycleData;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "bicycle".
*/
public class BicycleDao extends AbstractDao<BicycleData, Long> {

    public static final String TABLENAME = "bicycle";

    /**
     * Properties of entity BicycleData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Code = new Property(1, String.class, "code", false, "CODE");
        public final static Property Password = new Property(2, String.class, "password", false, "PASSWORD");
        public final static Property Time = new Property(3, java.util.Date.class, "time", false, "TIME");
        public final static Property Deleted = new Property(4, Boolean.class, "deleted", false, "DELETED");
        public final static Property Desc = new Property(5, String.class, "desc", false, "DESC");
    };

    private DaoSession daoSession;


    public BicycleDao(DaoConfig config) {
        super(config);
    }
    
    public BicycleDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"bicycle\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"CODE\" TEXT," + // 1: code
                "\"PASSWORD\" TEXT," + // 2: password
                "\"TIME\" INTEGER," + // 3: time
                "\"DELETED\" INTEGER," + // 4: deleted
                "\"DESC\" TEXT);"); // 5: desc
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"bicycle\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, BicycleData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String code = entity.getCode();
        if (code != null) {
            stmt.bindString(2, code);
        }
 
        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(3, password);
        }
 
        java.util.Date time = entity.getTime();
        if (time != null) {
            stmt.bindLong(4, time.getTime());
        }
 
        Boolean deleted = entity.getDeleted();
        if (deleted != null) {
            stmt.bindLong(5, deleted ? 1L: 0L);
        }
 
        String desc = entity.getDesc();
        if (desc != null) {
            stmt.bindString(6, desc);
        }
    }

    @Override
    protected void attachEntity(BicycleData entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public BicycleData readEntity(Cursor cursor, int offset) {
        BicycleData entity = new BicycleData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // code
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // password
            cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)), // time
            cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0, // deleted
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // desc
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, BicycleData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setCode(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPassword(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTime(cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)));
        entity.setDeleted(cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0);
        entity.setDesc(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(BicycleData entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(BicycleData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
