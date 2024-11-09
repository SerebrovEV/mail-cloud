package org.image.core.dto.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {

    private String recipientEmail;
    private String subject;
    private String body;

    public String printEventInfo() {
        return "%s %s".formatted(recipientEmail, subject);
    }
}
