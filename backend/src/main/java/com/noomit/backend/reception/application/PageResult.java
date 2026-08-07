package com.noomit.backend.reception.application;

import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {
}