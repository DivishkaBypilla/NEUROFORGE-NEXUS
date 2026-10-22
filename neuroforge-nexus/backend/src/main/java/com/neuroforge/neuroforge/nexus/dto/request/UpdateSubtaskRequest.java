package com.neuroforge.neuroforge.nexus.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSubtaskRequest {

    String title;

    String assigneeId;
}