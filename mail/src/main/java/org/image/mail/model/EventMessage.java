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
public class EventMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String recipientEmail;
    private String subject;
    private String body;
}
