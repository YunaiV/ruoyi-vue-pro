package com.somle.esb.enums;

public enum TenantId {
    DEFAULT(50001L);

    private final long id;

    TenantId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
