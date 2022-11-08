package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocExportDTO {
    private String date;
    private long docNumbers;
    private double totalEuro;
    private double relatedWarranty;
}
