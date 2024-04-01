package com.modoospace.reservation.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.reservation.controller.dto.search.AdminSearchRequest;
import com.modoospace.reservation.controller.dto.search.HostSearchRequest;
import com.modoospace.reservation.controller.dto.search.VisitorSearchRequest;
import com.modoospace.reservation.domain.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationSearchRequest {

    private Long visitorId;

    private Long hostId;

    private Long spaceId;

    private String spaceName;

    private ReservationStatus status;


    public ReservationSearchRequest(AdminSearchRequest searchRequest) {
        this(searchRequest.getVisitorId(), searchRequest.getHostId(), searchRequest.getSpaceId(), searchRequest.getSpaceName(), searchRequest.getStatus());
    }

    public ReservationSearchRequest(HostSearchRequest searchRequest, Member host) {
        this(null, host.getId(), searchRequest.getSpaceId(), searchRequest.getSpaceName(), searchRequest.getStatus());
    }

    public ReservationSearchRequest(VisitorSearchRequest searchRequest, Member visitor) {
        this(visitor.getId(), null, null, null, searchRequest.getStatus());
    }

    @Builder
    public ReservationSearchRequest(Long visitorId, Long hostId, Long spaceId, String spaceName, ReservationStatus status) {
        this.visitorId = visitorId;
        this.hostId = hostId;
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.status = status;
    }
}
