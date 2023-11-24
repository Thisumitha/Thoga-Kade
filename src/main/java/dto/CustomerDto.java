package dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CustomerDto {
    private String id;
    private String name;
    private String address;
    private double salary;


}
