package com.noomit.backend.reception.presentation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.noomit.backend.reception.application.AvailabilityTimeSlot;
import com.noomit.backend.reception.application.AvailableTechnician;
import com.noomit.backend.reception.application.TechnicianAvailabilityService;
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
    private final TechnicianAvailabilityService technicianAvailabilityService;

    @GetMapping("/slots")
    ApiResponse<List<AvailabilityTimeSlot>> getTimeSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AvailabilityTimeSlot> slots = technicianAvailabilityService.getByDate(date);
        return ApiResponse.success(slots);
    }

    @GetMapping("/dates")
    ApiResponse<List<LocalDate>> getAvailableDates() {
        return ApiResponse.success(technicianAvailabilityService.getAvailableDates());
    }

    @GetMapping
    ApiResponse<List<AvailableTechnicianResponse>> getAvailableTechnicians(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        List<AvailableTechnician> technicians = technicianAvailabilityService.getAvailableTechnicians(date, startTime, endTime);
        return ApiResponse.success(technicians.stream()
                .map(AvailableTechnicianResponse::from)
                .toList());
    }

    record AvailableTechnicianResponse(
            String technicianId, String technicianName, String slotId, LocalTime startTime, LocalTime endTime) {
        static AvailableTechnicianResponse from(AvailableTechnician t) {
            return new AvailableTechnicianResponse(
                    Long.toString(t.technicianId()), t.technicianName(), Long.toString(t.slotId()),
                    t.startTime(), t.endTime());
        }
    }
}