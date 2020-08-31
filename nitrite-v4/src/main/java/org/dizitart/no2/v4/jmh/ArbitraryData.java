package org.dizitart.no2.v4.jmh;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dizitart.no2.repository.annotations.Id;

/**
 * @author Anindya Chatterjee
 */
@Data
@Accessors(fluent = true, chain = true)
public class ArbitraryData {
    @Id
    private Integer id;
    private String text;
    private Double number1;
    private Double number2;
    private Integer index1;
    private Boolean flag1;
    private Boolean flag2;
}
