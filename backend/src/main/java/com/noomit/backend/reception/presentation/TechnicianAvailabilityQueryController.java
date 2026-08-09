package com.noomit.backend.reception.presentation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.TechnicianAvailabilityQueryService;
import com.noomit.backend.reception.application.AvailabilityTimeSlot;
import com.noomit.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counselor/reception/technicians/availability")
@RequiredArgsConstructor
class TechnicianAvailabilityQueryController {
    private final TechnicianAvailabilityQueryService technicianAvailabilityQueryService;

    @GetMapping("/slots")
    ApiResponse<List<AvailabilityTimeSlot>> getTimeSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AvailabilityTimeSlot> slots = technicianAvailabilityQueryService.getByDate(date);
        return ApiResponse.success(slots);
    }
}