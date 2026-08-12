package com.noomit.backend.reception.application;

import java.time.LocalDate;

public interface ServiceRequestNumberRepository {
    /** 해당 날짜의 카운터를 원자적으로 1 증가시키고 증가된 값을 반환 */
    int issueSequence(LocalDate requestDate);
}