package com.noomit.backend.reception.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.noomit.backend.customer.CustomerDirectory;
import com.noomit.backend.customer.CustomerInfo;
import com.noomit.backend.product.ProductDirectory;
import com.noomit.backend.product.ProductInfo;
import com.noomit.backend.reception.domain.ServiceRequest;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import com.noomit.backend.user.UserDirectory;
import com.noomit.backend.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ServiceRequestQueryService {
    private final ServiceRequestQueryRepository requestQueryRepository;
    private final UserDirectory userDirectory;
    private final CustomerDirectory customerDirectory;
    private final ProductDirectory productDirectory;

    public PageResult<ServiceRequestListItem> getList(ServiceRequestListQuery query) {
        PageResult<ServiceRequest> page = requestQueryRepository.search(
                query.status(), query.ascending(), query.page(), query.size());

        RequestViewContext context = loadRequestViewContext(page.content());

        List<ServiceRequestListItem> items = new ArrayList<>();
        for (ServiceRequest request : page.content()) {
            items.add(toListItem(request, context));
        }

        return new PageResult<>(items, page.page(), page.size(), page.totalElements());
    }

    public ServiceRequestDetail getDetail(long id) {
        ServiceRequest request = requestQueryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "접수를 찾을 수 없습니다."));

        CustomerInfo customer = customerDirectory.findById(request.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "고객 정보를 찾을 수 없습니다."));
        ProductInfo product = productDirectory.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "제품 정보를 찾을 수 없습니다."));
        String technicianName = request.technicianId() == null ? null : findTechnician(request.technicianId()).name();

        return new ServiceRequestDetail(
                request.id(),
                customer.name(),
                customer.phoneNumber(),
                customer.address(),
                customer.detailAddress(),
                product.modelName(),
                request.symptom(),
                request.status(),
                technicianName,
                request.visitDate(),
                request.visitStartTime(),
                request.visitEndTime(),
                request.remarks(),
                request.baseFee(),
                request.requestedAt(),
                request.assignedAt(),
                request.cancelledAt(),
                request.cancelReason());
    }

    public List<MyAssignedRequest> getMyAssignedRequests(long technicianId, LocalDate date) {
        List<ServiceRequest> requests = requestQueryRepository.findAssignedByTechnicianAndDate(technicianId, date);
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> customerIds = requests.stream()
                .map(ServiceRequest::customerId)
                .distinct()
                .toList();

        List<Long> productIds = requests.stream()
                .map(ServiceRequest::productId)
                .distinct()
                .toList();

        Map<Long, CustomerInfo> customers = getCustomers(customerIds);
        Map<Long, ProductInfo> products = getProducts(productIds);

        return requests.stream()
                .map(r -> toMyAssignedRequest(r, customers, products))
                .toList();
    }

    private MyAssignedRequest toMyAssignedRequest(ServiceRequest r, Map<Long, CustomerInfo> customers, Map<Long, ProductInfo> products) {
        CustomerInfo customer = customers.get(r.customerId());
        ProductInfo product = products.get(r.productId());
        return new MyAssignedRequest(
                r.id(),
                customer == null ? null : customer.name(),
                customer == null ? null : customer.address(),
                product == null ? null : product.modelName(),
                r.visitStartTime(),
                r.visitEndTime());
    }

    private UserRef findTechnician(long technicianId) {
        return userDirectory.findActiveByIds(List.of(technicianId)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "기사를 찾을 수 없습니다."));
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
        return customerDirectory.findByIds(ids);
    }

    private Map<Long, ProductInfo> getProducts(List<Long> ids) {
        if (ids.isEmpty()) { return Map.of(); }
        return productDirectory.findByIds(ids);
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