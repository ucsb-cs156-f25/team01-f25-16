package edu.ucsb.cs156.example.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsbdiningcommonsmenuitems")
public class UCSBDiningCommonsMenuItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "DINING_COMMONS_CODE")
  private String diningCommonsCode;

  @Column(name = "NAME")
  private String name;

  @Column(name = "STATION")
  private String station;
}
