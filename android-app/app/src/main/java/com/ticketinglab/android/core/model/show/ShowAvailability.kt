package com.ticketinglab.android.core.model.show

data class ShowAvailability(
    val seats: List<ShowSeatAvailability>,
    val sections: List<SectionAvailability>,
)

data class ShowSeatAvailability(
    val seatId: Long,
    val label: String,
    val rowNo: Int,
    val colNo: Int,
    val price: Int,
    val available: Boolean,
)

data class SectionAvailability(
    val sectionId: Long,
    val name: String,
    val price: Int,
    val remainingQty: Int,
)