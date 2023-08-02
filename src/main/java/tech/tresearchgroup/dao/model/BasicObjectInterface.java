package tech.tresearchgroup.dao.model;

import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

public interface BasicObjectInterface {
    @Serialize(order = 0)
    @SerializeNullable String getCreated();

    @Serialize(order = 1)
    @SerializeNullable String getUpdated();

    @Serialize(order = 2)
    @SerializeNullable Long getId();

    @Serialize(order = 3)
    @SerializeNullable LockType getLockType();

    void setCreated(String created);

    void setUpdated(String updated);

    void setId(Long id);

    void setLockType(LockType lockType);
}
