package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "trade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDTO {
    @Id
    @GeneratedValue
    private int id;
    private int amount;
    private int rank;

    @OneToOne
    private RsEventDto rsEventDto;
}
