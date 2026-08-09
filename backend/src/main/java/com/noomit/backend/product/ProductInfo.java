package com.noomit.backend.product;

/** 다른 모듈에 넘기는 최소 제품 정보. 내부 엔티티·도메인 객체를 밖으로 새지 않게 한다. */
public record ProductInfo(long id, String modelName) {
}
