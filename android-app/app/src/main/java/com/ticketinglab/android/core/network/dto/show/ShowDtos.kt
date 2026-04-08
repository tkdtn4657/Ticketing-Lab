package com.ticketinglab.android.core.network.dto.show

import com.ticketinglab.android.core.model.show.SectionAvailability
import com.ticketinglab.android.core.model.show.ShowAvailability
import com.ticketinglab.android.core.model.show.ShowSeatAvailability
import kotlinx.serialization.Serializable

@Serializable
data class ShowAvailabilityResponseDto(
    val seats: List<ShowSeatAvailabilityDto>,
    val sections: List<SectionAvailabilityDto>,
)

@Serializable
data class ShowSeatAvailabilityDto(
    val seatId: Long,
    val label: String,
    val rowNo: Int,
    val colNo: Int,
    val price: Int,
    val available: Boolean,
)

@Serializable
data class SectionAvailabilityDto(
    val sectionId: Long,
    val name: String,
    val price: Int,
    val remainingQty: Int,
)

fun ShowAvailabilityResponseDto.toModel(): ShowAvailability = ShowAvailability(
    seats = seats.map(ShowSeatAvailabilityDto::toModel),
    sections = sections.map(SectionAvailabilityDto::toModel),
)

fun ShowSeatAvailabilityDto.toModel(): ShowSeatAvailability = ShowSeatAvailability(
    seatId = seatId,
    label = label,
    rowNo = rowNo,
    colNo = colNo,
    price = price,
    available = available,
)

fun SectionAvailabilityDto.toModel(): SectionAvailability = SectionAvailability(
    sectionId = sectionId,
    name = name,
    price = price,
    remainingQty = remainingQty,
)