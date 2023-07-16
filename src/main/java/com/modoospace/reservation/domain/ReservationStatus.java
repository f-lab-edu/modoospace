package com.modoospace.reservation.domain;

import java.util.Arrays;
import java.util.List;

public enum ReservationStatus {
  WAITING, // 예약 승인 대기 중
  CANCELED, // 예약 취소됨
  COMPLETED; // 예약 완료됨

  /**
   * 활성화된 예약 상태(예약 대기, 예약 완료)를 포함하는 상태 리스트를 반환합니다.
   *
   * @return 활성화된 예약 상태 리스트
   */
  public static List<ReservationStatus> getActiveStatuses() {
    return Arrays.asList(WAITING, COMPLETED);
  }
}
