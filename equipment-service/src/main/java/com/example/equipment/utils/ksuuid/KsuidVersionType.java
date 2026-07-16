package com.example.equipment.utils.ksuuid;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.hibernate.usertype.UserVersionType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Slf4j
public class KsuidVersionType implements UserType<KsuidVersion>, UserVersionType<KsuidVersion> {

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class returnedClass() {
        return KsuidVersion.class;
    }

    @Override
    public boolean equals(KsuidVersion x, KsuidVersion y) throws HibernateException {
        if (x == y) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    @Override
    public int hashCode(KsuidVersion x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public KsuidVersion nullSafeGet(ResultSet rs, int index, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
        String val = rs.getString(index);
        if (val == null) {
            throw new SQLException("ETag must not be null");
        }
        return new KsuidVersion(val);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, KsuidVersion value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            if (log.isTraceEnabled()) {
                log.trace("Binding parameter [{}] as [VARCHAR] - [null]", index);
            }
            st.setNull(index, Types.VARCHAR);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Binding parameter [{}] as [VARCHAR] - [{}]", index, value);
            }
            st.setString(index, value.toString());
        }
    }

    @Override
    public KsuidVersion deepCopy(KsuidVersion value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(KsuidVersion value) throws HibernateException {
        return value.toString();
    }

    @Override
    public KsuidVersion assemble(Serializable cached, Object owner) throws HibernateException {
        return new KsuidVersion((String) cached);
    }

    @Override
    public KsuidVersion replace(KsuidVersion original, KsuidVersion target, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public KsuidVersion seed(SharedSessionContractImplementor session) {
        return new KsuidVersion();
    }

    @Override
    public KsuidVersion next(KsuidVersion current, SharedSessionContractImplementor session) {
        return current.increment();
    }

    @Override
    public int compare(KsuidVersion o1, KsuidVersion o2) {
        if (o1 == o2) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        return o1.toString().compareTo(o2.toString());
    }

}