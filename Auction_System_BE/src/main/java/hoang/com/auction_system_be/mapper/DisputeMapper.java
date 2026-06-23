package hoang.com.auction_system_be.mapper;

import hoang.com.auction_system_be.dto.response.DisputeResponse;
import hoang.com.auction_system_be.entity.Dispute;
import org.springframework.stereotype.Component;

@Component
public class DisputeMapper {

    public DisputeResponse toResponse(Dispute dispute) {
        if (dispute == null) {
            return null;
        }

        String raisedByName = null;
        if (dispute.getRaisedBy() != null) {
            raisedByName = dispute.getRaisedBy().getFirstName() + " " + dispute.getRaisedBy().getLastName();
        }

        return DisputeResponse.builder()
                .id(dispute.getId())
                .sessionId(dispute.getSession() != null ? dispute.getSession().getId() : null)
                .raisedById(dispute.getRaisedBy() != null ? dispute.getRaisedBy().getId() : null)
                .raisedByName(raisedByName)
                .type(dispute.getType())
                .description(dispute.getDescription())
                .status(dispute.getStatus())
                .assignedManagerId(dispute.getAssignedManager() != null ? dispute.getAssignedManager().getId() : null)
                .resolution(dispute.getResolution())
                .resolvedAt(dispute.getResolvedAt())
                .createdAt(dispute.getCreatedAt())
                .build();
    }
}
