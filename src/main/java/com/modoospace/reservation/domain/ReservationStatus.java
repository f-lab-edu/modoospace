package com.modoospace.reservation.domain;

public enum ReservationStatus {
  WAITING, // 예약 승인 대기 중
  APPROVED, // 예약 승인됨
  CANCELED, // 예약 취소됨
  COMPLETED // 예약 완료됨
}
