package com.modoospace.reservation.controller.dto.search;

import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommonSearchRequest {

    private final Long visitorId;

    private final Long hostId;

    private final Long spaceId;

    private final String spaceName;

    private final ReservationStatus status;


    public CommonSearchRequest(AdminSearchRequest searchRequest) {
        this(searchRequest.getVisitorId(), searchRequest.getHostId(), searchRequest.getSpaceId(),
                searchRequest.getSpaceName(), searchRequest.getStatus());
    }

    public CommonSearchRequest(HostSearchRequest searchRequest, Member host) {
        this(null, host.getId(), searchRequest.getSpaceId(), searchRequest.getSpaceName(),
                searchRequest.getStatus());
    }

    public CommonSearchRequest(VisitorSearchRequest searchRequest, Member visitor) {
        this(visitor.getId(), null, null, null, searchRequest.getStatus());
    }

    @Builder
    public CommonSearchRequest(Long visitorId, Long hostId, Long spaceId, String spaceName,
            ReservationStatus status) {
        this.visitorId = visitorId;
        this.hostId = hostId;
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.status = status;
    }
}
