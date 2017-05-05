package o.f.o.com.shareofo.db.model;

import o.f.o.com.shareofo.db.dao.DaoSession;
import de.greenrobot.dao.DaoException;

import o.f.o.com.shareofo.db.dao.BicycleDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.sql.Date;
// KEEP INCLUDES END
/**
 * Entity mapped to table "bicycle".
 */
public class BicycleData implements java.io.Serializable {

    private Long id;
    private String code;
    private String password;
    private java.util.Date time;
    private Boolean deleted;
    private String desc;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient BicycleDao myDao;


    // KEEP FIELDS - put your custom fields here
    public BicycleData(String code, String password) {
        this.code = code;
        this.password = password;
        this.time = new Date(System.currentTimeMillis());
    }
    // KEEP FIELDS END

    public BicycleData() {
    }

    public BicycleData(Long id) {
        this.id = id;
    }

    public BicycleData(Long id, String code, String password, java.util.Date time, Boolean deleted, String desc) {
        this.id = id;
        this.code = code;
        this.password = password;
        this.time = time;
        this.deleted = deleted;
        this.desc = desc;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBicycleDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public java.util.Date getTime() {
        return time;
    }

    public void setTime(java.util.Date time) {
        this.time = time;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}