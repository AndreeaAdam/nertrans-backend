package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalReportPdfDTO {
    private ArrayList<TotalReportDTO> totalReportDTOS;
    private double totalRon;
    private double totalEur;
    private double totalUsd;
}
