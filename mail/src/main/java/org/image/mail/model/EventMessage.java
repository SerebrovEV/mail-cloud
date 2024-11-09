package org.image.mail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

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
