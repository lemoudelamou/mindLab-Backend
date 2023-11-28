package com.example.mindLab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experiment_settings")
public class ExperimentSettings implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String shape;
    private String experimentLength;
    private String blinkDelay;
    private String isColorBlind;
    private String difficultyLevel;
    private String color1;
    private String color2;
    private String color3;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

}
