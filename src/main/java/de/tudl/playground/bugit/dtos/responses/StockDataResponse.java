package de.tudl.playground.bugit.dtos.responses;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockDataResponse {
    private Instant time;
    private String ticker;
    private Double adjClose;
    private Double close;
    private Double high;
    private Double low;
    private Double open;
    private Long volume;
}
