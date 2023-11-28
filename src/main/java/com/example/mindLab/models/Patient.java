package com.example.mindLab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "patient")
public class Patient implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String fullname;
    private LocalDate birthDate ;
    private String age ;
    private String strongHand;
    private boolean hasDiseases;
    private String diseases;


    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ExperimentSettings> experimentSettingsList;

}
