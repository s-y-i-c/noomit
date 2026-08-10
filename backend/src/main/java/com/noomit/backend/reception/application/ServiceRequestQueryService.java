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
import com.noomit.backend.product.SubCategoryInfo;
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

        String modelName;
        String subCategoryName;
        // 모델 코드 입력 vs 서브카테고리 + 모델명 입력 처리 구분
        if (request.productId() != null) {
            ProductInfo product = productDirectory.findById(request.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "제품 정보를 찾을 수 없습니다."));
            modelName = product.modelName();
            subCategoryName = null;
        } else {
            SubCategoryInfo subCategory = findSubCategory(request.selectedSubCategoryId());
            modelName = request.selectedModelName();
            subCategoryName = subCategory.name();
        }
        String technicianName = request.technicianId() == null ? null : findTechnician(request.technicianId()).name();
        String receptionistName = findReceptionist(request.receptionistId()).name();

        return new ServiceRequestDetail(
                request.id(),
                request.requestNumber(),
                request.customerId(),
                customer.name(),
                customer.phoneNumber(),
                customer.address(),
                customer.detailAddress(),
                request.productId(),
                request.selectedSubCategoryId(),
                modelName,
                subCategoryName,
                request.symptom(),
                request.status(),
                receptionistName,
                technicianName,
                request.visitDate(),
                request.visitStartTime(),
                request.visitEndTime(),
                request.remarks(),
                request.baseFee(),
                request.requestedAt(),
                request.assignedAt(),
                request.cancelledAt(),
                request.cancelReason(),
                request.version());
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
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, CustomerInfo> customers = getCustomers(customerIds);
        Map<Long, ProductInfo> products = getProducts(productIds);

        return requests.stream()
                .map(r -> toMyAssignedRequest(r, customers, products))
                .toList();
    }

    public MyAssignedRequestDetail getMyAssignedRequestDetail(long technicianId, long requestId) {
        ServiceRequest request = requestQueryRepository.findAssignedByTechnicianAndId(technicianId, requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "접수를 찾을 수 없습니다."));

        CustomerInfo customer = customerDirectory.findById(request.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "고객 정보를 찾을 수 없습니다."));
        String modelName = request.productId() != null
                ? productDirectory.findById(request.productId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "제품 정보를 찾을 수 없습니다."))
                        .modelName()
                : request.selectedModelName();

        return new MyAssignedRequestDetail(
                request.id(),
                request.requestNumber(),
                customer.name(),
                customer.phoneNumber(),
                customer.address(),
                customer.detailAddress(),
                modelName,
                request.symptom(),
                request.remarks(),
                request.visitDate(),
                request.visitStartTime(),
                request.visitEndTime());
    }

    private MyAssignedRequest toMyAssignedRequest(ServiceRequest r, Map<Long, CustomerInfo> customers, Map<Long, ProductInfo> products) {
        CustomerInfo customer = customers.get(r.customerId());
        String modelName = resolveModelName(r, products);
        return new MyAssignedRequest(
                r.id(),
                r.requestNumber(),
                customer == null ? null : customer.name(),
                customer == null ? null : customer.address(),
                modelName,
                r.visitStartTime(),
                r.visitEndTime());
    }

    private UserRef findTechnician(long technicianId) {
        return userDirectory.findActiveByIds(List.of(technicianId)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "기사를 찾을 수 없습니다."));
    }

    private UserRef findReceptionist(long receptionistId) {
        return userDirectory.findActiveByIds(List.of(receptionistId)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "접수자를 찾을 수 없습니다."));
    }

    private SubCategoryInfo findSubCategory(long subCategoryId) {
        SubCategoryInfo subCategory = productDirectory.findSubCategoriesByIds(List.of(subCategoryId)).get(subCategoryId);
        if (subCategory == null) {
            throw new BusinessException(ErrorCode.RECEPTION_NOT_FOUND, "서브카테고리 정보를 찾을 수 없습니다.");
        }
        return subCategory;
    }

    // 필요한 ID를 모아서 5개 Port를 배치 호출
    private RequestViewContext loadRequestViewContext(List<ServiceRequest> requests) {
        List<Long> customerIds = requests.stream()
                .map(ServiceRequest::customerId).distinct().toList();
        List<Long> productIds = requests.stream()
                .map(ServiceRequest::productId).filter(Objects::nonNull).distinct().toList();
        List<Long> subCategoryIds = requests.stream()
                .map(ServiceRequest::selectedSubCategoryId).filter(Objects::nonNull).distinct().toList();
        List<Long> technicianIds = requests.stream()
                .map(ServiceRequest::technicianId).filter(Objects::nonNull).distinct().toList(); // null인 기사는 조회 X
        List<Long> receptionistIds = requests.stream()
                .map(ServiceRequest::receptionistId).distinct().toList();

        return new RequestViewContext(
                getCustomers(customerIds),
                getProducts(productIds),
                getSubCategories(subCategoryIds),
                getTechnicians(technicianIds),
                getReceptionists(receptionistIds)
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

    private Map<Long, SubCategoryInfo> getSubCategories(List<Long> ids) {
        if (ids.isEmpty()) { return Map.of(); }
        return productDirectory.findSubCategoriesByIds(ids);
    }

    private Map<Long, UserRef> getTechnicians(List<Long> ids) {
        if (ids.isEmpty()) { return Map.of(); }
        return userDirectory.findActiveByIds(ids).stream()
                .collect(Collectors.toMap(UserRef::id, ref -> ref));
    }

    private Map<Long, UserRef> getReceptionists(List<Long> ids) {
        if (ids.isEmpty()) { return Map.of(); }
        return userDirectory.findActiveByIds(ids).stream()
                .collect(Collectors.toMap(UserRef::id, ref -> ref));
    }

    // 모델 코드가 확정된 제품이면 Product 조회 결과, 아니면 고객이 입력한 모델명
    private String resolveModelName(ServiceRequest r, Map<Long, ProductInfo> products) {
        if (r.productId() != null) {
            ProductInfo product = products.get(r.productId());
            return product == null ? null : product.modelName();
        }
        return r.selectedModelName();
    }

    // 제품이 확정된 경우 서브카테고리는 표시 X
    private String resolveSubCategoryName(ServiceRequest r, Map<Long, SubCategoryInfo> subCategories) {
        if (r.productId() != null) {
            return null;
        }
        SubCategoryInfo subCategory = subCategories.get(r.selectedSubCategoryId());
        return subCategory == null ? null : subCategory.name();
    }

    // row 하나 조립
    private ServiceRequestListItem toListItem(ServiceRequest r, RequestViewContext c) {
        CustomerInfo customer = c.customers().get(r.customerId());
        UserRef technician = r.technicianId() == null ? null : c.technicians().get(r.technicianId());
        UserRef receptionist = c.receptionists().get(r.receptionistId());

        return new ServiceRequestListItem(
                r.id(),
                r.requestNumber(),
                customer == null ? null : customer.name(),
                customer == null ? null : customer.phoneNumber(),
                resolveModelName(r, c.products()),
                resolveSubCategoryName(r, c.subCategories()),
                r.symptom(),
                r.status(),
                receptionist == null ? null : receptionist.name(),
                technician == null ? null : technician.name(),
                r.visitDate(),
                r.visitStartTime(),
                r.visitEndTime(),
                r.requestedAt());
    }

    // 5개 Port 결과를 한 번에 들고 다니는 내부 전용 묶음
    private record RequestViewContext(
            Map<Long, CustomerInfo> customers,
            Map<Long, ProductInfo> products,
            Map<Long, SubCategoryInfo> subCategories,
            Map<Long, UserRef> technicians,
            Map<Long, UserRef> receptionists) {
    }
}