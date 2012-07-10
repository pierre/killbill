package com.ning.billing.util.persist;

/**
 * Instances represent a lock mode for a row of a relational
 * database table. It is not intended that users spend much
 * time worrying about locking since Hibernate usually
 * obtains exactly the right lock level automatically.
 * Some "advanced" users may wish to explicitly specify lock
 * levels.
 *
 * @author Gavin King
 * @see Session#lock(Object, LockMode)
 */
public enum LockMode {
    /**
     * No lock required. If an object is requested with this lock
     * mode, a <tt>READ</tt> lock will be obtained if it is
     * necessary to actually read the state from the database,
     * rather than pull it from a cache.<br>
     * <br>
     * This is the "default" lock mode.
     */
    NONE(0),
    /**
     * A shared lock. Objects in this lock mode were read from
     * the database in the current transaction, rather than being
     * pulled from a cache.
     */
    READ(5),

    /**
     * Attempt to obtain an upgrade lock, using an Oracle-style
     * <tt>select for update nowait</tt>. The semantics of
     * this lock mode, once obtained, are the same as
     * <tt>UPGRADE</tt>.
     */
    UPGRADE_NOWAIT(10),
    /**
     * A <tt>WRITE</tt> lock is obtained when an object is updated
     * or inserted.   This lock mode is for internal use only and is
     * not a valid mode for <tt>load()</tt> or <tt>lock()</tt> (both
     * of which throw exceptions if WRITE is specified).
     */
    WRITE(10),

    /**
     *  start of javax.persistence.LockModeType equivalent modes
     */

    /**
     * Optimisticly assume that transaction will not experience contention for
     * entities.  The entity version will be verified near the transaction end.
     */
    OPTIMISTIC(6),

    /**
     * Optimisticly assume that transaction will not experience contention for
     * entities.  The entity version will be verified and incremented near the transaction end.
     */
    OPTIMISTIC_FORCE_INCREMENT(7),

    /**
     * Implemented as PESSIMISTIC_WRITE.
     * TODO:  introduce separate support for PESSIMISTIC_READ
     */
    PESSIMISTIC_READ(12),

    /**
     * Transaction will obtain a database lock immediately.
     * TODO:  add PESSIMISTIC_WRITE_NOWAIT
     */
    PESSIMISTIC_WRITE(13),

    /**
     * Transaction will immediately increment the entity version.
     */
    PESSIMISTIC_FORCE_INCREMENT(17);
    private final int level;

    private LockMode(int level) {
        this.level = level;
    }

    /**
     * Check if this lock mode is more restrictive than the given lock mode.
     *
     * @param mode LockMode to check
     * @return true if this lock mode is more restrictive than given lock mode
     */
    public boolean greaterThan(LockMode mode) {
        return level > mode.level;
    }

    /**
     * Check if this lock mode is less restrictive than the given lock mode.
     *
     * @param mode LockMode to check
     * @return true if this lock mode is less restrictive than given lock mode
     */
    public boolean lessThan(LockMode mode) {
        return level < mode.level;
    }
}
