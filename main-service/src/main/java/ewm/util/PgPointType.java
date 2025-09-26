package ewm.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.geometric.PGpoint;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PgPointType implements UserType<PGpoint> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<PGpoint> returnedClass() {
        return PGpoint.class;
    }

    @Override
    public boolean equals(PGpoint x, PGpoint y) {
        if (x == y) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    @Override
    public int hashCode(PGpoint x) {
        return x != null ? x.hashCode() : 0;
    }

    @Override
    public PGpoint nullSafeGet(ResultSet rs, int position,
                               SharedSessionContractImplementor session,
                               Object owner) throws SQLException {
        Object value = rs.getObject(position);
        if (value == null) {
            return null;
        }

        if (value instanceof PGpoint) {
            return (PGpoint) value;
        }

        if (value instanceof String) {
            String pointStr = (String) value;
            pointStr = pointStr.replace("(", "").replace(")", "");
            String[] coords = pointStr.split(",");
            double x = Double.parseDouble(coords[0].trim());
            double y = Double.parseDouble(coords[1].trim());
            return new PGpoint(x, y);
        }

        throw new IllegalArgumentException("Неизвестный тип для point: " + value.getClass());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, PGpoint value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value);
        }
    }

    @Override
    public PGpoint deepCopy(PGpoint value) {
        if (value == null) return null;
        return new PGpoint(value.x, value.y);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(PGpoint value) {
        return deepCopy(value);
    }

    @Override
    public PGpoint assemble(Serializable cached, Object owner) {
        return deepCopy((PGpoint) cached);
    }
}
