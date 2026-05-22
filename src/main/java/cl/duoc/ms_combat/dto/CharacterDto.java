package cl.duoc.ms_combat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterDto {
    private Long id;
    private Long userId;
    private String name;
    private Integer level;
    private String status;
}