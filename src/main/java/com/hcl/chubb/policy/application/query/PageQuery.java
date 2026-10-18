package com.hcl.chubb.policy.application.query;

public record PageQuery(int page, int size, PolicySortField sortBy, SortDirection direction) {
}
