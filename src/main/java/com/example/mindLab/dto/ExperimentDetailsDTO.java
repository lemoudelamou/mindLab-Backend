package com.example.mindLab.dto;


import com.example.mindLab.models.AverageReactionTimes;
import com.example.mindLab.models.ExperimentSettings;
import com.example.mindLab.models.Patient;
import com.example.mindLab.models.ReactionTimes;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class ExperimentDetailsDTO  implements Serializable {
    private Patient patient;
    private ExperimentSettings experimentSettings;
    private List<ReactionTimes> reactionTimes;
    private AverageReactionTimes averageReactionTimes;

}

