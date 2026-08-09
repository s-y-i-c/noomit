package com.noomit.backend.customer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * customer 모듈이 다른 모듈에 공개하는 조회 창구.
 *
 * <p>다른 모듈이 customer 테이블을 직접 읽으면 모듈 경계가 무너지므로, 이 인터페이스만 의존하게 한다.
 * 접수(reception) 목록·상세 화면처럼 고객명·연락처·주소만 필요한 화면 표시용이다.</p>
 */
public interface CustomerDirectory {
    /** 단건 조회. 고객이 없으면 빈 값을 반환한다. 활성/비활성 상관없이 조회된다. */
    Optional<CustomerInfo> findById(long customerId);

    /** 배치 조회. 목록 화면에서 N+1 없이 한 번에 가져오라고 Map으로 돌려준다. 없는 id는 그냥 빠진다. */
    Map<Long, CustomerInfo> findByIds(Collection<Long> customerIds);
}
