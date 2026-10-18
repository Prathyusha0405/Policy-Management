package com.hcl.chubb.policy.api.dto.response;

import java.util.List;

public record PolicyPageResponse(
        List<PolicyResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort) {
}
