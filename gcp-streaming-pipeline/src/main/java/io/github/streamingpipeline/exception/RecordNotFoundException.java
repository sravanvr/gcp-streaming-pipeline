package io.github.streamingpipeline.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends AccountsPipelineException {
	
    public RecordNotFoundException(UUID id) {
        super("Could not find metadata record for " + id);
    }
    
    public RecordNotFoundException(UUID id1, UUID id2) {
        super(String.format("Could not find metadata record for {} and {}", id1, id2));
    }
    
    public RecordNotFoundException(Long id) {
        super("Could not find Record for " + id);
    }
    
    public RecordNotFoundException(String id) {
        super("Could not find Record for " + id);
    }
}
