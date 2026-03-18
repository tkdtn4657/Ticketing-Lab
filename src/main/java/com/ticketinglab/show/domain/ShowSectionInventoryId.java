package com.ticketinglab.show.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ShowSectionInventoryId implements Serializable {

    private Long show;
    private Long section;
}
