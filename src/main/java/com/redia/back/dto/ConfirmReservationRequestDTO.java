package com.redia.back.dto;

import java.util.List;

public record ConfirmReservationRequestDTO(

        List<String> mesasIds) {
}
