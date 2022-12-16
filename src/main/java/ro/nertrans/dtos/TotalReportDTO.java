package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalReportDTO {
    private String docSeries;
    private double amountRon;
    private double amountEur;
    private double amountUsd;
}
