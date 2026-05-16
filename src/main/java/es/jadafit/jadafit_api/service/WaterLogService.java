package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.WaterLogCreateDTO;
import es.jadafit.jadafit_api.dto.WaterLogResponseDTO;
import es.jadafit.jadafit_api.dto.WaterDaySummaryDTO;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.WaterLog;
import es.jadafit.jadafit_api.repository.WaterLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WaterLogService {

    private final UserService userService;
    private final WaterLogRepository waterLogRepository;

    public WaterLogService(UserService userService, WaterLogRepository waterLogRepository) {
        this.userService = userService;
        this.waterLogRepository = waterLogRepository;
    }

    @Transactional
    public WaterLogResponseDTO createLog(UUID userId, WaterLogCreateDTO dto) {
        User user = userService.getUserById(userId);

        LocalDateTime loggedAt = dto.loggedAt() != null
                ? dto.loggedAt()
                : LocalDateTime.now();

        WaterLog log = WaterLog.builder()
                .user(user)
                .amountMl(dto.amountMl())
                .loggedAt(loggedAt)
                .build();

        WaterLog saved = waterLogRepository.save(log);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WaterDaySummaryDTO getTodaySummary(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<WaterLog> logs = waterLogRepository
                .findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, start, end);

        BigDecimal total = logs.stream()
                .map(WaterLog::getAmountMl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<WaterLogResponseDTO> logDTOs = logs.stream()
                .map(this::toResponse)
                .toList();

        return new WaterDaySummaryDTO(total, logDTOs);
    }

    @Transactional
    public void deleteLog(UUID userId, UUID logId) {
        WaterLog log = waterLogRepository.findByIdAndUserId(logId, userId)
                .orElseThrow(() -> new NotFoundException("Registro de agua no encontrado"));

        waterLogRepository.delete(log);
    }

    private WaterLogResponseDTO toResponse(WaterLog log) {
        return new WaterLogResponseDTO(
                log.getId(),
                log.getAmountMl(),
                log.getLoggedAt()
        );
    }
}
