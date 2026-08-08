package com.noomit.backend.reception.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.noomit.backend.reception.application.customer.CustomerInfo;
import com.noomit.backend.reception.application.customer.CustomerQueryPort;
import com.noomit.backend.reception.application.product.ProductInfo;
import com.noomit.backend.reception.application.product.ProductQueryPort;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.user.UserDirectory;
import com.noomit.backend.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceRequestQueryService {
    private final ServiceRequestQueryRepository requestQueryRepository;
    private final UserDirectory userDirectory;
    private final CustomerQueryPort customerQueryPort;
    private final ProductQueryPort productQueryPort;

    @Transactional(readOnly = true)
    public PageResult<ServiceRequestListItem> findList(ServiceRequestListQuery query) {
        PageResult<ServiceRequest> page = requestQueryRepository.search(
                query.status(), query.ascending(), query.page(), query.size());

        RequestViewContext context = loadRequestViewContext(page.content());

        List<ServiceRequestListItem> items = new ArrayList<>();
        for (ServiceRequest request : page.content()) {
            items.add(toListItem(request, context));
        }

        return new PageResult<>(items, page.page(), page.size(), page.totalElements());
    }

    // 필요한 ID를 모아서 3개 Port를 배치 호출
    private RequestViewContext loadRequestViewContext(List<ServiceRequest> requests) {
        List<Long> customerIds = requests.stream()
                .map(ServiceRequest::customerId).distinct().toList();
        List<Long> productIds = requests.stream()
                .map(ServiceRequest::productId).distinct().toList();
        List<Long> technicianIds = requests.stream()
                .map(ServiceRequest::technicianId).filter(Objects::nonNull).distinct().toList(); // null인 기사는 조회 X

        return new RequestViewContext(
                getCustomers(customerIds),
                getProducts(productIds),
                getTechnicians(technicianIds)
        );
    }

    // 빈 리스트 방어 -> 불필요한 호출 X
    private Map<Long, CustomerInfo> getCustomers(List<Long> ids) {
        if (ids.isEmpty()) { return Map.of(); }
        return customerQueryPort.getCustomers(ids);
    }

    private Map<Long, ProductInfo> getProducts(List<Long> ids) {
        if (ids.isEmpty()) { return Map.of(); }
        return productQueryPort.getProducts(ids);
    }

    private Map<Long, UserRef> getTechnicians(List<Long> ids) {
        if (ids.isEmpty()) { return Map.of(); }
        return userDirectory.findActiveByIds(ids).stream()
                .collect(Collectors.toMap(UserRef::id, ref -> ref));
    }

    // row 하나 조립
    private ServiceRequestListItem toListItem(ServiceRequest r, RequestViewContext c) {
        CustomerInfo customer = c.customers().get(r.customerId());
        ProductInfo product = c.products().get(r.productId());
        UserRef technician = r.technicianId() == null ? null : c.technicians().get(r.technicianId());

        return new ServiceRequestListItem(
                r.id(),
                customer == null ? null : customer.name(),
                customer == null ? null : customer.phoneNumber(),
                product == null ? null : product.modelName(),
                r.symptom(),
                r.status(),
                technician == null ? null : technician.name(),
                r.visitDate(),
                r.visitStartTime(),
                r.visitEndTime(),
                r.requestedAt());
    }

    // 3개 Port 결과를 한 번에 들고 다니는 내부 전용 묶음
    private record RequestViewContext(
            Map<Long, CustomerInfo> customers,
            Map<Long, ProductInfo> products,
            Map<Long, UserRef> technicians) {
    }
}